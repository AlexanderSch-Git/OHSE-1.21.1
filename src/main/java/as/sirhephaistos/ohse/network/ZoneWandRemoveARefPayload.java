package as.sirhephaistos.ohse.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public record ZoneWandRemoveARefPayload(
        BlockPos pos
)implements CustomPayload {

    public static final Id<ZoneWandRemoveARefPayload> ID =
            new Id<>(Identifier.of("ohse", "zone_wand_remove_ref"));  // Identifier for th

    public static final PacketCodec<RegistryByteBuf, ZoneWandRemoveARefPayload> CODEC =
            PacketCodec.tuple(
                    BlockPos.PACKET_CODEC, ZoneWandRemoveARefPayload::pos,
                    ZoneWandRemoveARefPayload::new
            );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
