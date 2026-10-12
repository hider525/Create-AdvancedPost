package ru.createadvancedpost.mixin;

import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorBlockEntity;
import com.simibubi.create.content.logistics.box.PackageItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = ChainConveyorBlockEntity.class, remap = false)
public abstract class ChainConveyorFallbackPortMixin {
    @Shadow
    public com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorRoutingTable routingTable;

    @Redirect(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/content/logistics/box/PackageItem;matchAddress(Lnet/minecraft/world/item/ItemStack;Ljava/lang/String;)Z"
            )
    )
    private boolean createPost$matchFallbackOnlyWhenNoDestination(ItemStack box, String filter) {
        if (!FrogportAddressFilters.FALLBACK.equals(filter)) {
            return PackageItem.matchAddress(box, filter);
        }

        for (var entry : routingTable.entriesByDistance) {
            if (!FrogportAddressFilters.FALLBACK.equals(entry.port())
                    && PackageItem.matchAddress(box, entry.port())) {
                return false;
            }
        }
        return true;
    }
}
