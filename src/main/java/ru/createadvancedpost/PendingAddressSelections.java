package ru.createadvancedpost;

import java.util.Map;
import java.util.List;
import java.util.TreeSet;
import java.util.UUID;
import java.util.WeakHashMap;

import com.simibubi.create.content.equipment.clipboard.ClipboardBlock;
import com.simibubi.create.content.equipment.clipboard.ClipboardBlockEntity;
import com.simibubi.create.content.equipment.clipboard.ClipboardEntry;
import com.simibubi.create.content.logistics.packager.PackagerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.neoforged.neoforge.network.PacketDistributor;
import ru.createadvancedpost.network.CancelDeliveryPayload;
import ru.createadvancedpost.network.OpenDeliveryScreenPayload;

public final class PendingAddressSelections {
    private static final Map<PackagerBlockEntity, UUID> PENDING = new WeakHashMap<>();

    private PendingAddressSelections() {
    }

    public static void open(ServerPlayer player, PackagerBlockEntity packager) {
        PENDING.put(packager, player.getUUID());
        PacketDistributor.sendToPlayer(
                player,
                new OpenDeliveryScreenPayload(packager.getBlockPos(), findClipboardAddresses(
                        (ServerLevel) packager.getLevel(), packager.getBlockPos()))
        );
    }

    public static boolean isPending(PackagerBlockEntity packager) {
        UUID playerId = PENDING.get(packager);
        if (playerId == null) {
            return false;
        }
        if (!(packager.getLevel() instanceof ServerLevel level)) {
            return true;
        }

        ServerPlayer player = level.getServer().getPlayerList().getPlayer(playerId);
        if (player != null && player.serverLevel() == level) {
            return true;
        }

        PENDING.remove(packager);
        packager.redstonePowered = false;
        packager.setChanged();
        return false;
    }

    public static void submit(ServerPlayer player, BlockPos pos, String submittedAddress) {
        ServerLevel level = player.serverLevel();
        PackagerBlockEntity packager = getPendingPackager(player, pos, level);
        if (packager == null) {
            return;
        }

        String address = submittedAddress.trim();
        if (address.isEmpty() || address.length() > 128) {
            player.displayClientMessage(
                    net.minecraft.network.chat.Component.translatable(
                            "chat.create_advancedpost.delivery.address_required"
                    ),
                    true
            );
            return;
        }

        PENDING.remove(packager);
        DeliverySenders.set(packager, player);
        try {
            packager.signBasedAddress = address;
            packager.attemptToSend(null);
            if (packager.buttonCooldown <= 0) {
                packager.buttonCooldown = 40;
            }
        } finally {
            DeliverySenders.clear(packager);
        }
    }

    public static void cancel(ServerPlayer player, BlockPos pos) {
        PackagerBlockEntity packager = getPendingPackager(player, pos, player.serverLevel());
        if (packager == null) {
            return;
        }

        PENDING.remove(packager);
        packager.redstonePowered = false;
        packager.setChanged();
    }

    private static PackagerBlockEntity getPendingPackager(ServerPlayer player, BlockPos pos, ServerLevel level) {
        if (!level.hasChunkAt(pos) || !(level.getBlockEntity(pos) instanceof PackagerBlockEntity packager)) {
            return null;
        }
        UUID pendingPlayer = PENDING.get(packager);
        return player.getUUID().equals(pendingPlayer) ? packager : null;
    }

    private static List<String> findClipboardAddresses(ServerLevel level, BlockPos packagerPos) {
        TreeSet<String> addresses = new TreeSet<>(String.CASE_INSENSITIVE_ORDER.thenComparing(String::compareTo));
        for (Direction direction : Direction.values()) {
            BlockPos clipboardPos = packagerPos.relative(direction);
            if (!(level.getBlockEntity(clipboardPos) instanceof ClipboardBlockEntity clipboard)) {
                continue;
            }

            Direction supportDirection = getSupportDirection(clipboard.getBlockState());
            if (!clipboardPos.relative(supportDirection).equals(packagerPos)) {
                continue;
            }

            for (List<ClipboardEntry> page : ClipboardEntry.readAll(clipboard.components())) {
                for (ClipboardEntry entry : page) {
                    if (entry.checked) {
                        continue;
                    }
                    String address = entry.text.getString().trim();
                    if (address.startsWith("#")) {
                        address = address.substring(1).trim();
                    }
                    if (!address.isEmpty()) {
                        addresses.add(address);
                    }
                }
            }
        }
        return List.copyOf(addresses);
    }

    private static Direction getSupportDirection(net.minecraft.world.level.block.state.BlockState state) {
        AttachFace face = state.getValue(ClipboardBlock.FACE);
        return switch (face) {
            case CEILING -> Direction.UP;
            case FLOOR -> Direction.DOWN;
            case WALL -> state.getValue(HorizontalDirectionalBlock.FACING).getOpposite();
        };
    }
}
