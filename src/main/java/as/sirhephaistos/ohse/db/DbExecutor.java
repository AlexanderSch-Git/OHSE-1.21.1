package as.sirhephaistos.ohse.db;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Dedicated single-thread executor for all DB operations.
 * Prevents blocking the main server thread during SQL I/O.
 */
public final class DbExecutor {
    private DbExecutor() {
    }

    // One background thread for sequential DB writes
    public static final ExecutorService SINGLE = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "OHSE-DB");
        t.setDaemon(true);
        return t;
    });
}