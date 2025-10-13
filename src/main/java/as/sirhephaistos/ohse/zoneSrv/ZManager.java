package as.sirhephaistos.ohse.zoneSrv;

import as.sirhephaistos.ohse.db.DbExecutor;
import as.sirhephaistos.ohse.db.ZoneRepository;
import as.sirhephaistos.ohse.network.ZoneRecuperationClientResponsePayload;
import as.sirhephaistos.ohse.zoneSrv.utils.ZUtility;
import lombok.Getter;
import lombok.Setter;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.Context;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.ChunkPos;

import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

@SuppressWarnings("unused")
public final class ZManager {
    private static final ZManager INSTANCE = new ZManager();

    private ZManager() {
    }

    public static ZManager getInstance() {
        return INSTANCE;
    }

    public void createZone(ZoneRecuperationClientResponsePayload payload, Context ctx) {
        ServerPlayerEntity player = ctx.player();
        if (player.getServer() == null) {
            player.sendMessage(Text.literal("[OHSE] Oops no server nani"), false);
            return;
        }
        // Permission gate: LuckPerms "ohse-admin" OR OP level >= 3
        if (!Permissions.check(player, "ohse-admin", 3)) {
            player.sendMessage(Text.literal("[OHSE] Permission denied"), false);
            return;
        }

        // 1) Compute chunk coverage from polygon
        double[] aabb = ZUtility.getCorners(payload.positions());
        Set<ChunkPos> chunks = ZUtility.getChunksInPolygon(payload.positions(), aabb);
        if (chunks.isEmpty()) {
            player.sendMessage(Text.literal("[OHSE] Oops no chunks found"), false);
            return;
        }
        // 2) Prepare zone data
        UUID zoneId = UUID.randomUUID();
        String name = payload.zoneName();
        String creator = player.getName().getString();
        double yMin = payload.yMin();
        double yMax = payload.yMax();
        String world = player.getServerWorld().getRegistryKey().getValue().toString();
        // 3) Feedback
        player.sendMessage(Text.literal("[OHSE] Saving zone \"" + name + "\" (" + chunks.size() + " chunks)…"), false);
        // 4) Persist asynchronously (no blocking on the main server thread)
        DbExecutor.SINGLE.submit(() -> {
            try {
                ZoneRepository.saveZoneWithChunks(
                        zoneId,
                        name,
                        creator,
                        world,
                        yMin,
                        yMax,
                        chunks
                );
                // Bounce feedback back to the main thread
                player.getServer().execute(() ->
                        player.sendMessage(Text.literal("[OHSE] Zone \"" + name + "\" saved ✓"), false)
                );
            } catch (Exception e) {
                player.getServer().execute(() ->
                        player.sendMessage(Text.literal("[OHSE] DB error while saving zone: " + e.getMessage()), false)
                );
            }
        });
    }
}
