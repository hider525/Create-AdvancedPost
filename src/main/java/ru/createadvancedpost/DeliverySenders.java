package ru.createadvancedpost;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.simibubi.create.content.logistics.packager.PackagerBlockEntity;
import net.minecraft.server.level.ServerPlayer;

public final class DeliverySenders {
    public record Delivery(UUID senderId, UUID recipientId) {
    }

    private static final Map<PackagerBlockEntity, Delivery> DELIVERIES = new HashMap<>();

    private DeliverySenders() {
    }

    public static void set(PackagerBlockEntity packager, UUID senderId, UUID recipientId) {
        DELIVERIES.put(packager, new Delivery(senderId, recipientId));
    }

    public static Delivery get(PackagerBlockEntity packager) {
        return DELIVERIES.get(packager);
    }

    public static void clear(PackagerBlockEntity packager) {
        DELIVERIES.remove(packager);
    }
}
