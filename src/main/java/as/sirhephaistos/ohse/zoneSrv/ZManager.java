package as.sirhephaistos.ohse.zoneSrv;

import as.sirhephaistos.ohse.network.ZoneRecuperationClientResponsePayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.Context;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public final class ZManager {
    private static final ZManager INSTANCE = new ZManager();

    private ZManager() { }

    public static ZManager getInstance() {
        return INSTANCE;
    }

    // ---- méthodes d'instance (pas static) ----
    public void createZone(ZoneRecuperationClientResponsePayload payload, Context ctx) {
        System.out.println("Creating zone on server with " + payload.positions().size() + " positions.");
    }
}

