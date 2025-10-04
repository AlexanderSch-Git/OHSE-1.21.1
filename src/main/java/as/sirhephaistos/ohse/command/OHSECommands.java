package as.sirhephaistos.ohse.command;

import as.sirhephaistos.ohse.registry.OHSEItems;
import com.mojang.brigadier.CommandDispatcher;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.literal;

public final class OHSECommands {
    private OHSECommands() {}

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, env) -> register(dispatcher));
    }

    private static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        // --- /ohse base literal
        var ohse = literal("ohse")
                .requires(src -> Permissions.check(src, "ohse-admin", 0)) // Permission level 2 (command blocks, server operators)
                .executes(ctx -> {
                    ctx.getSource().sendFeedback(() -> Text.literal("[OHSE] Root command"), false);
                    return 1;
                });

        // --- /ohse wand
        var wand = literal("wand")
                .executes(ctx -> {
                    ctx.getSource().sendFeedback(() -> Text.literal("[OHSE] /ohse wand <subcommand>"), false);
                    return 1;
                });

        // --- /ohse wand give
        var give = literal("give")
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
                .executes(ctx -> {
                    ctx.getSource().sendFeedback(() -> Text.literal("[OHSE] /ohse validate executed"), false);
                    return 1;
                });

        // --- /ohse help
        var help = literal("help")
                .executes(ctx -> {
                    var src = ctx.getSource();
                    src.sendFeedback(() -> Text.literal("[OHSE] Available commands:"), false);
                    src.sendFeedback(() -> Text.literal("/ohse wand give"), false);
                    src.sendFeedback(() -> Text.literal("/ohse validate"), false);
                    src.sendFeedback(() -> Text.literal("/ohse help"), false);
                    return 1;
                });

        // On assemble l’arbre :
        wand.then(give);         // /ohse wand give
        ohse.then(wand);         // /ohse wand
        ohse.then(validate);     // /ohse validate
        ohse.then(help);         // /ohse help

        // Enregistrement final
        dispatcher.register(ohse);
    }
}
