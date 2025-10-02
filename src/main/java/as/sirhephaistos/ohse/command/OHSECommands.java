// java
package as.sirhephaistos.ohse.command;

import as.sirhephaistos.ohse.registry.OHSEItems;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public final class OHSECommands {

    private OHSECommands() {}

    public static void register() {
        System.out.println("[OHSE]Registering Commands.");
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, env) -> register(dispatcher));
        System.out.println("[OHSE]Commands registered.");
    }

    private static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        System.out.println("[OHSE] Entering command registration.");
        dispatcher.register(
                CommandManager.literal("ohse")
                        .requires(source -> source.hasPermissionLevel(0))
                        .executes(ctx -> {
                            ctx.getSource().sendFeedback(() -> Text.literal("[OHSE] Entering command registration."), false);
                            return 1;
                        })
                        .then(CommandManager.literal("wand")
                                .executes(ctx ->{
                                    ctx.getSource().sendFeedback(() -> Text.literal("[OHSE] /ohse wand <give>"), false);
                                    return 1;
                                })
                                .then(CommandManager.literal("give")
                                        .executes(ctx -> {
                                            System.out.println("[OHSE]Giving a Wand.");
                                            ServerPlayerEntity player = ctx.getSource().getPlayer();
                                            if (player == null) {
                                                ctx.getSource().sendError(Text.literal("[OHSE] Player only command. used from console?"));
                                                return 0;
                                            }
                                            ItemStack wandStack = new ItemStack(OHSEItems.ZONE_WAND);
                                            boolean added = player.getInventory().insertStack(wandStack);
                                            if (!added) {
                                                player.dropItem(wandStack, false);
                                            }
                                            ctx.getSource().sendFeedback(() -> Text.literal("[OHSE] Wand given."), false);
                                            return 1;
                                        })
                                )
                        )
        );
    }
}
