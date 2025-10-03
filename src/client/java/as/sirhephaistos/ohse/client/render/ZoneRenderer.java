package as.sirhephaistos.ohse.client.render;

import as.sirhephaistos.ohse.client.utilityClasses.BetterPosition;
import as.sirhephaistos.ohse.zone.ZoneManager;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

import java.text.MessageFormat;

public final class ZoneRenderer implements WorldRenderEvents.Last {
    public static void register() {
        WorldRenderEvents.LAST.register( new ZoneRenderer());
    }
    @Override
    public void onLast(WorldRenderContext ctx) {
        var points = ZoneManager.getBottomPositions();
        if (points.isEmpty()) return;


        for (var p : points) {
            int xOffset = 1, yOffset = 1, zOffset = 1;
            switch (p.getFace()){
                case EAST -> xOffset = -1;
                case SOUTH -> zOffset = -1;
            }
            DrawUtil.drawFilledBox(ctx, p.getX(), ZoneManager.getSmallestY(), p.getZ(),
                    p.getX()+xOffset, ZoneManager.getSmallestY()+yOffset, p.getZ()+zOffset,
                    1f, 1f, 1f, 0.25f);
            DrawUtil.drawWireframeBox(ctx, p.getX(), ZoneManager.getSmallestY(), p.getZ(),
                    p.getX()+xOffset, ZoneManager.getSmallestY()+yOffset, p.getZ()+zOffset,
                    1f, 0f, 0f, 0.5f);
            DrawUtil.drawFilledBox(ctx, p.getX(), ZoneManager.getLargestY(), p.getZ(),
                    p.getX()+xOffset, ZoneManager.getLargestY()+yOffset, p.getZ()+zOffset,
                    1f, 1f, 1f, 0.25f);
            DrawUtil.drawWireframeBox(ctx, p.getX(),ZoneManager.getLargestY(), p.getZ(),
                    p.getX()+xOffset, ZoneManager.getLargestY()+yOffset, p.getZ()+zOffset,
                    1f, 0f, 0f, 0.5f);
        }

        if (points.size() < 2) return;

        for (int i = 0; i < points.size(); i++) {
            var p1 = points.get(i);
            var p2 = points.get((i + 1) % points.size());
            double x1 = p1.getX() + 0.5, y1 = p1.getY() + (double) 0.5, z1 = p1.getZ() + (double) 0.5;
            double x2 = p2.getX() + 0.5, y2 = p2.getY() + (double) 0.5, z2 = p2.getZ() + (double) 0.5;
            switch (p1.getFace()){
                case EAST -> x1 -= +1;
                case SOUTH -> z1 -= +1;
            }
            switch (p2.getFace()){
                case EAST -> x2 -= +1;
                case SOUTH -> z2 -= +1;
            }
            // now we offset based on the face direction to center the points on the cubes
            float upSET = ZoneManager.getLargestY() - ZoneManager.getSmallestY();
            if (i != points.size()-1) {
                DrawUtil.drawLine(ctx, x1, y1, z1, x2, y2, z2, 0f, 0f, 1f, 1f);
                if (upSET != 0) {
                    DrawUtil.drawLine(ctx, x1, y1+upSET, z1, x2, y2+upSET, z2, 0f, 0f, 1f, 1f);
                }
            }else if (points.size() > 2 ) {
                DrawUtil.drawLine(ctx, x1, y1, z1, x2, y2, z2, 0.5f, 0.5f, 0f, 1f);
                if (upSET  != 0) {
                    DrawUtil.drawLine(ctx, x1, y1+upSET, z1, x2, y2+upSET, z2, 0.5f, 0.5f, 0f, 1f);
                }
            }
            if (upSET != 0) {
                DrawUtil.drawLine(ctx, x1, y1, z1, x1, y1+upSET, z1, 0f, 1f, 0f, 1f);
            }
        }
    }
}
