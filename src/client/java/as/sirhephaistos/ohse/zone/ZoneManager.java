package as.sirhephaistos.ohse.zone;

import as.sirhephaistos.ohse.client.utilityClasses.BetterPosition;
import net.minecraft.util.math.BlockPos;

import java.util.LinkedList;

@SuppressWarnings("unused")
public final class ZoneManager {
    private static LinkedList<BetterPosition> bottomPositions = new LinkedList<BetterPosition>();
    private static int currentYNegativeOffset = 0;
    private static int currentYPositiveOffset = 0;
    private static int smallestY = 0;

    public static int getLargestY() {
        return largestY;
    }

    public static void setLargestY(int largestY) {
        ZoneManager.largestY = largestY;
    }

    public static int getSmallestY() {
        return smallestY;
    }

    public static void setSmallestY(int smallestY) {
        ZoneManager.smallestY = smallestY;
    }

    private static int largestY = 0;

    private static boolean editingPositiveY = true; // true if editing positive Y, false if editing negative Y
    public static boolean isEditingPositiveY() {
        return editingPositiveY;
    }
    public static void setEditingPositiveY(boolean editingPositiveY) {
        ZoneManager.editingPositiveY = editingPositiveY;
    }

    private ZoneManager() {}
    public static void clear() { bottomPositions.clear(); }
    public static boolean positionIsEmpty() { return bottomPositions.isEmpty(); }
    public static void addRef(BlockPos pos, int faceId) {
        bottomPositions.add(new BetterPosition(pos, faceId));
    }

    public static void addRef(BetterPosition pos) {
        if( insideWithOutY(pos) ) {
            throw new IllegalArgumentException("Position already exists (X and Z are the same).");
        }
         if (bottomPositions.isEmpty()){
            bottomPositions.add(pos);
            smallestY = pos.getY();
            largestY = pos.getY();
        }else{
            if (pos.getY() < smallestY) smallestY = pos.getY();
            if (pos.getY() > largestY) largestY = pos.getY();
            bottomPositions.add(pos);
            if (pos.getY() > smallestY){setAllYtoSmallestY();}
        }
    }
    private static boolean insideWithOutY(BetterPosition a) {
        for (BetterPosition b : bottomPositions) {
            double x1 = a.getX()+0.5;
            double z1 = a.getZ()+0.5;
            double x2 = b.getX()+0.5;
            double z2 = b.getZ()+0.5;
            switch (a.getFace()){
                case EAST -> x1 -= +1;
                case SOUTH -> z1 -= +1;
            }
            switch (b.getFace()){
                case EAST -> x2 -= +1;
                case SOUTH -> z2 -= +1;
            }
            if (x1 == x2 && z1 == z2)
                return true;
        };
        return false;
    }
    public static void setAllYtoSmallestY() {
        var temp = new LinkedList<BetterPosition>();
        for (BetterPosition bp : bottomPositions) {
            temp.add(new BetterPosition(bp.getX(), smallestY, bp.getZ(), bp.getFace()));
        }
        bottomPositions = temp;
    }
    public static LinkedList<BetterPosition> getBottomPositions() {
        return bottomPositions;
    }
    public static void removeLastRef() {
        if(!bottomPositions.isEmpty())
            bottomPositions.removeLast();
    }
    public static void removeFirstRef() {
        if(!bottomPositions.isEmpty())
            bottomPositions.removeFirst();
    }

    public static int getCurrentYPositiveOffset() {
        return currentYPositiveOffset;
    }

    public static void setCurrentYPositiveOffset(int currentYPositiveOffset) {
        ZoneManager.currentYPositiveOffset = currentYPositiveOffset;
    }

    public static int getCurrentYNegativeOffset() {
        return currentYNegativeOffset;
    }

    public static void setCurrentYNegativeOffset(int currentYNegativeOffset) {
        ZoneManager.currentYNegativeOffset = currentYNegativeOffset;
    }
}
