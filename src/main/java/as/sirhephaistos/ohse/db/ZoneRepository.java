package as.sirhephaistos.ohse.db;

import net.minecraft.util.math.ChunkPos;
import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.Set;
import java.util.UUID;

/**
 * Repository for persisting and managing OHSE zones.
 * - Uses transactions to insert the zone header + all its chunks atomically.
 * - Exposes a schema bootstrap (ensureSchema) you can call at server start.
 * - Stores Y bounds as DOUBLE (not INT).
 */
public final class ZoneRepository {
    private static final String INS_ZONE =
            "INSERT INTO zones (uuid, name, creator, world, yMin, yMax) VALUES (?,?,?,?,?,?)";

    private static final String INS_CHUNK =
            "INSERT INTO zone_chunks (zone_uuid, world, chunk_x, chunk_z) VALUES (?,?,?,?)";

    private static final String DEL_ZONE_BY_UUID =
            "DELETE FROM zones WHERE uuid = ?";

    private static final String EXISTS_ZONE_BY_UUID =
            "SELECT 1 FROM zones WHERE uuid = ?";

    private ZoneRepository() {
    }

    public static void ensureSchema() throws SQLException, IOException {
        try (Connection c = Database.getConnection();
             Statement st = c.createStatement()) {

            String ddl = loadResource("/sql/schema.sql");
            // optionnel : normalisation & retrait de commentaires simples
            ddl = ddl.replaceAll("(?m)^\\s*--.*$", "");   // lignes commençant par --
            for (String sql : ddl.split(";")) {
                String trimmed = sql.trim();
                if (!trimmed.isEmpty()) {
                    try {
                        st.execute(trimmed);
                    } catch (SQLException e) {
                        // log utile pour debug
                        System.err.println("[OHSE] DDL failed on: " + trimmed);
                        throw e;
                    }
                }
            }
        }
    }

    /**
     * Loads a text resource (like an SQL file) from the classpath.
     */
    private static String loadResource(String path) throws IOException {
        try (InputStream in = ZoneRepository.class.getResourceAsStream(path)) {
            if (in == null)
                throw new FileNotFoundException("Resource not found: " + path);
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    /**
     * Inserts a zone header and all its chunks in a single transaction.
     */
    public static void saveZoneWithChunks(
            UUID uuid,
            String name,
            String creator,
            @NotNull String world,
            double ymin,
            double ymax,
            Set<ChunkPos> chunks
    ) throws SQLException {
        if (chunks == null || chunks.isEmpty()) throw new IllegalArgumentException("chunks must not be null or empty");
        try (Connection c = Database.getConnection();
             PreparedStatement z  = c.prepareStatement(INS_ZONE);
             PreparedStatement cz = c.prepareStatement(INS_CHUNK)) {

            boolean prevAuto = c.getAutoCommit();
            c.setAutoCommit(false);
            try {
                z.setString(1, uuid.toString());
                z.setString(2, name);
                z.setString(3, creator);
                z.setString(4, world);
                z.setDouble(5, ymin);
                z.setDouble(6, ymax);
                z.executeUpdate();

                for (ChunkPos cp : chunks) {
                    cz.setString(1, uuid.toString());
                    cz.setString(2, world);
                    cz.setInt(3, cp.x);
                    cz.setInt(4, cp.z);
                    cz.addBatch();
                }
                cz.executeBatch();

                c.commit();
            } catch (SQLException e) {
                try { c.rollback(); } catch (SQLException ignore) {}
                throw e;
            } finally {
                try { c.setAutoCommit(prevAuto); } catch (SQLException ignore) {}
            }
        }
    }

    public static boolean deleteZone(UUID uuid) throws SQLException {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(DEL_ZONE_BY_UUID)) {
            ps.setString(1, uuid.toString());
            int rows = ps.executeUpdate();
            return rows > 0;
        }
    }

    public static boolean exists(UUID uuid) throws SQLException {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(EXISTS_ZONE_BY_UUID)) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
}
