package ru.createadvancedpost;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.simibubi.create.content.contraptions.actors.seat.SeatBlock;
import com.simibubi.create.content.contraptions.actors.seat.SeatEntity;
import com.simibubi.create.content.logistics.box.PackageItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import net.neoforged.neoforge.items.wrapper.PlayerMainInvWrapper;

public final class DeliveryStationEvents {
    private static final String ANNOUNCED_OPERATOR_KEY = "create_post_operator_announced";
    private static final int STATION_RADIUS = 4;
    private static final Pattern WAYBILL_NAME = Pattern.compile("^(?:ТТН|Waybill): ([0-9]{6})$");

    private DeliveryStationEvents() {
    }

    @SubscribeEvent
    public static void onLivingEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof LivingEntity mob)
                || mob instanceof Player
                || mob.tickCount % 20 != 0
                || !(mob.level() instanceof ServerLevel level)) {
            return;
        }

        SeatEntity seat = getCreateSeat(mob);
        if (seat == null || !hasNearbyStorage(level, seat.blockPosition())) {
            mob.getPersistentData().remove(ANNOUNCED_OPERATOR_KEY);
            return;
        }

        if (mob.getPersistentData().getBoolean(ANNOUNCED_OPERATOR_KEY)) {
            return;
        }

        mob.getPersistentData().putBoolean(ANNOUNCED_OPERATOR_KEY, true);
        Component message = Component.literal("[Create: AdvancedPost] ")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)
                .append(Component.translatable(
                        "chat.create_advancedpost.station.operator_assigned",
                        mob.getName().copy().withStyle(ChatFormatting.WHITE)
                ).withStyle(ChatFormatting.GRAY));
        level.getServer().getPlayerList().broadcastSystemMessage(message, false);
    }

    @SubscribeEvent
    public static void onOperatorInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || !(event.getTarget() instanceof LivingEntity mob)
                || mob instanceof Player
                || !(mob.level() instanceof ServerLevel level)) {
            return;
        }

        SeatEntity seat = getCreateSeat(mob);
        if (seat == null || !hasNearbyStorage(level, seat.blockPosition())) {
            return;
        }

        ItemStack ticket = player.getItemInHand(event.getHand());
        String waybill = ticket.is(Items.PAPER) ? getTtnCode(ticket) : null;
        if (waybill == null) {
            return;
        }

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);

        PackageMatch match = findPackage(level, seat.blockPosition(), waybill);
        if (match == null) {
            player.displayClientMessage(
                    ticketMessage(
                            "chat.create_advancedpost.parcel.not_found",
                            waybill,
                            ChatFormatting.RED
                    ),
                    false
            );
            return;
        }

        IItemHandler playerInventory = new PlayerMainInvWrapper(player.getInventory());
        ItemStack storedParcel = match.handler().getStackInSlot(match.slot()).copy();
        if (!ItemHandlerHelper.insertItemStacked(
                        playerInventory,
                        storedParcel,
                        true
                ).isEmpty()) {
            player.displayClientMessage(
                    Component.literal("[Create: AdvancedPost] ")
                            .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)
                            .append(Component.translatable("chat.create_advancedpost.parcel.inventory_full")
                                    .withStyle(ChatFormatting.GRAY)),
                    false
            );
            return;
        }

        ItemStack parcel = match.handler().extractItem(match.slot(), 1, false);
        if (parcel.isEmpty() || !PackageItem.isPackage(parcel) || !waybill.equals(getTtnCode(parcel))) {
            player.displayClientMessage(
                    ticketMessage(
                            "chat.create_advancedpost.parcel.not_found",
                            waybill,
                            ChatFormatting.RED
                    ),
                    false
            );
            return;
        }

        ItemStack remainder = ItemHandlerHelper.insertItemStacked(playerInventory, parcel, false);
        if (!remainder.isEmpty()) {
            ItemStack returned = match.handler().insertItem(match.slot(), remainder, false);
            if (!returned.isEmpty()) {
                player.drop(returned, false);
            }
            player.displayClientMessage(
                    Component.literal("[Create: AdvancedPost] ")
                            .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)
                            .append(Component.translatable("chat.create_advancedpost.parcel.delivery_failed")
                                    .withStyle(ChatFormatting.RED)),
                    false
            );
            return;
        }

        ticket.shrink(1);
        player.displayClientMessage(
                ticketMessage(
                        "chat.create_advancedpost.parcel.received",
                        waybill,
                        ChatFormatting.GOLD
                ),
                false
        );
    }

    private static SeatEntity getCreateSeat(LivingEntity mob) {
        Entity vehicle = mob.getVehicle();
        if (!(vehicle instanceof SeatEntity seat)
                || !(seat.level().getBlockState(seat.blockPosition()).getBlock() instanceof SeatBlock)) {
            return null;
        }
        return seat;
    }

    private static boolean hasNearbyStorage(ServerLevel level, BlockPos seatPos) {
        for (int y = -STATION_RADIUS; y <= STATION_RADIUS; y++) {
            for (int x = -STATION_RADIUS; x <= STATION_RADIUS; x++) {
                for (int z = -STATION_RADIUS; z <= STATION_RADIUS; z++) {
                    if (x * x + y * y + z * z > STATION_RADIUS * STATION_RADIUS) {
                        continue;
                    }
                    if (getItemHandler(level, seatPos.offset(x, y, z)) != null) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static PackageMatch findPackage(ServerLevel level, BlockPos seatPos, String waybill) {
        for (int y = -STATION_RADIUS; y <= STATION_RADIUS; y++) {
            for (int x = -STATION_RADIUS; x <= STATION_RADIUS; x++) {
                for (int z = -STATION_RADIUS; z <= STATION_RADIUS; z++) {
                    if (x * x + y * y + z * z > STATION_RADIUS * STATION_RADIUS) {
                        continue;
                    }

                    BlockPos pos = seatPos.offset(x, y, z);
                    IItemHandler handler = getItemHandler(level, pos);
                    if (handler == null) {
                        continue;
                    }
                    for (int slot = 0; slot < handler.getSlots(); slot++) {
                        ItemStack stack = handler.getStackInSlot(slot);
                        if (PackageItem.isPackage(stack) && waybill.equals(getTtnCode(stack))) {
                            return new PackageMatch(handler, slot);
                        }
                    }
                }
            }
        }
        return null;
    }

    private static IItemHandler getItemHandler(Level level, BlockPos pos) {
        if (!level.hasChunkAt(pos)) {
            return null;
        }

        IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, null);
        if (handler != null && handler.getSlots() > 0) {
            return handler;
        }
        for (Direction direction : Direction.values()) {
            handler = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, direction);
            if (handler != null && handler.getSlots() > 0) {
                return handler;
            }
        }

        if (level.getBlockEntity(pos) instanceof Container container && container.getContainerSize() > 0) {
            return new InvWrapper(container);
        }
        return null;
    }

    private static String getTtnCode(ItemStack stack) {
        String name = getCustomName(stack);
        if (name == null) {
            return null;
        }
        Matcher matcher = WAYBILL_NAME.matcher(name);
        return matcher.matches() ? matcher.group(1) : null;
    }

    private static String getCustomName(ItemStack stack) {
        Component name = stack.get(DataComponents.CUSTOM_NAME);
        return name == null ? null : name.getString();
    }

    private static Component ticketMessage(String messageKey, String waybill, ChatFormatting color) {
        return Component.literal("[Create: AdvancedPost] ")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)
                .append(Component.translatable(
                        messageKey,
                        Component.literal(waybill).withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD)
                ).withStyle(color));
    }

    private record PackageMatch(IItemHandler handler, int slot) {
    }
}
