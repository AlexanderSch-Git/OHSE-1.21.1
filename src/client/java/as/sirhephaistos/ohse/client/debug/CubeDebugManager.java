package as.sirhephaistos.ohse.client.debug;

import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public final class CubeDebugManager {
    private static final List<BlockPos> POSITIONS = new ArrayList<>();

    private CubeDebugManager() {}

    public static void clear() { POSITIONS.clear(); }

    @SuppressWarnings("unused")
    public static boolean isEmpty() { return POSITIONS.isEmpty(); }

    public static void addUnitCubeAt(BlockPos pos) {
        // Store WORLD coordinates only (no camera math here!)
        POSITIONS.add(pos.toImmutable());
    }

    public static List<BlockPos> getPositions() {
        return Collections.unmodifiableList(POSITIONS);
    }
}


