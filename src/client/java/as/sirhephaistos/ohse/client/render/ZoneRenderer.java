package as.sirhephaistos.ohse.client.render;

import as.sirhephaistos.ohse.zone.ZoneManager;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

public final class ZoneRenderer implements WorldRenderEvents.Last {
    public static void register() {
        WorldRenderEvents.LAST.register( new ZoneRenderer());
    }
    @Override
    public void onLast(WorldRenderContext ctx) {
        var points = ZoneManager.getBottomPositions();
        if (points.isEmpty()) return;

        //Draw cube at each point
        points.forEach(p -> {
            // Dessiner le cube à la position d'origine
            DrawUtil.drawWireframeBox(ctx, p.getX() - 0.5, p.getY() - 0.5, p.getZ() - 0.5,
                    p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5,
                    1f, 0f, 0f, 0.5f);
            DrawUtil.drawFilledBox(ctx, p.getX() - 0.5, p.getY() - 0.5, p.getZ() - 0.5,
                    p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5,
                    1f, 1f, 1f, 0.25f);

            // Si YOffset est différent de 0, dessiner le cube et la ligne à la position offset
            if (ZoneManager.getYOffset() != 0) {
                double yOffset = ZoneManager.getYOffset();
                DrawUtil.drawWireframeBox(ctx, p.getX() - 0.5, p.getY() + yOffset - 0.5, p.getZ() - 0.5,
                        p.getX() + 0.5, p.getY() + yOffset + 0.5, p.getZ() + 0.5,
                        0f, 0f, 1f, 0.5f);
                DrawUtil.drawFilledBox(ctx, p.getX() - 0.5, p.getY() + yOffset - 0.5, p.getZ() - 0.5,
                        p.getX() + 0.5, p.getY() + yOffset + 0.5, p.getZ() + 0.5,
                        1f, 1f, 1f, 0.25f);
                DrawUtil.drawLine(ctx, p.getX(), p.getY(), p.getZ(), p.getX(), p.getY() + yOffset, p.getZ(),
                        0f, 1f, 0f, 1f);
            }
        });


        if (points.size() < 2) return;

        /*for (int i = 0; i < points.size(); i++) {
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
            double upSET = ZoneManager.getLargestY() - ZoneManager.getSmallestY();
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
        }*/ // <-- OLD WAY now we get doubles that represent the center of the cube directly, face isnt needed for that anymore
        for (int i = 0; i < points.size(); i++) {
            var p1 = points.get(i);
            var p2 = points.get((i + 1) % points.size());
            boolean isLast = (i == points.size() - 1);

            float r = isLast && points.size() > 2 ? 0.5f: 0f;
            float g = isLast && points.size() > 2 ? 0.5f : 0f;
            float b = isLast && points.size() > 2 ? 0f : 1f;

            DrawUtil.drawLine(ctx,p1.getX(),p1.getY(),p1.getZ(),p2.getX(),p2.getY(),p2.getZ(), r, g, b, 1f);
            if (ZoneManager.getYOffset() != 0) {
                double yOffset = ZoneManager.getYOffset();
                DrawUtil.drawLine(ctx,p1.getX(),p1.getY()+yOffset,p1.getZ(),p2.getX(),p2.getY()+yOffset,p2.getZ(), r, g, b, 1f);
                DrawUtil.drawLine(ctx,p1.getX(),p1.getY(),p1.getZ(),p1.getX(),p1.getY()+yOffset,p1.getZ(), 0f, 1f, 0f, 1f);
                if (isLast && points.size() > 2) {
                    DrawUtil.drawLine(ctx,p2.getX(),p2.getY(),p2.getZ(),p2.getX(),p2.getY()+yOffset,p2.getZ(), 0f, 1f, 0f, 1f);
                }
            }
        }
    }
}
