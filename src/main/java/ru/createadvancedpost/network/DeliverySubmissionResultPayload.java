package ru.createadvancedpost.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import ru.createadvancedpost.CreateAdvancedPost;

public record DeliverySubmissionResultPayload(boolean accepted) implements CustomPacketPayload {
    public static final Type<DeliverySubmissionResultPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(
                    CreateAdvancedPost.MOD_ID,
                    "delivery_submission_result"
            ));
    public static final StreamCodec<RegistryFriendlyByteBuf, DeliverySubmissionResultPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL,
                    DeliverySubmissionResultPayload::accepted,
                    DeliverySubmissionResultPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}24
