package ru.createadvancedpost;

import net.neoforged.fml.common.Mod;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;

@Mod(CreateAdvancedPost.MOD_ID)
public final class CreateAdvancedPost {
    public static final String MOD_ID = "create_advancedpost";

    public CreateAdvancedPost(IEventBus modBus) {
        modBus.addListener(NetworkHandler::registerPayloads);
        NeoForge.EVENT_BUS.register(DeliveryStationEvents.class);
        NeoForge.EVENT_BUS.register(ParcelTrackingEvents.class);
    }
}
