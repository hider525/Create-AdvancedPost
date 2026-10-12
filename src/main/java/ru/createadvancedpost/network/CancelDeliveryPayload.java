package ru.createadvancedpost.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import ru.createadvancedpost.CreateAdvancedPost;

public record CancelDeliveryPayload(BlockPos packagerPos) implements CustomPacketPayload {
    public static final Type<CancelDeliveryPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(CreateAdvancedPost.MOD_ID, "cancel_delivery"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CancelDeliveryPayload> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC,
                    CancelDeliveryPayload::packagerPos,
                    CancelDeliveryPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
