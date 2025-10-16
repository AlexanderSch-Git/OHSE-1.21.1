package as.sirhephaistos.ohse.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Properties;

public final class Database {
    private static HikariDataSource ds;

    private Database() {}

    public static synchronized void init(Path configDir) throws Exception {
        if (ds != null) return;

        // 1) S'assurer que le dossier/config existent (créer avec défauts si absent)
        Files.createDirectories(configDir);
        Path propPath = configDir.resolve("ohse.properties");

        Properties p;
        if (Files.notExists(propPath)) {
            // Crée un fichier neuf avec des valeurs par défaut
            Properties def = defaultProps();
            try (var out = Files.newOutputStream(propPath, StandardOpenOption.CREATE_NEW)) {
                def.store(out, "OHSE database configuration — created automatically on first run");
            }
            p = def; // utilise les défauts pour ce tout premier boot
        } else {
            p = new Properties();
            try (var in = Files.newInputStream(propPath)) {
                p.load(in);
            }
            // Remplir les clés manquantes avec des valeurs par défaut
            Properties def = defaultProps();
            def.forEach(p::putIfAbsent);
        }

        // 2) Overrides optionnels UNIQUEMENT si la clé manque (ne pas écraser le fichier)
        applyOverridesIfMissing(p);

        // 3) Validation des clés requises
        mustHave(p, "jdbcUrl");
        mustHave(p, "dbUser");
        mustHave(p, "dbPass");

        // 4) Construire HikariCP
        HikariConfig cfg = new HikariConfig();
        cfg.setJdbcUrl(p.getProperty("jdbcUrl"));
        cfg.setUsername(p.getProperty("dbUser"));
        cfg.setPassword(p.getProperty("dbPass"));

        cfg.setMaximumPoolSize(Integer.parseInt(p.getProperty("poolMaxSize", "4")));
        cfg.setMinimumIdle(Integer.parseInt(p.getProperty("poolMinIdle", "1")));
        cfg.setConnectionTimeout(Long.parseLong(p.getProperty("connTimeoutMs", "10000")));
        cfg.setIdleTimeout(Long.parseLong(p.getProperty("idleTimeoutMs", "600000")));
        cfg.setMaxLifetime(Long.parseLong(p.getProperty("maxLifetimeMs", "1800000")));

        long leakMs = Long.parseLong(p.getProperty("leakDetectionMs", "0"));
        if (leakMs > 0) cfg.setLeakDetectionThreshold(leakMs);

        // Hints MySQL recommandés
        cfg.addDataSourceProperty("cachePrepStmts", "true");
        cfg.addDataSourceProperty("prepStmtCacheSize", "250");
        cfg.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        cfg.addDataSourceProperty("useServerPrepStmts", "true");

        ds = new HikariDataSource(cfg);
    }

    public static Connection getConnection() throws SQLException {
        if (ds == null) throw new SQLException("Database pool not initialized");
        return ds.getConnection();
    }

    @SuppressWarnings("unused")
    public static synchronized void shutdown() {
        if (ds != null) {
            ds.close();
            ds = null;
        }
    }

    // ---------- helpers ----------

    private static Properties defaultProps() {
        Properties p = new Properties();
        // Mets ici tes défauts de dev
        p.setProperty("jdbcUrl", "jdbc:mysql://localhost:3306/ohse"
                + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&rewriteBatchedStatements=true");
        p.setProperty("dbUser", "root");
        p.setProperty("dbPass", "1234"); // ← change en prod

        // Pool tuning (petits défauts sûrs)
        p.setProperty("poolMaxSize", "4");
        p.setProperty("poolMinIdle", "1");
        p.setProperty("connTimeoutMs", "10000");
        p.setProperty("idleTimeoutMs", "600000");
        p.setProperty("maxLifetimeMs", "1800000");
        p.setProperty("leakDetectionMs", "0");
        return p;
    }

    // N'applique les overrides ENV / -D que si la clé est absente dans le fichier
    private static void applyOverridesIfMissing(Properties p) {
        setIfPresentEnvIfMissing(p, "jdbcUrl", "OHSE_JDBC_URL");
        setIfPresentEnvIfMissing(p, "dbUser", "OHSE_DB_USER");
        setIfPresentEnvIfMissing(p, "dbPass", "OHSE_DB_PASS");

        setIfPresentSysPropIfMissing(p, "jdbcUrl", "ohse.jdbcUrl");
        setIfPresentSysPropIfMissing(p, "dbUser",  "ohse.dbUser");
        setIfPresentSysPropIfMissing(p, "dbPass",  "ohse.dbPass");
    }

    private static void setIfPresentEnvIfMissing(Properties p, String key, String env) {
        if (p.getProperty(key) == null || p.getProperty(key).isBlank()) {
            String v = System.getenv(env);
            if (v != null && !v.isBlank()) p.setProperty(key, v);
        }
    }

    private static void setIfPresentSysPropIfMissing(Properties p, String key, String sysProp) {
        if (p.getProperty(key) == null || p.getProperty(key).isBlank()) {
            String v = System.getProperty(sysProp);
            if (v != null && !v.isBlank()) p.setProperty(key, v);
        }
    }

    private static void mustHave(Properties p, String k) {
        if (Objects.requireNonNullElse(p.getProperty(k), "").isBlank()) {
            throw new IllegalStateException("Missing required DB property: " + k);
        }
    }
}
