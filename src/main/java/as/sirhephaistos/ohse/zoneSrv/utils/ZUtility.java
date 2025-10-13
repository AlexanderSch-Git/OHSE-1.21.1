package as.sirhephaistos.ohse.zoneSrv.utils;

import as.sirhephaistos.ohse.network.XZ;
import as.sirhephaistos.ohse.zoneSrv.ZManager;
import net.minecraft.util.math.ChunkPos;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class ZUtility {
    private ZUtility() {
    }

    /**
     * Get all chunks that are inside a polygon defined by its vertices
     *
     * @param vertices  the vertices of the polygon
     * @param aabbCache the bounding box of the polygon, defined by [minX, maxX, minZ, maxZ]
     * @return a set of ChunkPos representing the chunks that are inside the polygon
     */
    public static Set<ChunkPos>  getChunksInPolygon(List<XZ> vertices, double[] aabbCache) {
        // Math.floorDiv is used to correctly handle negative coordinates.
        int minChunkX = Math.floorDiv((int) Math.floor(aabbCache[0]), 16);
        int maxChunkX = Math.floorDiv((int) Math.floor(aabbCache[1]), 16);
        int minChunkZ = Math.floorDiv((int) Math.floor(aabbCache[2]), 16);
        int maxChunkZ = Math.floorDiv((int) Math.floor(aabbCache[3]), 16);
        Set<ChunkPos> result = new HashSet<>();
        // Loop through all chunk indices within the bounding box
        for (int cx = minChunkX; cx <= maxChunkX; cx++) {
            for (int cz = minChunkZ; cz <= maxChunkZ; cz++) {
                // Calculate the chunk center position in block coordinates
                double centerX = (cx << 4) + 8; // (cx * 16) + 8
                double centerZ = (cz << 4) + 8; // (cz * 16) + 8
                // Perform the point-in-polygon test using the chunk center
                if (isInPolygonRC(new XZ(centerX, centerZ), vertices, aabbCache)) {
                    result.add(new ChunkPos(cx, cz));
                }
            }
        }

        return result;
    }

    public static boolean isInPolygonRC(XZ point, List<XZ> vertices, double[] aabbCache) {
        if (!tryAABB2DCollision(point, aabbCache)) return false;
        return isPointInPolygon(point, vertices);
    }

    /**
     * AABB 2D collision detection between a p and a polygon
     * AABB stands for Axis-Aligned Bounding Box
     * 2D means we are working in a 2D space (X and Z coordinates)
     * This function checks if the p is inside the bounding box of the polygon
     * The bounding box is defined by the minimum and maximum X and Z coordinates of the polygon
     * If the p is outside the bounding box, it cannot be inside the polygon
     * This is a quick check to avoid unnecessary calculations
     *
     * @param point     the p to check
     * @param aabbCache the bounding box of the polygon, defined by [minX, maxX, minZ, maxZ]
     * @return true if the p is inside the bounding box of the polygon, false otherwise
     */
    public static boolean tryAABB2DCollision(XZ point, double[] aabbCache) {
        return !(point.x() < aabbCache[0]) && !(point.x() > aabbCache[1]) && !(point.z() < aabbCache[2]) && !(point.z() > aabbCache[3]);
    }

    /**
     * Ray-casting algorithm to determine if a point is inside a polygon
     * The algorithm works by drawing a horizontal ray to the right (EAST in Minecraft) from the point
     * and counting how many times it intersects the edges of the polygon
     * If the number of intersections is odd, the point is inside the polygon
     * If the number of intersections is even, the point is outside the polygon
     *
     * @param point    the point to check
     * @param vertices the vertices of the polygon
     * @return true if the point is inside the polygon, false otherwise
     */
    private static boolean isPointInPolygon(XZ point, List<XZ> vertices) {
        int intersections = 0;
        for (int i = 0; i < vertices.size(); i++) {
            XZ v1 = vertices.get(i);
            XZ v2 = vertices.get((i + 1) % vertices.size());
            intersections += rayIntersectsSegment(point, v1, v2);
        }
        return (intersections % 2) == 1;
    }
    /**
     * <p><strong>Checks if a horizontal ray to the right from point p intersects the segment v1-v2.</strong></p>
     * <a href="https://www.youtube.com/watch?v=RSXM9bgqxJM">Nice video explaining the algorithm by Inside Code</a>
     * <p><strong>TLDR:</strong>
     * <ul>
     *   <li>First condition: (p.z() &lt; v1.z()) != (p.z() &lt; v2.z())</li>
     *   <li>Second condition: p.x() &lt; v1.x() + ((p.z() - v1.z()) / (v2.z() - v1.z())) * (v2.x() - v1.x())</li>
     * </ul></p>
     * @param p  the point to check
     * @param v1 the first vertex of the segment
     * @param v2 the second vertex of the segment
     * @return 1 if the ray intersects the segment, 0 otherwise
     */
    private static int rayIntersectsSegment(XZ p, XZ v1, XZ v2) {
        return (((p.z() < v1.z()) != (p.z() < v2.z())) && (p.x() < (v1.x() + ((p.z() - v1.z()) / (v2.z() - v1.z())) * (v2.x() - v1.x())))) ? 1 : 0;
    }

    /**
     * Get the corners of a polygon defined by its vertices
     *
     * @param vertices the vertices of the polygon
     * @return an array of doubles representing the corners of the polygon, defined by [minX, maxX, minZ, maxZ]
     */
    public static double[] getCorners(List<XZ> vertices) {
        double minX = vertices.get(0).x();
        double maxX = vertices.get(0).x();
        double minZ = vertices.get(0).z();
        double maxZ = vertices.get(0).z();
        for (XZ vertex : vertices) {
            if (vertex.x() < minX) minX = vertex.x();
            if (vertex.x() > maxX) maxX = vertex.x();
            if (vertex.z() < minZ) minZ = vertex.z();
            if (vertex.z() > maxZ) maxZ = vertex.z();
        }
        return new double[]{minX, maxX, minZ, maxZ};
    }
}