package ru.createadvancedpost.client;

import net.minecraft.client.Minecraft;
import ru.createadvancedpost.network.OpenDeliveryScreenPayload;

public final class ClientPayloadHandler {
    private ClientPayloadHandler() {
    }

    public static void openDeliveryScreen(OpenDeliveryScreenPayload payload) {
        Minecraft.getInstance().setScreen(new DeliveryAddressScreen(payload.packagerPos(), payload.addresses()));
    }
}
