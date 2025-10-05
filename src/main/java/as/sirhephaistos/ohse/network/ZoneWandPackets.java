package as.sirhephaistos.ohse.network;

import as.sirhephaistos.ohse.zoneSrv.ZManager;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.text.Text;

import static net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.*;

/**
 * This class handles the registration of custom packets for the Zone Wand tool.
 * It defines both client-to-server (C2S) and server-to-client (S2C) packet types
 * and their respective handlers.
 */
@SuppressWarnings("resource") // Suppress resource warning for player because  minecraft handles it NEVER CLOSE NC oups
public class ZoneWandPackets {

    /**
     * Registers all client-to-server (C2S) packets and their handlers.
     * These packets are sent by the client to the server to perform actions
     * such as placing, removing, or interacting with the Zone Wand.
     */
    public static void registerC2SPackets() {
        // Register the ZoneWandClickPayload packet type
        PayloadTypeRegistry.playC2S().register(
                ZoneWandClickPayload.ID,
                ZoneWandClickPayload.CODEC
        );

        registerGlobalReceiver(ZoneWandClickPayload.ID, (payload, ctx) -> {
            var player = ctx.player();
            //warning as here ,now suppressed
            ctx.server().execute(() -> {
                // Validate the payload and send appropriate responses
                if (payload.faceId() < 0 && (payload.clickType() != 2)) {
                    player.sendMessage(Text.literal("[OHSE] Oops no face received"), false);
                    return;
                }
                // Handle different click types
                switch (payload.clickType()) {
                    case 0 -> // Handle left-click: send a ZoneWandRemoveARefPayload to the client
                            send(player, new ZoneWandRemoveARefPayload(payload.hitX(), payload.hitY(), payload.hitZ(), payload.faceId()));
                    case 1 -> // Handle right-click: send a ZoneWandPlaceARefPayload to the client
                        send(player,
                                new ZoneWandPlaceARefPayload(payload.hitX(), payload.hitY(), payload.hitZ(), payload.faceId()));
                    case 2 -> // Handle middle-click: send a ZoneWandMiddleClickPayload to the client
                        send(player, new ZoneWandMiddleClickPayload(payload.hitX(), payload.hitY(), payload.hitZ()));

                    default -> // Handle unknown click types should never happen
                        player.sendMessage(Text.literal("[OHSE] Unknown click type."), false);
                }
            });
        });

        // Register the ZoneWandMiddleScrollCLIENTPayload packet type
        PayloadTypeRegistry.playC2S().register(
                ZoneWandMiddleScrollCLIENTPayload.ID,
                ZoneWandMiddleScrollCLIENTPayload.CODEC
        );

        // Register the handler for ZoneWandMiddleScrollCLIENTPayload
        registerGlobalReceiver(ZoneWandMiddleScrollCLIENTPayload.ID, (payload, ctx) -> {
            var player = ctx.player();
            // warning as here ,now suppressed same as above
            ctx.server().execute(() -> {
                // Handle middle scroll: send the payload back to the client
                int dir = payload.direction();
                send(player, new ZoneWandMiddleScrollCLIENTPayload(dir));
            });
        });



        // ZoneWandPackets.registerC2SPackets()
        PayloadTypeRegistry.playC2S().register(
                ZoneRecuperationClientResponsePayload.ID,
                ZoneRecuperationClientResponsePayload.CODEC
        );
        ZManager
        registerGlobalReceiver(ZoneRecuperationClientResponsePayload.ID, ZManager::createZone);
    }

    /**
     * Registers all server-to-client (S2C) packets.
     * These packets are sent by the server to the client to update the client
     * about actions performed with the Zone Wand.
     */
    public static void registerS2CPackets() {
        // Register the ZoneWandPlaceARefPayload packet type
        PayloadTypeRegistry.playS2C().register(
                ZoneWandPlaceARefPayload.ID,
                ZoneWandPlaceARefPayload.CODEC
        );

        // Register the ZoneWandRemoveARefPayload packet type
        PayloadTypeRegistry.playS2C().register(
                ZoneWandRemoveARefPayload.ID,
                ZoneWandRemoveARefPayload.CODEC
        );

        // Register the ZoneWandMiddleScrollCLIENTPayload packet type
        PayloadTypeRegistry.playS2C().register(
                ZoneWandMiddleScrollCLIENTPayload.ID,
                ZoneWandMiddleScrollCLIENTPayload.CODEC
        );

        // Register the ZoneWandMiddleClickPayload packet type
        PayloadTypeRegistry.playS2C().register(
                ZoneWandMiddleClickPayload.ID,
                ZoneWandMiddleClickPayload.CODEC
        );

        // Register the ZoneWandInitRecuperationPayload packet type
        PayloadTypeRegistry.playS2C().register(
                ZoneWandInitRecuperationPayload.ID,
                ZoneWandInitRecuperationPayload.CODEC
        );
    }

}
