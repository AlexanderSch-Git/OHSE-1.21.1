package as.sirhephaistos.ohse.client;

import as.sirhephaistos.ohse.client.utilityClasses.BetterPosition;
import as.sirhephaistos.ohse.network.*;
import as.sirhephaistos.ohse.registry.OHSEItems;
import as.sirhephaistos.ohse.zone.ZoneManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.Optional;

import static net.minecraft.util.math.Direction.UP;

public class WandClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(this::tick);
        initWandScrollHandler();
        initWandPlaceARefHandler();
        initWandRemoveARefHandler();
        initWandMiddleClickHandler();
    }
    private static int pendingScrollDir = 0;
    public static void onScroll(double vertical) {
        if (vertical > 0) pendingScrollDir = +1;
        else if (vertical < 0) pendingScrollDir = -1;
    }

    private boolean prevLeft, prevRight, prevMiddle;

    private static boolean pendingMiddleClick = false;
    public static void setPendingMiddleClick(boolean pendingMiddleClick) {
        WandClient.pendingMiddleClick = pendingMiddleClick;
    }
    public static boolean isPendingMiddleClick() {
        return pendingMiddleClick;
    }

    private static boolean pendingLeftClick = false;
    public static void setPendingLeftClick(boolean pendingLeftClick) {
        WandClient.pendingLeftClick = pendingLeftClick;
    }
    public static boolean isPendingLeftClick() {
        return pendingLeftClick;
    }

    private void tick(MinecraftClient client) {
        if (client.player == null || client.world == null) return;

        ItemStack held = client.player.getMainHandStack();
        if (held.isEmpty() || held.getItem() != OHSEItems.ZONE_WAND) return;

        boolean leftNow   = client.options.attackKey.isPressed() || pendingLeftClick;
        pendingLeftClick = false; // reset
        boolean rightNow  = client.options.useKey.isPressed();
        boolean middleNow = client.options.pickItemKey.isPressed() || pendingMiddleClick;
        pendingMiddleClick = false; // reset

        boolean leftClick   = leftNow && !prevLeft;
        boolean rightClick  = rightNow && !prevRight;
        boolean middleClick = middleNow && !prevMiddle;

        prevLeft = leftNow;
        prevRight = rightNow;
        prevMiddle = middleNow;

        if (pendingScrollDir != 0) {
            int type = pendingScrollDir ;
            pendingScrollDir = 0;
            //client.player.sendMessage(Text.literal("[OHSE] Scroll Dir: " + type), false);
            ClientPlayNetworking.send(new ZoneWandMiddleScrollCLIENTPayload(type));
        }

        if (!(leftClick || rightClick || middleClick)) return;
        if (leftClick && rightClick) return;

        int clickType = middleClick ? 2 : (rightClick ? 1 : 0);

        HitResult hit = client.player.raycast(256.0, 0.0f, false);
        //client.player.sendMessage(Text.of("POS%s".formatted(hit.getPos())), false);
        Optional<BlockPos> posOpt = Optional.empty();
        int faceId = -1;
        //System.out.println("Hit face: " + faceId);
        if (hit.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHitResult = (BlockHitResult) hit;
            faceId = blockHitResult .getSide().getId();
            //client.player.sendMessage(Text.of("BLOCK%s".formatted(blockHitResult.getSide())), false);
            Vec3d pos3d = hit.getPos();
            BlockPos pos = new BlockPos(
                    (int)Math.floor(pos3d.x),
                    (int)Math.floor(pos3d.y),
                    (int)Math.floor(pos3d.z)
            );
            // If the hit side is UP, we want the block below the hit position
            if (blockHitResult.getSide() == UP) {
                pos = pos.down();
            }
            posOpt = Optional.of(pos);
        }

        //System.out.println("Hit face: " + faceId);

        ClientPlayNetworking.send(new ZoneWandClickPayload(clickType,faceId, posOpt));
        //System.out.println("[OHSE] Sent wand click type=" + clickType + " pos=" + posOpt);
    }
    private void initWandPlaceARefHandler(){
        ClientPlayNetworking.registerGlobalReceiver(ZoneWandPlaceARefPayload.ID, (payload, ctx) -> MinecraftClient.getInstance().execute(() -> {
            int x = payload.pos().getX();
            int y = payload.pos().getY();
            int z = payload.pos().getZ();
            float red = payload.red();
            float green = payload.green();
            float blue = payload.blue();
            float alpha = payload.alpha();
            // Add a debug cube at the given position
            // For now, just print a message
            if (MinecraftClient.getInstance().player != null) {
//                MinecraftClient.getInstance().player.sendMessage(
//                        Text.literal("[OHSE] Received ZoneWandPlaceARefPayload for pos " + x + "," + y + "," + z),
//                        false
//                );
                try {
                    ZoneManager.addRef(new BetterPosition(x, y, z, payload.faceId()));
                } catch (IllegalArgumentException e) {
                    MinecraftClient.getInstance().player.sendMessage(
                            Text.literal("[OHSE] Position already exists (ignoring Y)"),
                            false
                    );
                }
            }
        }));
    }
    private void initWandRemoveARefHandler() {
        ClientPlayNetworking.registerGlobalReceiver(ZoneWandRemoveARefPayload.ID, (payload, ctx) -> MinecraftClient.getInstance().execute(() -> {
            BlockPos pos = payload.pos();
            // ex: CubeDebugManager.removeCube(pos);

            var mc = MinecraftClient.getInstance();
            if (mc.player != null) {
//                mc.player.sendMessage(Text.literal(
//                        "[OHSE] Received ZoneWandRemoveARefPayload for pos " +
//                                pos.getX() + "," + pos.getY() + "," + pos.getZ()), false);
                ZoneManager.removeLastRef();
            }
        }));
    }
    private void initWandScrollHandler() {
        ClientPlayNetworking.registerGlobalReceiver(ZoneWandMiddleScrollCLIENTPayload.ID, (payload, ctx) -> MinecraftClient.getInstance().execute(() -> {
            int dir = payload.direction();
            var mc = MinecraftClient.getInstance();
//            if (mc.player != null) {
//                mc.player.sendMessage(Text.literal("[OHSE] Received ZoneWandMiddleScrollCLIENTPayload with dir " + dir), false);
//            }
            if (ZoneManager.isEditingPositiveY()){
                ZoneManager.setLargestY(ZoneManager.getLargestY()+dir);
            }else{
                ZoneManager.setSmallestY(ZoneManager.getSmallestY()+dir);
                ZoneManager.setAllYtoSmallestY();
            }
        }));
    }
    private void initWandMiddleClickHandler() {
        ClientPlayNetworking.registerGlobalReceiver(ZoneWandMiddleClickPayload.ID, (payload, ctx) -> MinecraftClient.getInstance().execute(() -> {
            var mc = MinecraftClient.getInstance();
            if (mc.player != null) {
//                mc.player.sendMessage(Text.literal("[OHSE] Received ZoneWandMiddleClickPayload"), false);
                ZoneManager.setEditingPositiveY(!ZoneManager.isEditingPositiveY());
                            mc.player.sendMessage(Text.literal("[OHSE] Now editing " + (ZoneManager.isEditingPositiveY() ? "Top" : "Down") + " Y"), false);
            }
        }));
    }
}
