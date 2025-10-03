package as.sirhephaistos.ohse.client;

import as.sirhephaistos.ohse.network.ZoneWandClickPayload;
import as.sirhephaistos.ohse.network.ZoneWandPlaceARefPayload;
import as.sirhephaistos.ohse.network.ZoneWandRemoveARefPayload;
import as.sirhephaistos.ohse.registry.OHSEItems;
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
        initWandPlaceARefHandler();
        initWandRemoveARefHandler();
    }

    private boolean prevLeft, prevRight, prevMiddle;

    private void tick(MinecraftClient client) {
        if (client.player == null || client.world == null) return;

        ItemStack held = client.player.getMainHandStack();
        if (held.isEmpty() || held.getItem() != OHSEItems.ZONE_WAND) return;

        boolean leftNow   = client.options.attackKey.isPressed();
        boolean rightNow  = client.options.useKey.isPressed();
        boolean middleNow = client.options.pickItemKey.isPressed();

        boolean leftClick   = leftNow && !prevLeft;
        boolean rightClick  = rightNow && !prevRight;
        boolean middleClick = middleNow && !prevMiddle;

        prevLeft = leftNow;
        prevRight = rightNow;
        prevMiddle = middleNow;

        if (!(leftClick || rightClick || middleClick)) return;
        if (leftClick && rightClick) return;

        int clickType = middleClick ? 2 : (rightClick ? 1 : 0);

        HitResult hit = client.player.raycast(256.0, 0.0f, false);
        //client.player.sendMessage(Text.of("POS%s".formatted(hit.getPos())), false);
        Optional<BlockPos> posOpt = Optional.empty();
        if (hit.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHitResult = (BlockHitResult) hit;
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

        ClientPlayNetworking.send(new ZoneWandClickPayload(clickType, posOpt));
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
            // TODO: ICI DIRE AU MANAGER D"AJOUTER UNE REF AU RENDERER
            if (MinecraftClient.getInstance().player != null) {
                MinecraftClient.getInstance().player.sendMessage(
                        Text.literal("[OHSE] Received ZoneWandPlaceARefPayload for pos " + x + "," + y + "," + z),
                        false
                );
            }
        }));
    }

    private void initWandRemoveARefHandler() {
        ClientPlayNetworking.registerGlobalReceiver(ZoneWandRemoveARefPayload.ID, (payload, ctx) -> MinecraftClient.getInstance().execute(() -> {
            BlockPos pos = payload.pos();
            // TODO: appeler ton manager pour retirer la ref à 'pos'
            // ex: CubeDebugManager.removeCube(pos);

            var mc = MinecraftClient.getInstance();
            if (mc.player != null) {
                mc.player.sendMessage(Text.literal(
                        "[OHSE] Received ZoneWandRemoveARefPayload for pos " +
                                pos.getX() + "," + pos.getY() + "," + pos.getZ()), false);
            }
        }));
    }
}
