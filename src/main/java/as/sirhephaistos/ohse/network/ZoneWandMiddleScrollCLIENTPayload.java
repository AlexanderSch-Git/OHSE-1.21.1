package as.sirhephaistos.ohse.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ZoneWandMiddleScrollCLIENTPayload(
        int direction // +1 or -1
)implements CustomPayload {

    public static final Id<ZoneWandMiddleScrollCLIENTPayload> ID =
            new Id<>(Identifier.of("ohse", "zone_wand_scroll_ref"));  // Identifier for th

    public static final PacketCodec<RegistryByteBuf, ZoneWandMiddleScrollCLIENTPayload> CODEC =
            PacketCodec.tuple(
                    PacketCodecs.VAR_INT, ZoneWandMiddleScrollCLIENTPayload::direction,
                    ZoneWandMiddleScrollCLIENTPayload::new
            );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
