package ru.createadvancedpost;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.saveddata.SavedData;

public final class TtnData extends SavedData {
    private static final String DATA_ID = "create_post_ttns";
    private static final int TOTAL_CODES = 1_000_000;
    private static final Factory<TtnData> FACTORY =
            new Factory<>(TtnData::new, TtnData::load, DataFixTypes.LEVEL);

    private final Set<String> issuedCodes = new HashSet<>();
    private final Map<UUID, Set<String>> pendingWaybills = new HashMap<>();

    private TtnData() {
    }

    private static TtnData load(CompoundTag tag, HolderLookup.Provider registries) {
        TtnData data = new TtnData();
        ListTag codes = tag.getList("issued_codes", Tag.TAG_STRING);
        for (int i = 0; i < codes.size(); i++) {
            data.issuedCodes.add(codes.getString(i));
        }
        ListTag pending = tag.getList("pending_waybills", Tag.TAG_COMPOUND);
        for (int i = 0; i < pending.size(); i++) {
            CompoundTag entry = pending.getCompound(i);
            Set<String> recipientCodes = new HashSet<>();
            ListTag recipientCodeList = entry.getList("codes", Tag.TAG_STRING);
            for (int j = 0; j < recipientCodeList.size(); j++) {
                recipientCodes.add(recipientCodeList.getString(j));
            }
            data.pendingWaybills.put(entry.getUUID("recipient"), recipientCodes);
        }
        return data;
    }

    public static String issue(ServerLevel level) {
        TtnData data = get(level);
        String code;
        do {
            if (data.issuedCodes.size() >= TOTAL_CODES) {
                throw new IllegalStateException("All six-digit TTN codes have been issued");
            }
            String digits = Integer.toString(ThreadLocalRandom.current().nextInt(TOTAL_CODES));
            code = "0".repeat(6 - digits.length()) + digits;
        } while (data.issuedCodes.contains(code));

        data.issuedCodes.add(code);
        data.setDirty();
        return code;
    }

    public static void deliverOrQueue(ServerLevel level, UUID recipientId, String code) {
        ServerPlayer recipient = level.getServer().getPlayerList().getPlayer(recipientId);
        if (recipient != null) {
            giveWaybill(recipient, code);
            return;
        }

        TtnData data = get(level);
        data.pendingWaybills.computeIfAbsent(recipientId, ignored -> new HashSet<>()).add(code);
        data.setDirty();
    }

    public static void deliverPending(ServerPlayer player) {
        TtnData data = get(player.serverLevel());
        Set<String> codes = data.pendingWaybills.remove(player.getUUID());
        if (codes == null || codes.isEmpty()) {
            return;
        }
        data.setDirty();
        for (String code : codes) {
            giveWaybill(player, code);
        }
    }

    private static TtnData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(FACTORY, DATA_ID);
    }

    private static void giveWaybill(ServerPlayer player, String code) {
        ItemStack paper = new ItemStack(Items.PAPER);
        paper.set(
                DataComponents.CUSTOM_NAME,
                Component.translatable("item.create_advancedpost.waybill", code)
                        .withStyle(net.minecraft.ChatFormatting.GOLD)
        );
        player.getInventory().placeItemBackInInventory(paper);
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag codes = new ListTag();
        for (String code : issuedCodes) {
            codes.add(StringTag.valueOf(code));
        }
        tag.put("issued_codes", codes);
        ListTag pending = new ListTag();
        for (Map.Entry<UUID, Set<String>> entry : pendingWaybills.entrySet()) {
            CompoundTag pendingEntry = new CompoundTag();
            pendingEntry.putUUID("recipient", entry.getKey());
            ListTag recipientCodes = new ListTag();
            for (String code : entry.getValue()) {
                recipientCodes.add(StringTag.valueOf(code));
            }
            pendingEntry.put("codes", recipientCodes);
            pending.add(pendingEntry);
        }
        tag.put("pending_waybills", pending);
        return tag;
    }
}
