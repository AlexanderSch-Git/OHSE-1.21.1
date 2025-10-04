package as.sirhephaistos.ohse.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ZoneWandClickPayload(
        int clickType,
        int faceId,
        double hitX,
        double hitY,
        double hitZ
) implements CustomPayload {

    public static final Id<ZoneWandClickPayload> ID =
            new Id<>(Identifier.of("ohse", "zone_wand_click"));  // Identifier for the packet type

    /***
     * Codec for serializing and deserializing the packet data.
     * Uses a tuple codec to handle multiple fields.
     *  Fields:
     *  - clickType: Variable-length integer (0=LEFT, 1=RIGHT, 2=MIDDLE)
     *  - pos: Optional BlockPos (the position of the block clicked, if any)
     */
    public static final PacketCodec<RegistryByteBuf, ZoneWandClickPayload> CODEC =
            PacketCodec.tuple(
                    PacketCodecs.VAR_INT, ZoneWandClickPayload::clickType,
                    PacketCodecs.VAR_INT, ZoneWandClickPayload::faceId,
                    PacketCodecs.DOUBLE, ZoneWandClickPayload::hitX,
                    PacketCodecs.DOUBLE, ZoneWandClickPayload::hitY,
                    PacketCodecs.DOUBLE, ZoneWandClickPayload::hitZ,
                    ZoneWandClickPayload::new
            );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
