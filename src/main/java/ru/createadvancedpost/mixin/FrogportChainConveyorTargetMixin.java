package ru.createadvancedpost.mixin;

import com.simibubi.create.content.logistics.packagePort.PackagePortBlockEntity;
import com.simibubi.create.content.logistics.packagePort.PackagePortTarget;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = PackagePortTarget.ChainConveyorFrogportTarget.class, remap = false)
public abstract class FrogportChainConveyorTargetMixin {
    @Inject(method = "register", at = @At("HEAD"), cancellable = true)
    private void createPost$preventForwardingPortFromReceiving(
            PackagePortBlockEntity port,
            LevelAccessor level,
            BlockPos portPos,
            CallbackInfo callbackInfo
    ) {
        if (FrogportAddressFilters.FORWARD.equals(port.getFilterString())) {
            callbackInfo.cancel();
        }
    }
}
