package as.sirhephaistos.ohse.client.debug;

import as.sirhephaistos.ohse.client.debug.utilityClasses.UtilsLinePoint;
import as.sirhephaistos.ohse.client.debug.utilityClasses.UtilsLinesInfos;

import java.util.ArrayList;
import java.util.List;

public class LineDebugManager {
    private static final List<UtilsLinesInfos> LINES = new ArrayList<>();

    private LineDebugManager() {}

    public static List<UtilsLinesInfos> getLines() { return LINES;}
    @SuppressWarnings("unused")
    public static void addLine(UtilsLinePoint start, UtilsLinePoint end) { LINES.add(new UtilsLinesInfos(start, end)); }
    public static void addLine(double x1, double y1, double z1, double x2, double y2, double z2) {
        LINES.add(new UtilsLinesInfos(new UtilsLinePoint(x1, y1, z1), new UtilsLinePoint(x2, y2, z2)));
    }
    public static void clear() { LINES.clear(); }
    @SuppressWarnings("unused")
    public static boolean isEmpty() { return LINES.isEmpty(); }
}
