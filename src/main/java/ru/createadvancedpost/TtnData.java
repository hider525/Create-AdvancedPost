package ru.createadvancedpost;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

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

    private TtnData() {
    }

    private static TtnData load(CompoundTag tag, HolderLookup.Provider registries) {
        TtnData data = new TtnData();
        ListTag codes = tag.getList("issued_codes", Tag.TAG_STRING);
        for (int i = 0; i < codes.size(); i++) {
            data.issuedCodes.add(codes.getString(i));
        }
        return data;
    }

    public static String issue(ServerLevel level) {
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
        data.setDirty();
        return code;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag codes = new ListTag();
        for (String code : issuedCodes) {
            codes.add(StringTag.valueOf(code));
        }
        tag.put("issued_codes", codes);
        return tag;
    }
}
