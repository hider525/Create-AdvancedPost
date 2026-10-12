package ru.createadvancedpost.mixin;

import com.simibubi.create.content.logistics.packager.repackager.PackageRepackageHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(value = PackageRepackageHelper.class, remap = false)
public abstract class PackageRepackageCapacityMixin {
    @ModifyConstant(
            method = {"repack", "repackBasedOnRecipes"},
            constant = @Constant(intValue = 9)
    )
    private int createPost$repackageMoreItemStacks(int original) {
        return 27;
    }
}
