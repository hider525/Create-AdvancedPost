package ru.createadvancedpost.network;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import ru.createadvancedpost.CreateAdvancedPost;

public record OpenDeliveryScreenPayload(BlockPos packagerPos, List<String> addresses, List<String> recipients)
        implements CustomPacketPayload {
    public static final Type<OpenDeliveryScreenPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(CreateAdvancedPost.MOD_ID, "open_delivery_screen"));
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenDeliveryScreenPayload> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC,
                    OpenDeliveryScreenPayload::packagerPos,
                    ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list(256)),
                    OpenDeliveryScreenPayload::addresses,
                    ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list(256)),
                    OpenDeliveryScreenPayload::recipients,
                    OpenDeliveryScreenPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
