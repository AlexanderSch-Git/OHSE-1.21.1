package as.sirhephaistos.ohse.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ZoneWandMiddleClickPayload(
        double hitX,
        double hitY,
        double hitZ
)implements CustomPayload {

    public static final Id<ZoneWandMiddleClickPayload> ID =
            new Id<>(Identifier.of("ohse", "zone_wand_middle_click"));  // Identifier for th

    public static final PacketCodec<RegistryByteBuf, ZoneWandMiddleClickPayload> CODEC =
            PacketCodec.tuple(
                    PacketCodecs.DOUBLE, ZoneWandMiddleClickPayload::hitX,
                    PacketCodecs.DOUBLE, ZoneWandMiddleClickPayload::hitY,
                    PacketCodecs.DOUBLE, ZoneWandMiddleClickPayload::hitZ,
                    ZoneWandMiddleClickPayload::new
            );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
