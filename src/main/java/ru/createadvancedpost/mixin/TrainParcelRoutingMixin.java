package ru.createadvancedpost.mixin;

import com.simibubi.create.content.trains.entity.Train;
import com.simibubi.create.content.trains.station.GlobalStation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.createadvancedpost.TrainParcelRouting;

@Mixin(value = GlobalStation.class, remap = false)
public abstract class TrainParcelRoutingMixin {
    @Shadow
    @Nullable
    public abstract Train getPresentTrain();

    @Inject(method = "runMailTransfer", at = @At("HEAD"))
    private void create_advancedpost$beginTrainRouting(CallbackInfo callbackInfo) {
        TrainParcelRouting.begin(getPresentTrain());
    }

    @Inject(method = "runMailTransfer", at = @At("RETURN"))
    private void create_advancedpost$endTrainRouting(CallbackInfo callbackInfo) {
        TrainParcelRouting.end();
    }

    @Redirect(
            method = "runMailTransfer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/neoforged/neoforge/items/ItemHandlerHelper;insertItemStacked(Lnet/neoforged/neoforge/items/IItemHandler;Lnet/minecraft/world/item/ItemStack;Z)Lnet/minecraft/world/item/ItemStack;",
                    ordinal = 0
            )
    )
    private ItemStack create_advancedpost$routeIncomingPackage(
            IItemHandler inventory, ItemStack stack, boolean simulate) {
        if (simulate) {
            return ItemHandlerHelper.insertItemStacked(inventory, stack, true);
        }
        return TrainParcelRouting.insert(inventory, stack, false);
    }
}
