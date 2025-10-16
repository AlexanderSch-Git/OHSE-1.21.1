package as.sirhephaistos.ohse.command;

import as.sirhephaistos.ohse.config.OHSEConfig;
import as.sirhephaistos.ohse.db.DbExecutor;
import as.sirhephaistos.ohse.network.ZoneWandInitRecuperationPayload;
import as.sirhephaistos.ohse.registry.OHSEItems;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import devktl.sirhephaistos.ohse.compat.cobblemon.api.MobCatalogProvider;
import devktl.sirhephaistos.ohse.compat.cobblemon.api.MobInfo;
import devktl.sirhephaistos.ohse.compat.cobblemon.api.OHSECobblemonCompatAPI;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
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
                                        builder.suggest(id.toString());
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


        var testCompat = literal("testCompat")
                .requires(src -> Permissions.check(src, "ohse-admin", 3))
                .executes(ctx -> {
                    ServerCommandSource src = ctx.getSource();
                    var loader = net.fabricmc.loader.api.FabricLoader.getInstance();

                    // 0) Vérifs basiques
                    if (!loader.isModLoaded("cobblemon")) {
                        src.sendFeedback(() -> net.minecraft.text.Text.literal("❌ Cobblemon not detected."), false);
                        return 1;
                    }

                    // 1) Récup du catalogue via compat (instance ou provider legacy)
                    Map<String, List<MobInfo>> map;

                    Object apiObj = loader.getObjectShare().get("ohse-cobble-compat:api");
                    if (apiObj instanceof OHSECobblemonCompatAPI api) {
                        map = api.getMobCatalog();
                    } else {
                        Object provObj = loader.getObjectShare().get("ohse-cobble-compat:provider");
                        if (provObj instanceof MobCatalogProvider prov) {
                            map = prov.getAllMobs();
                        } else {
                            src.sendFeedback(() -> net.minecraft.text.Text.literal("⚠️ Cobblemon detected, but compat module not present/published on the server."), false);
                            return 1;
                        }
                    }

                    if (map.isEmpty()) {
                        src.sendFeedback(() -> net.minecraft.text.Text.literal("⚠️ Compat ready but no species indexed yet (try after reload)."), false);
                        return 1;
                    }

                    // 2) Aplatir et préparer les enregistrements pour la DB
                    final List<MobRepository.SimpleMob> recs = new ArrayList<>();
                    final List<String> preview = new ArrayList<>();
                    final int previewLimit = 20;
                    int total = 0;

                    for (var entry : map.entrySet()) {
                        String namespace = entry.getKey(); // ex: "cobblemon" (ou autre mod compat à l’avenir)
                        var list = entry.getValue();
                        for (var m : list) {
                            // Sanitize display name
                            String display = (m.displayName() == null || m.displayName().isBlank())
                                    ? m.modId()
                                    : m.displayName();
                            // On garde l'UUID fourni par le compat (utile pour cross-ref)
                            recs.add(new MobRepository.SimpleMob(m.uuid(), m.modId(), display));

                            if (preview.size() < previewLimit) {
                                preview.add(display + " (" + m.modId() + ") → " + m.uuid());
                            }
                            total++;
                        }
                    }

                    if (recs.isEmpty()) {
                        src.sendFeedback(() -> net.minecraft.text.Text.literal("⚠️ Compat map returned no mobs to import."), false);
                        return 1;
                    }

                    int finalTotal = total;
                    src.sendFeedback(() -> net.minecraft.text.Text.literal("[OHSE] Importing " + finalTotal + " compat mob(s)…"), false);

                    // 3) Offload DB work
                    DbExecutor.SINGLE.submit(() -> {
                        try {
                            // NB: on garde la même logique que registerMobs: skipIfExists = true (unicité sur mod_id côté repo)
                            MobRepository.Result res = MobRepository.insertSimpleBatch(recs, /* skipIfExists = */ true);

                            // 4) Feedback côté serveur (thread principal)
                            src.getServer().execute(() -> {
                                var sb = new StringBuilder();
                                sb.append("[OHSE] Compat import done. Inserted: ")
                                        .append(res.inserted())
                                        .append(", skipped (already present): ")
                                        .append(res.skipped())
                                        .append("\n");

                                for (int i = 0; i < preview.size(); i++) {
                                    sb.append(i + 1).append(". ").append(preview.get(i)).append("\n");
                                }
                                if (finalTotal > previewLimit) {
                                    sb.append("...and ").append(finalTotal - previewLimit).append(" more.");
                                }

                                src.sendFeedback(() -> net.minecraft.text.Text.literal(sb.toString()), false);
                            });
                        } catch (Exception e) {
                            src.getServer().execute(() ->
                                    src.sendError(net.minecraft.text.Text.literal("[OHSE] Compat import error: " + e.getMessage()))
                            );
                        }
                    });

                    return 1;
                });



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
        ohse.then(testCompat); // /ohse testCompat

        // Enregistrement final
        dispatcher.register(ohse);
    }

    private static String prettyFromId(Identifier id) {
        String path = id.getPath().replace('_', ' ');
        if (path.isEmpty()) return id.toString();
        return Character.toUpperCase(path.charAt(0)) + path.substring(1);
    }
}
