package as.sirhephaistos.ohse.zoneSrv;

import as.sirhephaistos.ohse.network.ZoneRecuperationClientResponsePayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.Context;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public final class ZManager {
    public static final ZManager INSTANCE = new ZManager();
    private ZManager() {}

    /** Simulates the creation of a zone on the server using the provided payload.
     * @param payload The payload containing zone data.
     * @param ctx     The server play networking context.
     */
    public static void createZone(@NotNull ZoneRecuperationClientResponsePayload payload,
                                  @NotNull Context ctx) {
        System.out.println("Creating zone on server with " + payload.positions().size() + " positions.");
    }
}
