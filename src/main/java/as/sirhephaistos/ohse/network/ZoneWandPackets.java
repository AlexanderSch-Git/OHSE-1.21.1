package as.sirhephaistos.ohse.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.text.MessageFormat;


public class ZoneWandPackets {

    public static void registerC2SPackets() {
        //System.out.println("Registering ZoneWandPackets C2S");
        // 1) Enregistre le codec
        PayloadTypeRegistry.playC2S().register(
                ZoneWandClickPayload.ID,
                ZoneWandClickPayload.CODEC
        );

        // 2) Handler serveur
        ServerPlayNetworking.registerGlobalReceiver(ZoneWandClickPayload.ID, (payload, ctx) -> {
            //System.out.println("[OHSE]Received ZoneWandClickPayload.");
            var player = ctx.player();
            ctx.server().execute(() -> {
                int t = payload.clickType();
                String typeName = switch (t) {
                    case 0 -> "LEFT";
                    case 1 -> "RIGHT";
                    case 2 -> "MIDDLE";
                    default -> "UNKNOWN";
                };

                if (payload.pos().isEmpty()) return;

                BlockPos pos = payload.pos().get();
                var state = player.getWorld().getBlockState(pos);
                String name = state.getBlock().getName().getString();

                player.sendMessage(
                        Text.literal(MessageFormat.format("[OHSE] Registered action{0} on {1} @ {2}", typeName, name, pos.toShortString())),
                        false
                );
                switch (typeName) {
                    case "LEFT" -> {
                        ServerPlayNetworking.send(player, new ZoneWandRemoveARefPayload(pos));
                    }

                    case "RIGHT" -> {
                        ServerPlayNetworking.send(player,
                                new ZoneWandPlaceARefPayload(pos,1.0F,1.0F,1.0F,0.25F));
                    }
                    case "MIDDLE" -> {
                        // middle click
                    }
                    default -> {
                        player.sendMessage(Text.literal("[OHSE] Unknown click type."), false);
                    }
                }
            });
        });
    }

    public static void registerS2CPackets() {
        // 2 ) Enregistre le codec
        PayloadTypeRegistry.playS2C().register(
                ZoneWandPlaceARefPayload.ID,
                ZoneWandPlaceARefPayload.CODEC
        );
        PayloadTypeRegistry.playS2C().register(
                ZoneWandRemoveARefPayload.ID,
                ZoneWandRemoveARefPayload.CODEC
        );
        // doit on faire un handler ou il va cote client plutot ?
    }

}
