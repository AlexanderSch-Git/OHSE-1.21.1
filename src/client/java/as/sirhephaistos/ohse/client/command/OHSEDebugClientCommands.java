package as.sirhephaistos.ohse.client.command;

import as.sirhephaistos.ohse.client.debug.CubeDebugManager;
import as.sirhephaistos.ohse.client.debug.LineDebugManager;
import as.sirhephaistos.ohse.client.render.DebugCubeRenderer;
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

/**
 * Client-side commands for the OHSE debugging tools.
 * /ohse_debug cube here   -> adds a 1x1x1 wireframe at the looked-at block
 * /ohse_debug cube clear  -> clears all demo cubes
 */
public final class OHSEDebugClientCommands {
    private OHSEDebugClientCommands() {}

    public static void register() {
        System.out.println("[OHSE debug] Registering Client Debug Commands.");
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, env) -> register(dispatcher));
        System.out.println("[OHSE debug] Client Debug Commands registered.");
    }

    private static void register(CommandDispatcher<ServerCommandSource> dispatcher){
        System.out.println("[OHSE debug] Entering command registration.");
        dispatcher.register(
                CommandManager.literal("ohse_debug")
                        .requires(source -> source.hasPermissionLevel(0))
                        .executes(ctx -> {
                            ctx.getSource().sendFeedback(() -> Text.literal(
                                    "OHSE Debug Commands: /ohse_debug cube here | /ohse_debug cube clear | /ohse_debug drawline x1 y1 z1 x2 y2 z2"
                            ), false);
                            return 1;
                        })

                        // ---- /ohse_debug cube ...
                        .then(
                                CommandManager.literal("cube")
                                        .executes(ctx -> {
                                            ctx.getSource().sendFeedback(() -> Text.literal(
                                                    "[OHSE DEBUG] Commands: /ohse_debug cube here | /ohse_debug cube clear | /ohse_debug cube bigCube"
                                            ), false);
                                            return 1;
                                        })
                                        .then(
                                                CommandManager.literal("here")
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
                                                            System.out.printf("%s issued /ohse_debug cube here%n", player.getName());
                                                            var mc = MinecraftClient.getInstance();
                                                            if (mc.crosshairTarget == null || mc.crosshairTarget.getType() != HitResult.Type.BLOCK) {
                                                                if (mc.player == null)
                                                                    throw new IllegalStateException("MinecraftClient.player is null");
                                                                mc.player.sendMessage(Text.literal("[OHSE DEBUG] look at a block to place a debug cube"), true);
                                                                return 0;
                                                            }
                                                            var bhr = (net.minecraft.util.hit.BlockHitResult) mc.crosshairTarget;
                                                            BlockPos bp = bhr.getBlockPos();
                                                            CubeDebugManager.addUnitCubeAt(bp);
                                                            player.sendMessage(Text.literal(String.format("[OHSE DEBUG] Added debug cube at %s", bp)), false);
                                                            return 1;
                                                        })
                                        )
                                        .then(
                                                CommandManager.literal("bigCube")
                                                        .executes(ctx -> {
                                                            DebugCubeRenderer.setBigBoxDrawn(!DebugCubeRenderer.isBigBoxDrawn());
                                                            ctx.getSource().sendFeedback(() -> Text.literal(
                                                                    String.format("[OHSE DEBUG] Big cube drawing %s",
                                                                            DebugCubeRenderer.isBigBoxDrawn() ? "enabled" : "disabled")
                                                            ), false);
                                                            return 1;
                                                        })
                                        )
                                        .then(
                                                CommandManager.literal("clear")
                                                        .executes(ctx -> {
                                                            ServerPlayerEntity player = ctx.getSource().getPlayer();
                                                            if (player == null) {
                                                                ctx.getSource().sendError(Text.literal("[OHSE] Player only command. used from console?"));
                                                                return 0;
                                                            }
                                                            System.out.printf("%s issued /ohse_debug cube clear%n", player.getName());
                                                            int cubesCleared = CubeDebugManager.getPositions().size();
                                                            CubeDebugManager.clear();
                                                            player.sendMessage(Text.literal(String.format(
                                                                    "[OHSE DEBUG] Cleared %s debug cubes, cubes are now %s",
                                                                    cubesCleared, CubeDebugManager.getPositions().size()
                                                            )), false);
                                                            return 1;
                                                        })
                                        )
                        )

                        // ---- /ohse_debug drawline x1 y1 z1 x2 y2 z2
                        // ---- /ohse_debug line draw x1 y1 z1 x2 y2 z2  |  /ohse_debug line clear
                        .then(
                                CommandManager.literal("line")
                                        .executes(ctx -> {
                                            ctx.getSource().sendFeedback(() -> Text.literal(
                                                    "[OHSE DEBUG] Usage: /ohse_debug line draw x1 y1 z1 x2 y2 z2  |  /ohse_debug line clear"
                                            ), false);
                                            return 1;
                                        })
                                        .then(
                                                CommandManager.literal("draw")
                                                        .then(CommandManager.argument("x1", DoubleArgumentType.doubleArg())
                                                                .then(CommandManager.argument("y1", DoubleArgumentType.doubleArg())
                                                                        .then(CommandManager.argument("z1", DoubleArgumentType.doubleArg())
                                                                                .then(CommandManager.argument("x2", DoubleArgumentType.doubleArg())
                                                                                        .then(CommandManager.argument("y2", DoubleArgumentType.doubleArg())
                                                                                                .then(CommandManager.argument("z2", DoubleArgumentType.doubleArg())
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
                                                        )
                                        )
                                        .then(
                                                CommandManager.literal("clear")
                                                        .executes(ctx -> {
                                                            int before = LineDebugManager.getLines().size();   // ajoute size() dans le manager si besoin
                                                            LineDebugManager.clear();
                                                            ctx.getSource().sendFeedback(() -> Text.literal(
                                                                    String.format("[OHSE DEBUG] Cleared %d debug lines, now %d", before, LineDebugManager.getLines().size())
                                                            ), false);
                                                            return 1;
                                                        })
                                        )
                        )

        );

    }
}
