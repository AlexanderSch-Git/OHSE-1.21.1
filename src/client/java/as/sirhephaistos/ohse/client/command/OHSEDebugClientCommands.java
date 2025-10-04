package as.sirhephaistos.ohse.client.command;

import as.sirhephaistos.ohse.client.debug.CubeDebugManager;
import as.sirhephaistos.ohse.client.debug.LineDebugManager;
import as.sirhephaistos.ohse.client.render.DebugCubeRenderer;
import as.sirhephaistos.ohse.zone.ZoneManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class OHSEDebugClientCommands {
    private OHSEDebugClientCommands() {}

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, env) -> register(dispatcher));
    }

    private static void register(CommandDispatcher<ServerCommandSource> d) {
        // /ohse_debug (root)
        var root = literal("ohse_debug")
                .requires(src -> src.hasPermissionLevel(0))
                .executes(ctx -> {
                    ctx.getSource().sendFeedback(() -> Text.literal(
                            "OHSE Debug Commands: /ohse_debug cube here | /ohse_debug cube clear | /ohse_debug cube bigCube | /ohse_debug line draw x1 y1 z1 x2 y2 z2 | /ohse_debug line clear"
                    ), false);
                    return 1;
                });

        // ===== cube subtree =====
        var cube = literal("cube")
                .executes(ctx -> {
                    ctx.getSource().sendFeedback(() -> Text.literal(
                            "[OHSE DEBUG] Commands: /ohse_debug cube here | /ohse_debug cube clear | /ohse_debug cube bigCube"
                    ), false);
                    return 1;
                });

        var cubeHere = literal("here")
                .executes(ctx -> {
                    ServerPlayerEntity player = ctx.getSource().getPlayer();
                    if (player == null) {
                        ctx.getSource().sendError(Text.literal("[OHSE DEBUG] Player only command. used from console?"));
                        return 0;
                    }
                    if (ctx.getSource().getWorld() == null) {
                        ctx.getSource().sendError(Text.literal("[OHSE DEBUG] World is null, cannot add debug cube."));
                        return 0;
                    }

                    var mc = MinecraftClient.getInstance();
                    if (mc.crosshairTarget == null || mc.crosshairTarget.getType() != HitResult.Type.BLOCK) {
                        if (mc.player == null) throw new IllegalStateException("MinecraftClient.player is null");
                        mc.player.sendMessage(Text.literal("[OHSE DEBUG] look at a block to place a debug cube"), true);
                        return 0;
                    }
                    var bhr = (net.minecraft.util.hit.BlockHitResult) mc.crosshairTarget;
                    BlockPos bp = bhr.getBlockPos();
                    CubeDebugManager.addUnitCubeAt(bp);
                    player.sendMessage(Text.literal(String.format("[OHSE DEBUG] Added debug cube at %s", bp)), false);
                    return 1;
                });

        var cubeBig = literal("bigCube")
                .executes(ctx -> {
                    DebugCubeRenderer.setBigBoxDrawn(!DebugCubeRenderer.isBigBoxDrawn());
                    ctx.getSource().sendFeedback(() -> Text.literal(
                            String.format("[OHSE DEBUG] Big cube drawing %s",
                                    DebugCubeRenderer.isBigBoxDrawn() ? "enabled" : "disabled")
                    ), false);
                    return 1;
                });

        var cubeClear = literal("clear")
                .executes(ctx -> {
                    ServerPlayerEntity player = ctx.getSource().getPlayer();
                    if (player == null) {
                        ctx.getSource().sendError(Text.literal("[OHSE] Player only command. used from console?"));
                        return 0;
                    }
                    int before = CubeDebugManager.getPositions().size();
                    CubeDebugManager.clear();
                    player.sendMessage(Text.literal(String.format(
                            "[OHSE DEBUG] Cleared %d debug cubes, now %d",
                            before, CubeDebugManager.getPositions().size()
                    )), false);
                    return 1;
                });

        // assemble: /ohse_debug cube ...
        cube.then(cubeHere);
        cube.then(cubeBig);
        cube.then(cubeClear);

        // ===== line subtree =====
        var line = literal("line")
                .executes(ctx -> {
                    ctx.getSource().sendFeedback(() -> Text.literal(
                            "[OHSE DEBUG] Usage: /ohse_debug line draw x1 y1 z1 x2 y2 z2  |  /ohse_debug line clear"
                    ), false);
                    return 1;
                });

        var lineDraw = literal("draw")
                .then(argument("x1", DoubleArgumentType.doubleArg())
                        .then(argument("y1", DoubleArgumentType.doubleArg())
                                .then(argument("z1", DoubleArgumentType.doubleArg())
                                        .then(argument("x2", DoubleArgumentType.doubleArg())
                                                .then(argument("y2", DoubleArgumentType.doubleArg())
                                                        .then(argument("z2", DoubleArgumentType.doubleArg())
                                                                .executes(ctx -> {
                                                                    double x1 = DoubleArgumentType.getDouble(ctx, "x1");
                                                                    double y1 = DoubleArgumentType.getDouble(ctx, "y1");
                                                                    double z1 = DoubleArgumentType.getDouble(ctx, "z1");
                                                                    double x2 = DoubleArgumentType.getDouble(ctx, "x2");
                                                                    double y2 = DoubleArgumentType.getDouble(ctx, "y2");
                                                                    double z2 = DoubleArgumentType.getDouble(ctx, "z2");

                                                                    LineDebugManager.addLine(x1, y1, z1, x2, y2, z2);

                                                                    ctx.getSource().sendFeedback(() -> Text.literal(String.format(
                                                                            "[OHSE DEBUG] Draw line from (%.2f, %.2f, %.2f) to (%.2f, %.2f, %.2f)",
                                                                            x1, y1, z1, x2, y2, z2
                                                                    )), false);
                                                                    return 1;
                                                                })
                                                        )
                                                )
                                        )
                                )
                        )
                );

        var lineClear = literal("clear")
                .executes(ctx -> {
                    int before = LineDebugManager.getLines().size();
                    LineDebugManager.clear();
                    ctx.getSource().sendFeedback(() -> Text.literal(
                            String.format("[OHSE DEBUG] Cleared %d debug lines, now %d", before, LineDebugManager.getLines().size())
                    ), false);
                    return 1;
                });

        var wandclear = literal("wandclear")
                .executes(ctx -> {
                    ctx.getSource().sendFeedback(() -> Text.literal(
                            "[OHSE DEBUG] Before Largest Y%d, Smallest Y%d, editingPositiveY %s. Theres is %d positions stored. Current offsets: +%d/-%d"
                            .formatted(ZoneManager.getLargestY(), ZoneManager.getSmallestY(), ZoneManager.isEditingPositiveY(),
                            ZoneManager.getBottomPositions().size(), ZoneManager.getCurrentYPositiveOffset(), ZoneManager.getCurrentYNegativeOffset())
                    ), false);
                    ZoneManager.setLargestY(0);
                    ZoneManager.setSmallestY(0);
                    ZoneManager.clear();
                    ZoneManager.setEditingPositiveY(true);
                    ZoneManager.setCurrentYNegativeOffset(0);
                    ZoneManager.setCurrentYPositiveOffset(0);
                    ctx.getSource().sendFeedback(() -> Text.literal(
                            "[OHSE DEBUG] Now Largest Y%d, Smallest Y%d, editingPositiveY %s. Theres is %d positions stored. Current offsets: +%d/-%d"
                                    .formatted(ZoneManager.getLargestY(), ZoneManager.getSmallestY(), ZoneManager.isEditingPositiveY(),
                                            ZoneManager.getBottomPositions().size(), ZoneManager.getCurrentYPositiveOffset(), ZoneManager.getCurrentYNegativeOffset())
                    ), false);
                    return 1;
                });

        // assemble: /ohse_debug line ...
        line.then(lineDraw);
        line.then(lineClear);

        // ===== assemble root =====
        root.then(cube);
        root.then(line);
        root.then(wandclear);


        // register
        d.register(root);
    }
}
