package as.sirhephaistos.ohse.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ZoneWandInitRecuperationPayload(
        String zoneName
)implements CustomPayload {
    public static final Id<ZoneWandInitRecuperationPayload> ID =
            new Id<>(Identifier.of("ohse", "zone_recuperation_inquiry"));

    // Un payload vide = pas de données à lire/écrire
    public static final PacketCodec<RegistryByteBuf, ZoneWandInitRecuperationPayload> CODEC =
            PacketCodec.tuple(
                    PacketCodecs.STRING, ZoneWandInitRecuperationPayload::zoneName,
                    ZoneWandInitRecuperationPayload::new
            );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
