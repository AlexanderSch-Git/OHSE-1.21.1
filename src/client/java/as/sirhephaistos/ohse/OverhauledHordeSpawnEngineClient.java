package as.sirhephaistos.ohse;

import net.fabricmc.api.ClientModInitializer;

public class OverhauledHordeSpawnEngineClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
        // print a log to confirm that the client has been initialized
        System.out.println("OHSE Client Initialized");
	}
}