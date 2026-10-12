package ru.createadvancedpost;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.simibubi.create.content.logistics.packager.PackagerBlockEntity;
import net.minecraft.server.level.ServerPlayer;

public final class DeliverySenders {
    private static final Map<PackagerBlockEntity, UUID> SENDERS = new HashMap<>();

    private DeliverySenders() {
    }

    public static void set(PackagerBlockEntity packager, ServerPlayer player) {
        SENDERS.put(packager, player.getUUID());
    }

    public static UUID get(PackagerBlockEntity packager) {
        return SENDERS.get(packager);
    }

    public static void clear(PackagerBlockEntity packager) {
        SENDERS.remove(packager);
    }
}
