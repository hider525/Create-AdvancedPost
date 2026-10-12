package ru.createadvancedpost.mixin;

import com.simibubi.create.content.logistics.packager.PackagerBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(value = PackagerBlockEntity.class, remap = false)
public abstract class PackagerCapacityMixin {
    @ModifyConstant(
            method = "attemptToSend",
            constant = @Constant(intValue = 9)
    )
    private int createPost$packMoreItemStacks(int original) {
        return 27;
    }
}
