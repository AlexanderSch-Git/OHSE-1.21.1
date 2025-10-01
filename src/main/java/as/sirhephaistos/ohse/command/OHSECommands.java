package as.sirhephaistos.ohse.command;

import as.sirhephaistos.ohse.registry.OHSEItems;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.item.ItemStack;

import java.util.Collection;

/**
 * Registers the base /ohse command and simple subcommands.
 * For now these are skeleton handlers without selection logic.
 */
public final class OHSECommands {

    private OHSECommands() {}

    public static void register() {
        CommandRegistrationCallback.EVENT.register(OHSECommands::onRegister);
    }

    private static void onRegister(CommandDispatcher<ServerCommandSource> dispatcher,
                                   CommandRegistryAccess registryAccess,
                                   CommandManager.RegistrationEnvironment env) {

        dispatcher.register(CommandManager.literal("ohse")
                .requires(src -> src.hasPermissionLevel(2)) // adjust as you like
                .then(CommandManager.literal("wand")
                        .then(CommandManager.literal("give")
                                // optional target(s)
                                .then(CommandManager.argument("targets", net.minecraft.command.argument.EntityArgumentType.players())
                                        .executes(ctx -> {
                                            Collection<ServerPlayerEntity> targets =
                                                    net.minecraft.command.argument.EntityArgumentType.getPlayers(ctx, "targets");
                                            int count = 0;
                                            for (ServerPlayerEntity p : targets) {
                                                p.giveItemStack(new ItemStack(OHSEItems.ZONE_WAND));
                                                p.sendMessage(Text.literal("[OHSE] Given Zone Wand."), false);
                                                count++;
                                            }
                                            int finalCount = count;
                                            ctx.getSource().sendFeedback(() -> Text.literal("[OHSE] Gave Zone Wand to " + finalCount + " player(s)."), true);
                                            return count;
                                        })
                                )
                                // no targets -> give to executor if player
                                .executes(ctx -> {
                                    ServerCommandSource src = ctx.getSource();
                                    PlayerEntity player = src.getPlayer();
                                    if (player == null) {
                                        src.sendError(Text.literal("[OHSE] You must specify a player from console."));
                                        return 0;
                                    }
                                    player.giveItemStack(new ItemStack(OHSEItems.ZONE_WAND));
                                    player.sendMessage(Text.literal("[OHSE] Given Zone Wand."), false);
                                    return 1;
                                })
                        )
                        .then(CommandManager.literal("clear")
                                .executes(ctx -> {
                                    // Will clear the current polygon selection later.
                                    ctx.getSource().sendFeedback(() ->
                                            Text.literal("[Wand] Selection cleared (skeleton: no data yet)."), true);
                                    return 1;
                                })
                        )
                )
        );
    }
}

