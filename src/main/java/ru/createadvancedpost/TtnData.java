package ru.createadvancedpost;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;

public final class TtnData extends SavedData {
    private static final String DATA_ID = "create_post_ttns";
    private static final int TOTAL_CODES = 1_000_000;
    private static final Factory<TtnData> FACTORY =
            new Factory<>(TtnData::new, TtnData::load, DataFixTypes.LEVEL);

    private final Set<String> issuedCodes = new HashSet<>();
    private final Map<String, TrackingRecord> trackingRecords = new HashMap<>();

    private TtnData() {
    }

    private static TtnData load(CompoundTag tag, HolderLookup.Provider registries) {
        TtnData data = new TtnData();
        ListTag codes = tag.getList("issued_codes", Tag.TAG_STRING);
        for (int i = 0; i < codes.size(); i++) {
            data.issuedCodes.add(codes.getString(i));
        }
        ListTag records = tag.getList("tracking_records", Tag.TAG_COMPOUND);
        for (int i = 0; i < records.size(); i++) {
            CompoundTag recordTag = records.getCompound(i);
            String code = recordTag.getString("code");
            if (data.issuedCodes.contains(code)) {
                data.trackingRecords.put(code, TrackingRecord.load(recordTag));
            }
        }
        return data;
    }

    public static String issue(ServerLevel level, String destination, BlockPos origin) {
        TtnData data = level.getServer().overworld().getDataStorage().computeIfAbsent(FACTORY, DATA_ID);
        String code;
        do {
            if (data.issuedCodes.size() >= TOTAL_CODES) {
                throw new IllegalStateException("All six-digit TTN codes have been issued");
            }
            String digits = Integer.toString(ThreadLocalRandom.current().nextInt(TOTAL_CODES));
            code = "0".repeat(6 - digits.length()) + digits;
        } while (data.issuedCodes.contains(code));

        data.issuedCodes.add(code);
        data.trackingRecords.put(code, new TrackingRecord(
                Objects.requireNonNullElse(destination, ""),
                level.getServer().overworld().getGameTime(),
                level.dimension().location().toString(),
                origin,
                -1,
                "",
                "",
                BlockPos.ZERO
        ));
        data.setDirty();
        return code;
    }

    public static Optional<TrackingRecord> getTracking(ServerLevel level, String code) {
        TtnData data = level.getServer().overworld().getDataStorage().computeIfAbsent(FACTORY, DATA_ID);
        return Optional.ofNullable(data.trackingRecords.get(code));
    }

    public static void markReceived(
            ServerLevel level, String code, String receiver, BlockPos position) {
        TtnData data = level.getServer().overworld().getDataStorage().computeIfAbsent(FACTORY, DATA_ID);
        TrackingRecord record = data.trackingRecords.get(code);
        if (record == null || record.isReceived()) {
            return;
        }

        data.trackingRecords.put(code, new TrackingRecord(
                record.destination(),
                record.createdAt(),
                record.originDimension(),
                record.origin(),
                level.getServer().overworld().getGameTime(),
                receiver,
                level.dimension().location().toString(),
                position
        ));
        data.setDirty();
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag codes = new ListTag();
        for (String code : issuedCodes) {
            codes.add(StringTag.valueOf(code));
        }
        tag.put("issued_codes", codes);
        ListTag records = new ListTag();
        trackingRecords.forEach((code, record) -> records.add(record.save(code)));
        tag.put("tracking_records", records);
        return tag;
    }

    public record TrackingRecord(
            String destination,
            long createdAt,
            String originDimension,
            BlockPos origin,
            long receivedAt,
            String receiver,
            String receivedDimension,
            BlockPos receivedPosition
    ) {
        public boolean isReceived() {
            return receivedAt >= 0;
        }

        private CompoundTag save(String code) {
            CompoundTag tag = new CompoundTag();
            tag.putString("code", code);
            tag.putString("destination", destination);
            tag.putLong("created_at", createdAt);
            tag.putString("origin_dimension", originDimension);
            tag.putInt("origin_x", origin.getX());
            tag.putInt("origin_y", origin.getY());
            tag.putInt("origin_z", origin.getZ());
            tag.putLong("received_at", receivedAt);
            tag.putString("receiver", receiver);
            tag.putString("received_dimension", receivedDimension);
            tag.putInt("received_x", receivedPosition.getX());
            tag.putInt("received_y", receivedPosition.getY());
            tag.putInt("received_z", receivedPosition.getZ());
            return tag;
        }

        private static TrackingRecord load(CompoundTag tag) {
            return new TrackingRecord(
                    tag.getString("destination"),
                    tag.getLong("created_at"),
                    tag.getString("origin_dimension"),
                    new BlockPos(tag.getInt("origin_x"), tag.getInt("origin_y"), tag.getInt("origin_z")),
                    tag.contains("received_at", Tag.TAG_LONG) ? tag.getLong("received_at") : -1,
                    tag.getString("receiver"),
                    tag.getString("received_dimension"),
                    new BlockPos(tag.getInt("received_x"), tag.getInt("received_y"), tag.getInt("received_z"))
            );
        }
    }
}
