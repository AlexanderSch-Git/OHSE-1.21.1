package as.sirhephaistos.ohse.command;

import as.sirhephaistos.ohse.config.OHSEConfig;
import as.sirhephaistos.ohse.network.ZoneWandInitRecuperationPayload;
import as.sirhephaistos.ohse.registry.OHSEItems;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.literal;

public final class OHSECommands {
    private OHSECommands() {}

    /**
     * Registers the OHSE commands with the Minecraft command dispatcher.
     * This method should be called during server initialization to ensure
     * the commands are available to players with the appropriate permissions.
     */
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, env) -> register(dispatcher));
    }

    /**
     * Helper method to build and register the command tree.
     * @param dispatcher the command dispatcher to register commands with.
     */
    private static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        // --- /ohse base literal
        var ohse = literal("ohse")
                .requires(src -> Permissions.check(src, "ohse-admin", 3)) // Permission level 2 (command blocks, server operators)
                .executes(ctx -> {
                    ctx.getSource().sendFeedback(() -> Text.literal("[OHSE] Root command"), false);
                    return 1;
                });

        // --- /ohse wand
        var wand = literal("wand")
                .requires(src -> Permissions.check(src, "ohse-admin", 3))
                .executes(ctx -> {
                    ctx.getSource().sendFeedback(() -> Text.literal("[OHSE] /ohse wand <subcommand>"), false);
                    return 1;
                });

        // --- /ohse wand give
        var give = literal("give")
                .requires(src -> Permissions.check(src, "ohse-admin", 3))
                .executes(ctx -> {
                    ServerPlayerEntity player = ctx.getSource().getPlayer();
                    if (player == null) {
                        ctx.getSource().sendError(Text.literal("[OHSE] Player only command."));
                        return 0;
                    }
                    ItemStack wandStack = new ItemStack(OHSEItems.ZONE_WAND);
                    if (!player.getInventory().insertStack(wandStack)) {
                        player.dropItem(wandStack, false);
                    }
                    ctx.getSource().sendFeedback(() -> Text.literal("[OHSE] Wand given."), false);
                    return 1;
                });

        // --- /ohse validate
        var validate = literal("validate")
                .requires(src -> Permissions.check(src, "ohse-admin", 3))
                .then(CommandManager.argument("zoneName", StringArgumentType.word())
                        .executes(ctx -> {
                            String zoneName = StringArgumentType.getString(ctx, "zoneName");
                            ctx.getSource().sendFeedback(
                                    () -> Text.literal("[OHSE] Starting zone validation for: " + zoneName),
                                    false
                            );
                            ServerPlayerEntity player = ctx.getSource().getPlayer();
                            if (player != null) {
                                ServerPlayNetworking.send(player, new ZoneWandInitRecuperationPayload(zoneName));
                            }
                            return 1;
                        })
                );


        // --- /ohse help
        var help = literal("help")
                .requires(src -> Permissions.check(src, "ohse-admin", 3))
                .executes(ctx -> {
                    var src = ctx.getSource();
                    src.sendFeedback(() -> Text.literal("[OHSE] Available commands:"), false);
                    src.sendFeedback(() -> Text.literal("/ohse wand give"), false);
                    src.sendFeedback(() -> Text.literal("/ohse validate"), false);
                    src.sendFeedback(() -> Text.literal("/ohse help"), false);
                    return 1;
                });

        // dans OHSECommands.register(...)
        var cfgShow = literal("show")
                .requires(src -> Permissions.check(src, "ohse-admin", 3))
                .executes(ctx -> {
                    var c = OHSEConfig.get();
                    ctx.getSource().sendFeedback(() -> Text.literal(
                            "[OHSE] Config:\n  maxRaycastDistance = %s\n  sprintMultiplier   = %d\n  sneakMultiplier    = %d"
                                    .formatted(c.maxRaycastDistance, c.ctrlMultiplier, c.shiftMultiplier)
                    ), false);
                    return 1;
                });

        var cfgReload = literal("reload")
                .requires(src -> Permissions.check(src, "ohse-admin", 3))
                .executes(ctx -> {
                    as.sirhephaistos.ohse.config.OHSEConfig.reload();
                    ctx.getSource().sendFeedback(() -> Text.literal("[OHSE] Config reloaded."), false);
                    return 1;
                });

        var cfg = literal("config")
                .requires(src -> Permissions.check(src, "ohse-admin", 3))
                .requires(src -> src.hasPermissionLevel(2))
                .then(cfgShow)
                .then(cfgReload);



        // On assemble l’arbre :
        wand.then(give);         // /ohse wand give
        ohse.then(wand);         // /ohse wand
        ohse.then(validate);     // /ohse validate
        ohse.then(help);         // /ohse help
        ohse.then(cfg);

        // Enregistrement final
        dispatcher.register(ohse);
    }
}
