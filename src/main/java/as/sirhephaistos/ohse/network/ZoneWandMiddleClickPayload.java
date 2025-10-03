package as.sirhephaistos.ohse.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public record ZoneWandMiddleClickPayload(
        BlockPos pos
)implements CustomPayload {

    public static final Id<ZoneWandMiddleClickPayload> ID =
            new Id<>(Identifier.of("ohse", "zone_wand_remove_ref"));  // Identifier for th

    public static final PacketCodec<RegistryByteBuf, ZoneWandMiddleClickPayload> CODEC =
            PacketCodec.tuple(
                    BlockPos.PACKET_CODEC, ZoneWandMiddleClickPayload::pos,
                    ZoneWandMiddleClickPayload::new
            );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
