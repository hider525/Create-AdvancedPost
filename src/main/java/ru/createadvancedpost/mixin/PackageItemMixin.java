package ru.createadvancedpost.mixin;

import com.simibubi.create.content.logistics.box.PackageItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(value = PackageItem.class, remap = false)
public abstract class PackageItemMixin {
    @ModifyConstant(
            method = {"containing", "getContents"},
            constant = @Constant(intValue = 9)
    )
    private static int createPost$expandPackageInventory(int original) {
        return 27;
    }
}
