package as.sirhephaistos.ohse;

import as.sirhephaistos.ohse.client.command.OHSEDebugClientCommands;
import as.sirhephaistos.ohse.client.render.DebugCubeRenderer;
import as.sirhephaistos.ohse.client.render.DebugLineRenderer;
import as.sirhephaistos.ohse.client.render.ZoneRenderer;
import net.fabricmc.api.ClientModInitializer;

public class OverhauledHordeSpawnEngineClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
        OHSEDebugClientCommands.register();
        DebugCubeRenderer.register();
        DebugLineRenderer.register();
        ZoneRenderer.register();
        // print a log to confirm that the client has been initialized
        //System.out.println("OHSE Client Initialized");
	}
}