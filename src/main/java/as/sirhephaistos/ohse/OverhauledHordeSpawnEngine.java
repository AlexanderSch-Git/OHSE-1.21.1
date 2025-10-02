package as.sirhephaistos.ohse;

import as.sirhephaistos.ohse.command.OHSECommands;
import as.sirhephaistos.ohse.registry.OHSEItems;
import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OverhauledHordeSpawnEngine implements ModInitializer {
	public static final String MOD_ID = "ohse";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
        LOGGER.info("[OHSE]Overhauled Horde Spawn Engine initialisation.");
        // Register items
        OHSEItems.register();
        LOGGER.info("[OHSE]Items registered.");
        // Register commands
        OHSECommands.register();
        LOGGER.info("[OHSE]Commands registered.");
        LOGGER.info("[OHSE]Overhauled Horde Spawn Engine initialized.");
	}
}