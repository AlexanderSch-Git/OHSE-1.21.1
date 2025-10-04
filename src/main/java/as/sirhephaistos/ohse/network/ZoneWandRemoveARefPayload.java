package as.sirhephaistos.ohse.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ZoneWandRemoveARefPayload(
        double hitX,
        double hitY,
        double hitZ,
        int faceId
)implements CustomPayload {

    public static final Id<ZoneWandRemoveARefPayload> ID =
            new Id<>(Identifier.of("ohse", "zone_wand_remove_ref"));

    public static final PacketCodec<RegistryByteBuf, ZoneWandRemoveARefPayload> CODEC =
            PacketCodec.tuple(
                    PacketCodecs.DOUBLE, ZoneWandRemoveARefPayload::hitX,
                    PacketCodecs.DOUBLE, ZoneWandRemoveARefPayload::hitY,
                    PacketCodecs.DOUBLE, ZoneWandRemoveARefPayload::hitZ,
                    PacketCodecs.VAR_INT, ZoneWandRemoveARefPayload::faceId,
                    ZoneWandRemoveARefPayload::new
            );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}