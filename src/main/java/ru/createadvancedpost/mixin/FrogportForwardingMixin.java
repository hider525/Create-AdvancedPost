package ru.createadvancedpost.mixin;

import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.packagePort.frogport.FrogportBlockEntity;
import com.simibubi.create.content.logistics.packagePort.postbox.PostboxBlockEntity;
import com.simibubi.create.foundation.item.ItemHelper;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = FrogportBlockEntity.class, remap = false)
public abstract class FrogportForwardingMixin {
    @Shadow
    protected abstract IItemHandler getAdjacentInventory(Direction side);

    @Inject(method = "tryPullingFrom", at = @At("HEAD"), cancellable = true)
    private void createPost$pullOwnAddressPackages(
            IItemHandler handler,
            CallbackInfoReturnable<Boolean> callbackInfo
    ) {
        FrogportBlockEntity frogport = (FrogportBlockEntity) (Object) this;
        String filter = frogport.getFilterString();
        if (FrogportAddressFilters.FALLBACK.equals(filter)) {
            callbackInfo.setReturnValue(false);
            return;
        }
        if (!FrogportAddressFilters.FORWARD.equals(filter)) {
            return;
        }

        ItemStack extract = ItemHelper.extract(handler, PackageItem::isPackage, false);

        if (extract.isEmpty()) {
            callbackInfo.setReturnValue(false);
            return;
        }

        frogport.startAnimation(extract, true);
        callbackInfo.setReturnValue(true);
    }

    @Inject(method = "tryPullingFromOwnAndAdjacentInventories", at = @At("HEAD"), cancellable = true)
    private void createPost$forwardAnyPackageAndKeepFallbackReceiveOnly(CallbackInfo callbackInfo) {
        FrogportBlockEntity frogport = (FrogportBlockEntity) (Object) this;
        if (FrogportAddressFilters.FALLBACK.equals(frogport.getFilterString())) {
            callbackInfo.cancel();
            return;
        }
        if (!FrogportAddressFilters.FORWARD.equals(frogport.getFilterString())) {
            return;
        }

        if (frogport.target == null || !frogport.target.export(
                frogport.getLevel(),
                frogport.getBlockPos(),
                com.simibubi.create.content.logistics.box.PackageStyles.getDefaultBox(),
                true
        )) {
            callbackInfo.cancel();
            return;
        }

        if (createPost$pullFromSource(frogport, frogport.inventory)) {
            callbackInfo.cancel();
            return;
        }

        IItemHandler below = createPost$getInventoryBelow(frogport);
        if (below != null) {
            createPost$pullFromSource(frogport, below);
        }
        callbackInfo.cancel();
    }

    @Inject(method = "tryPushingToAdjacentInventories", at = @At("HEAD"), cancellable = true)
    private void createPost$pushFallbackPackagesToStorage(CallbackInfo callbackInfo) {
        FrogportBlockEntity frogport = (FrogportBlockEntity) (Object) this;
        if (!FrogportAddressFilters.FALLBACK.equals(frogport.getFilterString())) {
            return;
        }

        IItemHandler storage = createPost$getInventoryBelow(frogport);
        if (storage == null) {
            callbackInfo.cancel();
            return;
        }

        IItemHandler buffer = frogport.inventory;
        boolean changed = false;
        for (int slot = 0; slot < buffer.getSlots(); slot++) {
            ItemStack stack = buffer.getStackInSlot(slot);
            if (!PackageItem.isPackage(stack)) {
                continue;
            }

            ItemStack simulated = stack.copyWithCount(1);
            if (!ItemHandlerHelper.insertItemStacked(storage, simulated, true).isEmpty()) {
                continue;
            }

            ItemStack parcel = buffer.extractItem(slot, 1, false);
            if (parcel.isEmpty()) {
                continue;
            }

            ItemStack remainder = ItemHandlerHelper.insertItemStacked(storage, parcel, false);
            if (!remainder.isEmpty()) {
                ItemStack returned = buffer.insertItem(slot, remainder, false);
                if (!returned.isEmpty()) {
                    frogport.drop(returned);
                }
            } else {
                changed = true;
            }
        }

        if (changed) {
            frogport.getLevel().blockEntityChanged(frogport.getBlockPos());
        }
        callbackInfo.cancel();
    }

    private boolean createPost$pullFromSource(FrogportBlockEntity frogport, IItemHandler source) {
        for (int slot = 0; slot < source.getSlots(); slot++) {
            ItemStack stack = source.getStackInSlot(slot);
            if (!PackageItem.isPackage(stack)
                    || source.extractItem(slot, 1, true).isEmpty()
                    || !frogport.target.export(
                            frogport.getLevel(),
                            frogport.getBlockPos(),
                            stack.copyWithCount(1),
                            true
                    )) {
                continue;
            }

            ItemStack parcel = source.extractItem(slot, 1, false);
            if (parcel.isEmpty()) {
                continue;
            }

            frogport.startAnimation(parcel, true);
            return true;
        }
        return false;
    }

    private IItemHandler createPost$getInventoryBelow(FrogportBlockEntity frogport) {
        if (frogport.getLevel() == null) {
            return null;
        }

        BlockEntity below = frogport.getLevel().getBlockEntity(frogport.getBlockPos().below());
        if (below instanceof PostboxBlockEntity postbox) {
            return postbox.inventory;
        }
        if (below instanceof Container container && container.getContainerSize() > 0) {
            return new InvWrapper(container);
        }
        return getAdjacentInventory(Direction.DOWN);
    }
}
