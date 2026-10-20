package ru.createadvancedpost.mixin;

import java.util.UUID;

import com.simibubi.create.content.logistics.packager.PackagerBlockEntity;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = PackagerBlockEntity.class, remap = false)
public abstract class PackagerBlockEntityMixin {
    @Inject(method = "activate", at = @At("HEAD"), cancellable = true)
    private void createPost$openDeliveryScreen(CallbackInfo callbackInfo) {
        PackagerBlockEntity packager = (PackagerBlockEntity) (Object) this;
        if (!(packager.getLevel() instanceof ServerLevel serverLevel)
                || !packager.redstoneModeActive()
                || packager.buttonCooldown > 0) {
            return;
        }

        ServerPlayer nearestPlayer = null;
        double nearestDistance = Double.MAX_VALUE;
        Vec3 center = Vec3.atCenterOf(packager.getBlockPos());
        for (ServerPlayer player : serverLevel.players()) {
            double distance = player.distanceToSqr(center);
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearestPlayer = player;
            }
        }
        if (nearestPlayer == null) {
            return;
        }

        packager.redstonePowered = true;
        packager.setChanged();
        ru.createadvancedpost.PendingAddressSelections.open(nearestPlayer, packager);
        callbackInfo.cancel();
    }

    @Inject(method = "lazyTick", at = @At("HEAD"), cancellable = true)
    private void createPost$waitForAddress(CallbackInfo callbackInfo) {
        if (ru.createadvancedpost.PendingAddressSelections.isPending((PackagerBlockEntity) (Object) this)) {
            callbackInfo.cancel();
        }
    }

    @ModifyArg(
            method = "attemptToSend",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/content/logistics/box/PackageItem;clearAddress(Lnet/minecraft/world/item/ItemStack;)V"
            )
    )
    private ItemStack createPost$labelPackage(ItemStack box) {
        PackagerBlockEntity packager = (PackagerBlockEntity) (Object) this;
        Level level = packager.getLevel();
        if (!(level instanceof ServerLevel serverLevel)) {
            return box;
        }

        String code = ru.createadvancedpost.TtnData.issue(
                serverLevel,
                packager.signBasedAddress,
                packager.getBlockPos()
        );
        Component label = Component.translatable("item.create_advancedpost.waybill", code)
                .withStyle(net.minecraft.ChatFormatting.GOLD);
        box.set(DataComponents.CUSTOM_NAME, label);

        UUID senderId = ru.createadvancedpost.DeliverySenders.get(packager);
        if (senderId != null) {
            ServerPlayer sender = serverLevel.getServer().getPlayerList().getPlayer(senderId);
            if (sender != null) {
                ItemStack paper = new ItemStack(Items.PAPER);
                paper.set(DataComponents.CUSTOM_NAME, label);
                sender.getInventory().placeItemBackInInventory(paper);
            }
            ru.createadvancedpost.DeliverySenders.clear(packager);
        }

        return box;
    }
}
