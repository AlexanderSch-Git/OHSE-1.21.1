package as.sirhephaistos.ohse.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.Optional;
//this payload C2S packet is sent when the player clicks with the zone wand
// it contains the click type (left, right, middle) and the block position if any
public record ZoneWandClickPayload(
        int clickType,                 // 0=LEFT, 1=RIGHT, 2=MIDDLE
        Optional<BlockPos> pos
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
                    PacketCodecs.optional(BlockPos.PACKET_CODEC), ZoneWandClickPayload::pos,
                    ZoneWandClickPayload::new
            );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
