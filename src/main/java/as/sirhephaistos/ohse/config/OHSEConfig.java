package as.sirhephaistos.ohse.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * OHSEConfig
 * Utility class that manages the mod configuration stored as JSON on disk.
 * - Provides lazy loading and saving of a single {@link OHSEConfigData} instance.
 * - Uses Gson for serialization with pretty printing to produce human-readable JSON.
 * - Thread-safety:
 *   - {@code INSTANCE} is declared {@code volatile} to ensure safe publication.
 *   - {@link #load()} and {@link #save()} are {@code synchronized} to serialize disk access.
 * Usage:
 * - Call {@link #get()} to obtain the current config instance (will load it if necessary).
 * - Call {@link #save()} to persist the current state to disk.
 * - Call {@link #reload()} to force reloading from disk.
 */
public final class OHSEConfig {

    /**
     * Gson instance configured to pretty-print JSON.
     */
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    /**
     * Path to the configuration file.
     * Resolved using Fabric's config directory so it follows the platform convention.
     */
    private static final Path PATH = FabricLoader.getInstance()
            .getConfigDir().resolve("ohse.json");

    /**
     * Singleton instance of the configuration data.
     * Declared {@code volatile} so that changes made in one thread become visible to others
     * without requiring additional synchronization on reads in {@link #get()}.
     */
    private static volatile OHSEConfigData INSTANCE;

    /**
     * Private constructor to prevent instantiation
     */
    private OHSEConfig() {
    }

    /**
     * Returns the current configuration data instance.
     * If the instance is not yet loaded, {@link #load()} is invoked to initialize it from disk.
     * This method performs a cheap null-check and defers actual loading to {@link #load()},
     * which is synchronized to ensure only one thread performs the disk IO at a time.
     * @return the active OHSEConfigData instance (never null).
     */
    public static OHSEConfigData get() {
        // If not loaded yet, load() will initialize INSTANCE; load() is synchronized.
        if (INSTANCE == null) load();
        return INSTANCE;
    }

    /**
     * Loads configuration from the JSON file on disk.
     * Behavior:
     * - If the config file does not exist, a default OHSEConfigData is created and saved.
     * - If the file exists, it is deserialized using Gson. If deserialization returns null
     *   (malformed/empty file), a default instance is used instead.
     * - Any IOException during read/write results in falling back to a default instance
     *   and printing the stack trace for debugging.
     * The method is synchronized to prevent concurrent load/save operations from colliding.
     */
    public static synchronized void load() {
        try {
            // If the file does not exist, initialize a default config and persist it.
            if (Files.notExists(PATH)) {
                INSTANCE = new OHSEConfigData();
                save();
                return;
            }
            try (Reader r = Files.newBufferedReader(PATH)) {
                OHSEConfigData data = GSON.fromJson(r, OHSEConfigData.class);
                if (data == null) data = new OHSEConfigData();
                validateDataOrCrash(data);
                INSTANCE = data;
            }
        } catch (Exception e) {
            INSTANCE = new OHSEConfigData();
            throw new IllegalStateException("Failed to load config, using defaults\n"+e.getMessage(), e);
        }
    }

    /**
     * Validates the loaded configuration data.
     * Throws IllegalStateException if any parameter is out of acceptable bounds.
     * This ensures that the mod does not operate with invalid config values.
     * @param data the OHSEConfigData instance to validate
     */
    private static void validateDataOrCrash(OHSEConfigData data) {
        if (data.maxRaycastDistance <= 0) {
            throw new IllegalStateException("Invalid config: maxRaycastDistance must be positive");
        }
        if (data.maxRaycastDistance > 1024.0) {
            throw new IllegalStateException("Invalid config: maxRaycastDistance too large (max 1024.0)");
        }
        if (data.scrollStep <= 0) {
            throw new IllegalStateException("Invalid config: scrollStep must be positive");
        }
        if (data.ctrlMultiplier <= 0) {
            throw new IllegalStateException("Invalid config: ctrlMultiplier must be positive");
        }
        if (data.shiftMultiplier <= 0) {
            throw new IllegalStateException("Invalid config: shiftMultiplier must be positive");
        }
    }

    /**
     * Saves the current configuration to disk in JSON format.
     * Steps:
     * - Ensures the parent directory for the config file exists by calling Files.createDirectories.
     * - Uses try-with-resources to open a Writer and writes the JSON representation.
     * - If {@code INSTANCE} is null, a new default OHSEConfigData is serialized (prevents writing null).
     * - Any IOException is caught and its stack trace printed (no exception is propagated).
     * The method is synchronized to avoid concurrent writes and races with {@link #load()}.
     */
    public static synchronized void save() {
        try {
            Files.createDirectories(PATH.getParent());
            try (Writer w = Files.newBufferedWriter(PATH)) {
                GSON.toJson(INSTANCE != null ? INSTANCE : new OHSEConfigData(), w);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to save config\n"+e.getMessage(), e);
        }
    }

    /**
     * Reloads configuration from disk.
     * Convenience wrapper around {@link #load()} to make intent explicit at call sites.
     */
    public static void reload() {
        load();
    }
}
