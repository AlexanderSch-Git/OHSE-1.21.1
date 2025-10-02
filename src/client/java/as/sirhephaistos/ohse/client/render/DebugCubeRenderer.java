package as.sirhephaistos.ohse.client.render;

import as.sirhephaistos.ohse.client.debug.CubeDebugManager;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.render.*;

public final class DebugCubeRenderer implements WorldRenderEvents.Last {
    private static boolean bigBoxDrawn = false;

    public static boolean isBigBoxDrawn() {
        return bigBoxDrawn;
    }

    public static void setBigBoxDrawn(boolean value) {
        bigBoxDrawn = value;
    }

    public static void register() {
        WorldRenderEvents.LAST.register( new DebugCubeRenderer());
    }
    @Override
    public void onLast(WorldRenderContext ctx) {
        // Example usage of DrawUtil to draw a wireframe box at the origin
        if (bigBoxDrawn) {// Only draw the big box if the flag is set
            DrawUtil.drawWireframeBox(ctx,
                    -5, -5, -5,
                    5, 5, 5,
                    1f, 0f, 0f, 0.5f);
        }
        // Draw all cubes from CubeDebugManager
        var cubes = CubeDebugManager.getPositions();
        if (cubes.isEmpty()) return; // Nothing to draw
        for (var pos : cubes) {
            DrawUtil.drawFilledBox(ctx,
                    pos.getX(), pos.getY(), pos.getZ(),
                    pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1,
                    1f, 1f, 1f, 0.2f);
            DrawUtil.drawWireframeBox(ctx,
                    pos.getX(), pos.getY(), pos.getZ(),
                    pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1,
                    1f, 0f, 0f, 0.5f);
        }
    }
    /*
    @Override
    public void onLast(WorldRenderContext ctx) {
        MatrixStack matrices = ctx.matrixStack();
        if (matrices == null) return;

        // --- IMPORTANT ---
        // In practice on 1.21.1 with Fabric's LAST event, the matrix may NOT be camera-offset.
        // To ensure world-space coordinates render fixed in the world, translate by -camera here.
        var cam = ctx.camera() != null ? ctx.camera().getPos() : null;
        matrices.push();
        if (cam != null) {
            matrices.translate(-cam.x, -cam.y, -cam.z);
        }

        // Overlay: visible through blocks
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();

        // Build a LINES buffer (1.21+)
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);

        // Color RGBA
        float r = 1f, g = 0f, b = 0f, a = 0.5f;

        // Hardcoded WORLD-SPACE box (DO NOT subtract camera here)
        Box worldBox = new Box(-5, -5, -5, 10, 10, 10);

        // Draw directly via Minecraft's util (no custom helpers)
        WorldRenderer.drawBox(
                matrices, buffer,
                worldBox.minX, worldBox.minY, worldBox.minZ,
                worldBox.maxX, worldBox.maxY, worldBox.maxZ,
                r, g, b, a
        );

        // Flush with the proper shader for line rendering
        RenderSystem.setShader(GameRenderer::getRenderTypeLinesProgram);
        BufferRenderer.drawWithGlobalProgram(buffer.end());

        // Restore GL state
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();

        matrices.pop();
    }
    */

//    @Override <-- Use this method for rollback compatibility
/*    public void OLDonLast(WorldRenderContext ctx) {
        var matrices = ctx.matrixStack(); // MatrixStack is a stack of matrices used for rendering. Matrices are used to transform vertices. Verticies are points in 3D space.
        var camera = ctx.camera(); // Camera of the player
        var consumers = ctx.consumers(); // VertexConsumerProvider is used to get a VertexConsumer for rendering. VertexConsumer is used to render vertices.
        if (matrices == null) throw new IllegalStateException("No MatrixStack");
        if (camera == null) throw new IllegalStateException("No camera");
        if (consumers == null) throw new IllegalStateException("No VertexConsumerProvider");

        var cam = camera.getPos(); // Get the position of the camera
        if (cam == null) throw new IllegalStateException("No camera position");
        matrices.push(); // Push the current matrix onto the stack
        matrices.translate(-cam.x, -cam.y, -cam.z); // Translate the matrix to the camera position, so we can render relative to the user position

        VertexConsumer lines = consumers.getBuffer(RenderLayer.getLines()); // Get a VertexConsumer for rendering lines from the provider.
        float red = 1.0f, green = 0.0f, blue = 0.0f, alpha = 1.0f; // Color of the lines (red, green, blue, alpha) it is RGBA format here the results are red lines
        if (lines == null) throw new IllegalStateException("No VertexConsumer for lines");
        for (Box box: CubeDebugManager.getCubes()){
            // Were going to inflate the box by a small amount so that we can prevent z-fighting
            Box inflated = DrawUtil.expand(box,0.0025); // Inflate the box by 0.0025 in all directions
            DrawUtil.drawWireframeBox(matrices, lines, inflated, red, green, blue, alpha); // Draw the box
        }
        matrices.pop(); // Pop the matrix off the stack to restore the previous state

        RenderSystem.enableDepthTest();
    }*/
}
