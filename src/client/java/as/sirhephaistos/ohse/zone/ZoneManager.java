package as.sirhephaistos.ohse.zone;

import as.sirhephaistos.ohse.client.utilityClasses.BetterPosition;
import as.sirhephaistos.ohse.network.XZ;
import as.sirhephaistos.ohse.network.ZoneRecuperationClientResponsePayload;
import lombok.Getter;
import lombok.Setter;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import org.jetbrains.annotations.NotNull;
import oshi.util.tuples.Pair;

import java.util.LinkedList;
import java.util.List;

/**
 * The ZoneManager class manages a collection of BetterPosition objects, representing positions in a zone.
 * It provides methods to add, remove, and manipulate these positions, ensuring constraints on their coordinates.
 */
@Setter
@Getter
public final class ZoneManager {
    // List of positions in the zone
    @Getter  private static LinkedList<BetterPosition> bottomPositions = new LinkedList<>();
    // Current offsets for Y-axis manipulation
    @Setter @Getter private static int YOffset = 0;
    // Smallest and largest Y values in the zone
    @Setter @Getter private static double smallestY = 0;
    // Indicates whether editing is focused on positive Y values
    @Setter @Getter private static boolean editingPositiveY = true;
    // Private constructor to prevent instantiation
    private ZoneManager() {}
    /**
     * Clears all positions from the zone.
     */
    public static void clear() {
        bottomPositions.clear();
    }
    /**
     * Checks if the list of positions is empty.
     *
     * @return true if no positions exist, false otherwise.
     */
    public static boolean positionIsEmpty() {
        return bottomPositions.isEmpty();
    }
    /**
     * Adds a new position to the zone.
     *
     * @param pos The BetterPosition to add.
     * @throws IllegalArgumentException if a position with the same X and Z already exists.
     */
    public static void addRef(BetterPosition pos) {
        if (insideWithOutY(pos)) {
            throw new IllegalArgumentException("Position already exists (X and Z are the same).");
        }
        if (bottomPositions.isEmpty()) {
            bottomPositions.add(pos);
            smallestY = pos.getY();
        } else {
            /*bottomPositions.add(pos);
            smallestY = Math.min(smallestY, pos.getY());
            largestY = Math.max(largestY, pos.getY());
            if (pos.getY() > smallestY) setAllYtoSmallestY();*/ // Old behavior: always set all Y to smallest Y
            bottomPositions.add(pos);
            if (pos.getY() < smallestY){
                double diff = Math.abs(smallestY - pos.getY());
                setSmallestY(pos.getY());
                setYOffset((int) (getYOffset() + diff));
                setAllYtoSmallestY();
            }
            if (pos.getY() >= smallestY+ getYOffset()){
                setYOffset((int) (pos.getY()-smallestY));
                pos.setY(smallestY);
            }
        }
    }
    /**
     * Checks if a position with the same X and Z coordinates already exists in the zone.
     *
     * @param a The position to check.
     * @return true if a matching position exists, false otherwise.
     */
   private static boolean insideWithOutY(BetterPosition a) {
        return bottomPositions.stream()
            .anyMatch(bp -> bp.getX() == a.getX() && bp.getZ() == a.getZ());
    }
    /**
     * Sets the Y coordinate of all positions to the smallest Y value in the zone.
     */
    public static void setAllYtoSmallestY() {bottomPositions.forEach(bp -> bp.setY(smallestY));}
    /**
     * Removes the last position added to the zone.
     */
    public static void removeLastRef() {
        if (!bottomPositions.isEmpty())
            bottomPositions.removeLast();
        if (bottomPositions.isEmpty()) {
            smallestY = 0;
            YOffset = 0;}
    }
    /**
     * Retrieves the number of positions currently in the zone.
     * @return The size of the positions list.
     */
    public static int positionsSize() {
        return bottomPositions.size();
    }

    public static @NotNull String validateCurrentZone(String zoneName) {
        StringBuilder report = new StringBuilder();
        if (bottomPositions.isEmpty()) {
            report.append("[OHSE] No positions defined in the zone.\n");
            return report.toString();
        }
        try{
            List<XZ> xzList = bottomPositions.stream()
                .map(bp -> new XZ(bp.getX(),bp.getZ())).toList();
            ClientPlayNetworking.send(new ZoneRecuperationClientResponsePayload(zoneName,
                xzList,ZoneManager.getSmallestY(), ZoneManager.getSmallestY()+getYOffset()));
            //ZoneManager.clear();
        }
        catch (Exception e) {
            report.append("[OHSE] An error occurred during validation: ").append(e.getMessage()).append("\n");
        }
        if (report.isEmpty()) {
            report.append("[OHSE] Zone validation passed. No issues found.\n");
        }
        return report.toString();
    }
}
