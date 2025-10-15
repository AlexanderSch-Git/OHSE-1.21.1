package as.sirhephaistos.ohse.db;

import org.jetbrains.annotations.Nullable;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Repository for OHSE mobs (minimal version).
 *
 * Inserts only the required columns: uuid, mod_id, display_name.
 * All other columns rely on DB defaults defined in schema.sql.
 *
 * Methods:
 * - insertSimpleBatch(): batch insert with optional skip-if-exists by mod_id
 * - existsByModId() / existsByModIdIn(): fast existence checks
 *
 * Assumes the 'mobs' table exists with sensible DEFAULT values. See schema.sql.
 */
public final class MobRepository {

    private MobRepository() {}

    // ---- SQL ----------------------------------------------------------------

    private static final String Q_EXISTS_BY_MODID =
            "SELECT mod_id FROM mobs WHERE mod_id IN (%s)";

    private static final String Q_EXISTS_ONE =
            "SELECT 1 FROM mobs WHERE mod_id = ? LIMIT 1";

    // Minimal insert (let DB defaults populate the rest)
    private static final String INS_MINIMAL =
            "INSERT INTO mobs (uuid, mod_id, display_name) VALUES (?,?,?)";

    // ---- DTO ----------------------------------------------------------------

    /**
     * Minimal mob payload: only the 3 fields we want to persist explicitly.
     */
    public static final class SimpleMob {
        public final UUID uuid;
        public final String modId;       // e.g., "minecraft:zombie"
        public final String displayName;

        public SimpleMob(UUID uuid, String modId, String displayName) {
            this.uuid = Objects.requireNonNull(uuid, "uuid");
            this.modId = Objects.requireNonNull(modId, "modId");
            this.displayName = Objects.requireNonNull(displayName, "displayName");
        }
    }

    // ---- Public API ----------------------------------------------------------

    public static boolean existsByModId(String modId) throws SQLException {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(Q_EXISTS_ONE)) {
            ps.setString(1, modId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Returns the subset of modIds that already exist in DB.
     * Queries in safe chunks to avoid very large IN() lists.
     */
    public static Set<String> existsByModIdIn(Collection<String> modIds) throws SQLException {
        if (modIds == null || modIds.isEmpty()) return Collections.emptySet();

        final int CHUNK = 500;
        Set<String> existing = new HashSet<>();
        List<String> list = new ArrayList<>(modIds);

        try (Connection c = Database.getConnection()) {
            for (int i = 0; i < list.size(); i += CHUNK) {
                List<String> slice = list.subList(i, Math.min(i + CHUNK, list.size()));
                String placeholders = slice.stream().map(s -> "?").collect(Collectors.joining(","));
                String sql = String.format(Q_EXISTS_BY_MODID, placeholders);

                try (PreparedStatement ps = c.prepareStatement(sql)) {
                    int idx = 1;
                    for (String s : slice) ps.setString(idx++, s);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            existing.add(rs.getString(1));
                        }
                    }
                }
            }
        }
        return existing;
    }

    /**
     * Batch insert of minimal mob records.
     * If skipIfExists = true, pre-checks by mod_id and drops existing from the batch.
     *
     * @return Result(insertedCount, skippedCount)
     */
    public static Result insertSimpleBatch(Collection<SimpleMob> mobs, boolean skipIfExists) throws SQLException {
        if (mobs == null || mobs.isEmpty()) return new Result(0, 0);

        List<SimpleMob> batch = new ArrayList<>(mobs);

        int skipped = 0;
        if (skipIfExists) {
            Set<String> modIds = batch.stream().map(m -> m.modId).collect(Collectors.toSet());
            Set<String> already = existsByModIdIn(modIds);
            if (!already.isEmpty()) {
                batch.removeIf(m -> already.contains(m.modId));
                skipped = mobs.size() - batch.size();
            }
        }

        if (batch.isEmpty()) return new Result(0, skipped);

        try (Connection c = Database.getConnection();
             PreparedStatement ins = c.prepareStatement(INS_MINIMAL)) {

            boolean prevAuto = c.getAutoCommit();
            c.setAutoCommit(false);

            try {
                for (SimpleMob m : batch) {
                    ins.setString(1, m.uuid.toString());
                    ins.setString(2, m.modId);
                    ins.setString(3, m.displayName);
                    ins.addBatch();
                }

                ins.executeBatch();
                c.commit();
                return new Result(batch.size(), skipped);

            } catch (SQLException e) {
                try { c.rollback(); } catch (SQLException ignore) {}
                throw e;
            } finally {
                try { c.setAutoCommit(prevAuto); } catch (SQLException ignore) {}
            }
        }
    }

    // ---- Helpers -------------------------------------------------------------

    public record Result(int inserted, int skipped) {}
}
