package as.sirhephaistos.ohse.zoneSrv;

import as.sirhephaistos.ohse.network.ZoneRecuperationClientResponsePayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.Context;
public final class ZManager {
    public static final ZManager INSTANCE = new ZManager();
    private ZManager() {}

    public static void createZone(ZoneRecuperationClientResponsePayload payload, Context ctx) {
        System.out.println("Creating zone on server with " + payload.positions().size() + " positions.");
    }
}
