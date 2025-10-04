package as.sirhephaistos.ohse;

import as.sirhephaistos.ohse.command.OHSECommands;
import as.sirhephaistos.ohse.config.OHSEConfig;
import as.sirhephaistos.ohse.network.ZoneWandPackets;
import as.sirhephaistos.ohse.registry.OHSEItems;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OverhauledHordeSpawnEngine implements ModInitializer {
	public static final String MOD_ID = "ohse";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    /**
     * This function is called when the mod is initialized.*/
    @Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
        LOGGER.info("[OHSE]Overhauled Horde Spawn Engine initialisation.");
        // load config
        OHSEConfig.load();
        LOGGER.info("[OHSE]Config loaded.");
        // Register items
        OHSEItems.register();
        LOGGER.info("[OHSE]Items registered.");
        // Register commands
        OHSECommands.register();
        LOGGER.info("[OHSE]Commands registered.");
        // Register Packets
        // 1) déclarer le codec du payload C2S
        ZoneWandPackets.registerC2SPackets();
        ZoneWandPackets.registerS2CPackets();
        LOGGER.info("[OHSE]Packets registered.");


        LOGGER.info("[OHSE]Overhauled Horde Spawn Engine initialized.");
	}
}