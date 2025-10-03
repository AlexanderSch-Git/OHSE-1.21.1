package as.sirhephaistos.ohse.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public record ZoneWandPlaceARefPayload (
        BlockPos pos,
        int faceId, // Direction.getId()
        float red,
        float green,
        float blue,
        float alpha
)implements CustomPayload {

    public static final Id<ZoneWandPlaceARefPayload> ID =
            new Id<>(Identifier.of("ohse", "zone_wand_place_ref"));  // Identifier for th

    public static final PacketCodec<RegistryByteBuf, ZoneWandPlaceARefPayload> CODEC =
            PacketCodec.tuple(
                    BlockPos.PACKET_CODEC, ZoneWandPlaceARefPayload::pos,
                    PacketCodecs.VAR_INT, ZoneWandPlaceARefPayload::faceId,
                    PacketCodecs.FLOAT, ZoneWandPlaceARefPayload::red,
                    PacketCodecs.FLOAT, ZoneWandPlaceARefPayload::green,
                    PacketCodecs.FLOAT, ZoneWandPlaceARefPayload::blue,
                    PacketCodecs.FLOAT, ZoneWandPlaceARefPayload::alpha,
                    ZoneWandPlaceARefPayload::new
            );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
