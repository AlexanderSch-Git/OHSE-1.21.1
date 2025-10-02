package as.sirhephaistos.ohse.client.render;

import as.sirhephaistos.ohse.client.debug.LineDebugManager;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.render.*;

public final class DebugLineRenderer implements WorldRenderEvents.Last{

    public static void register() {
        WorldRenderEvents.LAST.register( new DebugLineRenderer());
    }

    @Override
    public void onLast(WorldRenderContext ctx) {
        var linesToDraw = LineDebugManager.getLines();
        if (linesToDraw.isEmpty()) return; // Nothing to draw
        for (var line : linesToDraw) {
            DrawUtil.drawLine(ctx,
                    line.start.x, line.start.y, line.start.z,
                    line.end.x, line.end.y, line.end.z,
                    0f, 1f, 0f, 1f); // RGBA Green line
        }
    }
}
