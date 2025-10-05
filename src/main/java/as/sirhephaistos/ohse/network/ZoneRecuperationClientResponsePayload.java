package as.sirhephaistos.ohse.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.List;

public record ZoneRecuperationClientResponsePayload(
        String zoneName,
        List<XZ> positions,
        double yMin,
        double yMax
) implements CustomPayload {

    public static final Id<ZoneRecuperationClientResponsePayload> ID =
            new Id<>(Identifier.of("ohse", "zone_recuperation_client_response"));

    public static final PacketCodec<RegistryByteBuf, ZoneRecuperationClientResponsePayload> CODEC =
            PacketCodec.tuple(
                    PacketCodecs.STRING, ZoneRecuperationClientResponsePayload::zoneName,
                    XZ.CODEC.collect(PacketCodecs.toList()), ZoneRecuperationClientResponsePayload::positions,
                    PacketCodecs.DOUBLE, ZoneRecuperationClientResponsePayload::yMin,
                    PacketCodecs.DOUBLE, ZoneRecuperationClientResponsePayload::yMax,
                    ZoneRecuperationClientResponsePayload::new
            );

    @Override
    public Id<? extends CustomPayload> getId() { return ID; }
}
