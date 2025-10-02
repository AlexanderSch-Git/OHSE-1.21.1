package as.sirhephaistos.ohse.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Box;

import static net.minecraft.util.math.Direction.*;

public final class DrawUtil {
    private DrawUtil() {}

    /** Draws a wireframe box.
     * @param ctx WorldRenderContext from the render event
     * @param x1 Minimum X coordinate
     * @param y1 Minimum Y coordinate
     * @param z1 Minimum Z coordinate
     * @param x2 Maximum X coordinate
     * @param y2 Maximum Y coordinate
     * @param z2 Maximum Z coordinate
     * @param r Red color component (0.0 - 1.0)
     * @param g Green color component (0.0 - 1.0)
     * @param b Blue color component (0.0 - 1.0)
     * @param a Alpha (transparency) component (0.0 - 1.
     */
    public static void drawWireframeBox(WorldRenderContext ctx,
                                        double x1, double y1, double z1,
                                        double x2, double y2, double z2,
                                        float r, float g, float b, float a) {
        MatrixStack matrices = ctx.matrixStack();
        if (matrices == null) return;
        // --- IMPORTANT ---
        // In practice on 1.21.1 with Fabric's LAST event, the matrix may NOT be camera-offset.
        // To ensure world-space coordinates render fixed in the world, translate by -camera here.
        var cam = ctx.camera() != null ? ctx.camera().getPos() : null;
        matrices.push(); // Push the current matrix onto the stack
        if (cam == null) return; // We need a camera to render relative to the user
        matrices.translate(-cam.x, -cam.y, -cam.z); // (" it moves to world space ") based on the camera position
        // Overlay: visible through blocks
        RenderSystem.disableDepthTest(); // Disable depth testing so we can see through blocks
        RenderSystem.enableBlend(); // Enable blending so we can have transparency
        RenderSystem.defaultBlendFunc(); // Set the blend function to the default
        RenderSystem.disableCull(); // Disable face culling so we can see lines even if they are backfacing the camera

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);

        Box worldBox = new Box(x1, y1, z1, x2, y2, z2);

        WorldRenderer.drawBox(matrices, buffer,
                worldBox.minX, worldBox.minY, worldBox.minZ,
                worldBox.maxX, worldBox.maxY, worldBox.maxZ,
                r, g, b, a);

        RenderSystem.setShader(GameRenderer::getRenderTypeLinesProgram);
        BufferRenderer.drawWithGlobalProgram(buffer.end());

        // Restore GL state
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();

        matrices.pop();
    }

    /** Draws a filled box.
     * @param ctx WorldRenderContext from the render event
     * @param x1 Minimum X coordinate
     * @param y1 Minimum Y coordinate
     * @param z1 Minimum Z coordinate
     * @param x2 Maximum X coordinate
     * @param y2 Maximum Y coordinate
     * @param z2 Maximum Z coordinate
     * @param r Red color component (0.0 - 1.0)
     * @param g Green color component (0.0 - 1.0)
     * @param b Blue color component (0.0 - 1.0)
     * @param a Alpha (transparency) component (0.0 - 1.0)
     */
    public static void drawFilledBox(WorldRenderContext ctx,
                                 double x1, double y1, double z1,
                                 double x2, double y2, double z2,
                                 float r, float g, float b, float a) {
        MatrixStack matrices = ctx.matrixStack();
        if (matrices == null) return;
        // --- IMPORTANT ---
        // In practice on 1.21.1 with Fabric's LAST event, the matrix may NOT be camera-offset.
        // To ensure world-space coordinates render fixed in the world, translate by -camera here.
        var cam = ctx.camera() != null ? ctx.camera().getPos() : null;
        matrices.push(); // Push the current matrix onto the stack
        if (cam == null) return; // We need a camera to render relative to the user
        matrices.translate(-cam.x, -cam.y, -cam.z); // (" it moves to world space ") based on the camera position
        // Overlay: visible through blocks
        RenderSystem.disableDepthTest(); // Disable depth testing so we can see through blocks
        RenderSystem.enableBlend(); // Enable blending so we can have transparency
        RenderSystem.defaultBlendFunc(); // Set the blend function to the default
        RenderSystem.disableCull(); // Disable face culling so we can see lines even if they are backfacing the camera

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        //Box worldBox = new Box(x1, y1, z1, x2, y2, z2);

        //not working well for filled box because of quads winding order issues maybe or something
        /*WorldRenderer.drawBox(matrices, buffer,
                worldBox.minX, worldBox.minY, worldBox.minZ,
                worldBox.maxX, worldBox.maxY, worldBox.maxZ,
                r, g, b, a);*/

        WorldRenderer.renderFilledBoxFace(matrices,buffer, NORTH,
                (float) x1,(float)y1,(float)z1,(float)x2,(float)y2,(float)z2,r,g,b,a);
        WorldRenderer.renderFilledBoxFace(matrices,buffer, SOUTH,
                (float) x1,(float)y1,(float)z1,(float)x2,(float)y2,(float)z2,r,g,b,a);
        WorldRenderer.renderFilledBoxFace(matrices,buffer, EAST,
                (float) x1,(float)y1,(float)z1,(float)x2,(float)y2,(float)z2,r,g,b,a);
        WorldRenderer.renderFilledBoxFace(matrices,buffer,WEST,
                (float) x1,(float)y1,(float)z1,(float)x2,(float)y2,(float)z2,r,g,b,a);
        WorldRenderer.renderFilledBoxFace(matrices,buffer,UP,
                (float) x1,(float)y1,(float)z1,(float)x2,(float)y2,(float)z2,r,g,b,a);
        WorldRenderer.renderFilledBoxFace(matrices,buffer,DOWN,
                (float) x1,(float)y1,(float)z1,(float)x2,(float)y2,(float)z2,r,g,b,a);

        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        BufferRenderer.drawWithGlobalProgram(buffer.end());

        // Restore GL state
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();

        matrices.pop();
    }

    /** Draws a line between two points.
     * @param ctx WorldRenderContext from the render event
     * @param x1 Starting point X coordinate
     * @param y1 Starting point Y coordinate
     * @param z1 Starting point Z coordinate
     * @param x2 Ending point X coordinate
     * @param y2 Ending point Y coordinate
     * @param z2 Ending point Z coordinate
     * @param r Red color component (0.0 - 1.0)
     * @param g Green color component (0.0 - 1.0)
     * @param b Blue color component (0.0 - 1.0)
     * @param a Alpha (transparency) component (0.0 - 1.0)
     */
    public static void drawLine(WorldRenderContext ctx,
                                double x1, double y1, double z1,
                                double x2, double y2, double z2,
                                float r, float g, float b, float a) {
        MatrixStack matrices = ctx.matrixStack();
        if (matrices == null) return;
        // --- IMPORTANT ---
        // In practice on 1.21.1 with Fabric's LAST event, the matrix may NOT be camera-offset.
        // To ensure world-space coordinates render fixed in the world, translate by -camera here.
        var cam = ctx.camera() != null ? ctx.camera().getPos() : null;
        matrices.push(); // Push the current matrix onto the stack
        if (cam == null) return; // We need a camera to render relative to the user
        matrices.translate(-cam.x, -cam.y, -cam.z); // (" it moves to world space ") based on the camera position
        // Overlay: visible through blocks
        RenderSystem.disableDepthTest(); // Disable depth testing so we can see through blocks
        RenderSystem.enableBlend(); // Enable blending so we can have transparency
        RenderSystem.defaultBlendFunc(); // Set the blend function to the default
        RenderSystem.disableCull(); // Disable face culling so we can see lines even if they are backfacing the camera

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);
        MatrixStack.Entry entry = matrices.peek();
        if (entry == null) return;

        // direction normale (requise par le shader LINES)
        double dx = x2 - x1, dy = y2 - y1, dz = z2 - z1;
        double len = Math.sqrt(dx*dx + dy*dy + dz*dz);
        if (len == 0) {
            matrices.pop();
            RenderSystem.disableBlend();
            RenderSystem.enableDepthTest();
            return;
        }
        float nx = (float)(dx / len);
        float ny = (float)(dy / len);
        float nz = (float)(dz / len);

        // 2 sommets = 1 segment
        buffer.vertex(entry, (float)x1, (float)y1, (float)z1)
                .color(r, g, b, a)
                .normal(entry, nx, ny, nz);
        buffer.vertex(entry, (float)x2, (float)y2, (float)z2)
                .color(r, g, b, a)
                .normal(entry, nx, ny, nz);

        RenderSystem.setShader(GameRenderer::getRenderTypeLinesProgram);
        BufferRenderer.drawWithGlobalProgram(buffer.end());

        // Restore GL state
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();

        matrices.pop();
    }
}
