package as.sirhephaistos.ohse.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ZoneWandPlaceARefPayload (
        double hitX,
        double hitY,
        double hitZ,
        int faceId // Direction.getId()
)implements CustomPayload {

    public static final Id<ZoneWandPlaceARefPayload> ID =
            new Id<>(Identifier.of("ohse", "zone_wand_place_ref"));  // Identifier for th

    public static final PacketCodec<RegistryByteBuf, ZoneWandPlaceARefPayload> CODEC =
            PacketCodec.tuple(
                    PacketCodecs.DOUBLE, ZoneWandPlaceARefPayload::hitX,
                    PacketCodecs.DOUBLE, ZoneWandPlaceARefPayload::hitY,
                    PacketCodecs.DOUBLE, ZoneWandPlaceARefPayload::hitZ,
                    PacketCodecs.VAR_INT, ZoneWandPlaceARefPayload::faceId,
                    ZoneWandPlaceARefPayload::new
            );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
