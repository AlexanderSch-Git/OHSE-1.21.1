package as.sirhephaistos.ohse.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

/** (x,z) pair for network transport */
public record XZ(double x, double z) {
    public static final PacketCodec<RegistryByteBuf, XZ> CODEC =
            PacketCodec.tuple(
                    PacketCodecs.DOUBLE, XZ::x,
                    PacketCodecs.DOUBLE, XZ::z,
                    XZ::new
            );
}