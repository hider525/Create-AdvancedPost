package ru.createadvancedpost.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import ru.createadvancedpost.CreateAdvancedPost;

public record SubmitDeliveryPayload(BlockPos packagerPos, String address, String recipient)
        implements CustomPacketPayload {
    public static final Type<SubmitDeliveryPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(CreateAdvancedPost.MOD_ID, "submit_delivery"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SubmitDeliveryPayload> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC,
                    SubmitDeliveryPayload::packagerPos,
                    ByteBufCodecs.stringUtf8(128),
                    SubmitDeliveryPayload::address,
                    ByteBufCodecs.stringUtf8(16),
                    SubmitDeliveryPayload::recipient,
                    SubmitDeliveryPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
