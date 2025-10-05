package as.sirhephaistos.ohse;

import as.sirhephaistos.ohse.client.command.OHSEDebugClientCommands;
import as.sirhephaistos.ohse.client.render.DebugCubeRenderer;
import as.sirhephaistos.ohse.client.render.DebugLineRenderer;
import as.sirhephaistos.ohse.client.render.ZoneRenderer;
import as.sirhephaistos.ohse.zoneSrv.ZManager;
import net.fabricmc.api.ClientModInitializer;

public class OverhauledHordeSpawnEngineClient implements ClientModInitializer {
    /***
     * This method is called when the mod is initialized on the client side.
     * It is used to set up client-specific features such as rendering and client-side commands.
     */
    @Override
    public void onInitializeClient() {
        OHSEDebugClientCommands.register(); // Register client-side debug commands
        DebugCubeRenderer.register(); // Register the debug cube renderer
        DebugLineRenderer.register(); // Register the debug line renderer
        ZoneRenderer.register(); // Register the zone renderer
    }
}