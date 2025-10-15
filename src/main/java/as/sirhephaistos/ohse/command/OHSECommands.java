package as.sirhephaistos.ohse.command;

import as.sirhephaistos.ohse.config.OHSEConfig;
import as.sirhephaistos.ohse.db.DbExecutor;
import as.sirhephaistos.ohse.network.ZoneWandInitRecuperationPayload;
import as.sirhephaistos.ohse.registry.OHSEItems;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import as.sirhephaistos.ohse.db.MobRepository;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import devktl.sirhephaistos.ohse.compat.cobblemon.api.OHSECobblemonCompatAPI;
import devktl.sirhephaistos.ohse.compat.cobblemon.api.MobInfo;

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

        var registerMobs = literal("registerMobs")
                .requires(src -> Permissions.check(src, "ohse-admin", 3))
                .then(CommandManager.argument("filter", StringArgumentType.word())
                        .suggests((ctx, builder) -> {
                            // Suggest only namespaces that actually contain at least one "mob" (non-MISC spawn group).
                            Set<String> namespaces = Registries.ENTITY_TYPE.stream()
                                    .filter(t -> {
                                        SpawnGroup g = t.getSpawnGroup();
                                        return g != null && g != SpawnGroup.MISC;
                                    })
                                    .map(Registries.ENTITY_TYPE::getId)
                                    .filter(Objects::nonNull)
                                    .map(Identifier::getNamespace)
                                    .collect(Collectors.toCollection(TreeSet::new));
                            for (String ns : namespaces) builder.suggest(ns);

                            // Bonus: suggest a few full ids that are mobs, to guide the user.
                            Registries.ENTITY_TYPE.stream()
                                    .filter(t -> {
                                        SpawnGroup g = t.getSpawnGroup();
                                        return g != null && g != SpawnGroup.MISC;
                                    })
                                    .limit(10)
                                    .forEach(t -> {
                                        Identifier id = Registries.ENTITY_TYPE.getId(t);
                                        if (id != null) builder.suggest(id.toString());
                                    });

                            return CompletableFuture.completedFuture(builder.build());
                        })
                        .executes(ctx -> {
                            ServerCommandSource src = ctx.getSource();
                            String filter = StringArgumentType.getString(ctx, "filter");

                            // 1) Select EntityTypes, restricted to “mobs” (non-MISC spawn group)
                            final List<EntityType<?>> selected;
                            if (filter.contains(":")) {
                                // Exact id requested
                                Identifier wanted = Identifier.tryParse(filter);
                                if (wanted == null) {
                                    src.sendError(Text.literal("[OHSE] Invalid identifier: " + filter));
                                    return 0;
                                }
                                EntityType<?> type = Registries.ENTITY_TYPE.get(wanted);
                                if (type.getSpawnGroup() != SpawnGroup.MISC) {
                                    selected = List.of(type);
                                } else {
                                    selected = List.of();
                                }
                            } else {
                                // Namespace filter
                                selected = Registries.ENTITY_TYPE.stream()
                                        .filter(type -> {
                                            Identifier id = Registries.ENTITY_TYPE.getId(type);
                                            if (id == null) return false;
                                            if (!id.getNamespace().equals(filter)) return false;
                                            SpawnGroup g = type.getSpawnGroup();
                                            return g != null && g != SpawnGroup.MISC;
                                        })
                                        .toList();
                            }

                            if (selected.isEmpty()) {
                                src.sendFeedback(() -> Text.literal("[OHSE] No mob EntityType found for \"" + filter + "\""), false);
                                return 0;
                            }

                            src.sendFeedback(() -> Text.literal("[OHSE] Importing " + selected.size() + " mob(s) for \"" + filter + "\"…"), false);

                            // 2) Offload DB work
                            DbExecutor.SINGLE.submit(() -> {
                                try {
                                    // 2.a) Build minimal records (uuid, mod_id, display_name)
                                    List<MobRepository.SimpleMob> recs = new ArrayList<>(selected.size());
                                    for (EntityType<?> t : selected) {
                                        Identifier id = Registries.ENTITY_TYPE.getId(t);
                                        if (id == null) continue;
                                        String modId = id.toString();
                                        String display = t.getName().getString();
                                        if (display.startsWith("entity.") || display.equalsIgnoreCase(id.toString())) {
                                            display = prettyFromId(id); // fallback on dedicated server
                                        }
                                        recs.add(new MobRepository.SimpleMob(UUID.randomUUID(), id.toString(), display));
                                        recs.add(new MobRepository.SimpleMob(UUID.randomUUID(), modId, display));
                                    }

                                    // 2.b) Batch insert (skip duplicates by mod_id)
                                    MobRepository.Result res = MobRepository.insertSimpleBatch(recs, /*skipIfExists=*/true);

                                    // 3) Feedback on the main thread
                                    src.getServer().execute(() ->
                                            src.sendFeedback(() -> Text.literal(
                                                    "[OHSE] Import done. Inserted: " + res.inserted() + ", skipped (already present): " + res.skipped()
                                            ), false)
                                    );
                                } catch (Exception e) {
                                    src.getServer().execute(() ->
                                            src.sendError(Text.literal("[OHSE] Import error: " + e.getMessage()))
                                    );
                                }
                            });

                            return 1;
                        })
                );


        var testCompatibiltyAPIs = literal("testCompat")
                .requires(src -> Permissions.check(src, "ohse-admin", 3))
                .executes(ctx -> {
                    var source = ctx.getSource();
                    var loader = net.fabricmc.loader.api.FabricLoader.getInstance();

                    if (!loader.isModLoaded("cobblemon")) {
                        source.sendFeedback(() -> net.minecraft.text.Text.literal("❌ Cobblemon not detected."), false);
                        return 1;
                    }//  ze need to add a verif for conpat nod exiting on server
                    // Try instance-based API
                    java.util.Map<String, java.util.List<devktl.sirhephaistos.ohse.compat.cobblemon.api.MobInfo>> map = java.util.Map.of();
                    Object apiObj = loader.getObjectShare().get("ohse-cobble-compat:api");
                    if (apiObj instanceof devktl.sirhephaistos.ohse.compat.cobblemon.api.OHSECobblemonCompatAPI api) {
                        map = api.getMobCatalog();
                    } else {
                        // Fallback: old provider key
                        Object provObj = loader.getObjectShare().get("ohse-cobble-compat:provider");
                        if (provObj instanceof devktl.sirhephaistos.ohse.compat.cobblemon.api.MobCatalogProvider prov) {
                            map = prov.getAllMobs();
                        } else {
                            source.sendFeedback(() -> net.minecraft.text.Text.literal("⚠️ Cobblemon detected, but compat API not published."), false);
                            return 1;
                        }
                    }

                    var list = map.getOrDefault("cobblemon", java.util.List.of());
                    int total = list.size();
                    if (total == 0) {
                        source.sendFeedback(() -> net.minecraft.text.Text.literal("⚠️ Compat ready but no species indexed yet (try after reload)."), false);
                        return 1;
                    }

                    int limit = Math.min(20, total); // avoid spam
                    var sb = new StringBuilder();
                    sb.append("✅ Cobblemon compat — ").append(total).append(" species.\n");
                    for (int i = 0; i < limit; i++) {
                        var m = list.get(i);
                        sb.append(i + 1).append(". ")
                                .append(m.displayName()).append("  (")
                                .append(m.modId()).append(")  → ")
                                .append(m.uuid()).append("\n");
                    }
                    if (total > limit) sb.append("...and ").append(total - limit).append(" more.");

                    source.sendFeedback(() -> net.minecraft.text.Text.literal(sb.toString()), false);
                    return 1;
                })
                ;



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
        ohse.then(registerMobs); // /ohse registerMobs
        ohse.then(testCompatibiltyAPIs); // /ohse testCompat

        // Enregistrement final
        dispatcher.register(ohse);
    }

    private static String prettyFromId(Identifier id) {
        String path = id.getPath().replace('_', ' ');
        if (path.isEmpty()) return id.toString();
        return Character.toUpperCase(path.charAt(0)) + path.substring(1);
    }
}
