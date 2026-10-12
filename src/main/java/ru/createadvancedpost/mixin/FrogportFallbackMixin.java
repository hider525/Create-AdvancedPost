package ru.createadvancedpost.mixin;

import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorRoutingTable;
import com.simibubi.create.content.logistics.box.PackageItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ChainConveyorRoutingTable.class, remap = false)
public abstract class FrogportFallbackMixin {
    @Inject(method = "getExitFor", at = @At("HEAD"), cancellable = true)
    private void createPost$routeUnknownAddressesToFallback(
            ItemStack box,
            CallbackInfoReturnable<BlockPos> callbackInfo
    ) {
        ChainConveyorRoutingTable routingTable = (ChainConveyorRoutingTable) (Object) this;
        ChainConveyorRoutingTable.RoutingTableEntry fallback = null;
        for (ChainConveyorRoutingTable.RoutingTableEntry entry : routingTable.entriesByDistance) {
            if (FrogportAddressFilters.FALLBACK.equals(entry.port())) {
                if (fallback == null) {
                    fallback = entry;
                }
                continue;
            }
            if (PackageItem.matchAddress(box, entry.port())) {
                callbackInfo.setReturnValue(entry.nextConnection());
                return;
            }
        }

        callbackInfo.setReturnValue(fallback == null ? BlockPos.ZERO : fallback.nextConnection());
    }
}
