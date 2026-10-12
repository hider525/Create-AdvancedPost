package ru.createadvancedpost;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import ru.createadvancedpost.network.CancelDeliveryPayload;
import ru.createadvancedpost.network.OpenDeliveryScreenPayload;
import ru.createadvancedpost.network.SubmitDeliveryPayload;

public final class NetworkHandler {
    private NetworkHandler() {
    }

    public static void registerPayloads(
            net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(
                OpenDeliveryScreenPayload.TYPE,
                OpenDeliveryScreenPayload.STREAM_CODEC,
                (payload, context) -> {
                    if (FMLEnvironment.dist == Dist.CLIENT) {
                        ru.createadvancedpost.client.ClientPayloadHandler.openDeliveryScreen(payload);
                    }
                }
        );
        registrar.playToServer(
                SubmitDeliveryPayload.TYPE,
                SubmitDeliveryPayload.STREAM_CODEC,
                NetworkHandler::submitDelivery
        );
        registrar.playToServer(
                CancelDeliveryPayload.TYPE,
                CancelDeliveryPayload.STREAM_CODEC,
                NetworkHandler::cancelDelivery
        );
    }

    private static void submitDelivery(SubmitDeliveryPayload payload, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer player) {
            PendingAddressSelections.submit(player, payload.packagerPos(), payload.address());
        }
    }

    private static void cancelDelivery(CancelDeliveryPayload payload, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer player) {
            PendingAddressSelections.cancel(player, payload.packagerPos());
        }
    }
}
