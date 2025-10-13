package as.sirhephaistos.ohse.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public final class Database {
    private static HikariDataSource ds;

    private Database() {}

    public static synchronized void init(Path configDir) throws Exception {
        if (ds != null) return;

        // charge config
        Properties p = new Properties();
        try (FileInputStream in = new FileInputStream(configDir.resolve("ohse.properties").toFile())) {
            p.load(in);
        }

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

        // optimisations MySQL recommandées
        cfg.addDataSourceProperty("cachePrepStmts", "true");
        cfg.addDataSourceProperty("prepStmtCacheSize", "250");
        cfg.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        cfg.addDataSourceProperty("useServerPrepStmts", "true");

        ds = new HikariDataSource(cfg);
    }

    public static Connection getConnection() throws SQLException {
        if (ds == null) throw new SQLException("Database pool not initialized");
        return ds.getConnection(); // à fermer dans un try-with-resources
    }

    public static synchronized void shutdown() {
        if (ds != null) {
            ds.close();
            ds = null;
        }
    }
}
