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
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

import static net.minecraft.util.math.Direction.UP;

/**
 * Handles client-side logic for the Zone Wand, including input handling and communication with the server.
 * This class is set as the entry point in the `fabric.mod.json` file.
 */
public class WandClient implements ClientModInitializer {

    /**
     * Initializes the client-side logic for the Zone Wand.
     * Registers event listeners and network handlers.
     */
    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(this::tick);
        initWandScrollHandler();
        initWandPlaceARefHandler();
        initWandRemoveARefHandler();
        initWandMiddleClickHandler();
    }

    // Used as a simple state machine to track scroll direction while buffering multiple scroll events
    private static int pendingScrollDir = 0;

    /**
     * Handles scroll events for the Zone Wand.
     * @param vertical Positive for scroll up, negative for scroll down.
     */
    public static void onScroll(double vertical) {
        if (vertical > 0) pendingScrollDir = +1;
        else if (vertical < 0) pendingScrollDir = -1;
    }

    // Used to track previous button states for edge detection preventing multiple clicks per press
    private boolean prevLeft, prevRight, prevMiddle;
    private static boolean pendingMiddleClick = false;

    /**
     * Sets the pending middle click state.
     * This is useful for triggering middle-click actions programmatically.
     * @param pendingMiddleClick true to simulate a middle click, false otherwise.
     */
    public static void setPendingMiddleClick(boolean pendingMiddleClick) {
        WandClient.pendingMiddleClick = pendingMiddleClick;
    }

    /**
     * Checks if a middle click action is pending.
     * @return true if a middle click action is pending, false otherwise.
     */
    public static boolean isPendingMiddleClick() {
        return pendingMiddleClick;
    }

    // Used to simulate left click actions programmatically
    private static boolean pendingLeftClick = false;

    /**
     * Sets the pending left click state.
     * This is useful for triggering left-click actions programmatically.
     * @param pendingLeftClick true to simulate a left click, false otherwise.
     */
    public static void setPendingLeftClick(boolean pendingLeftClick) {
        WandClient.pendingLeftClick = pendingLeftClick;
    }

    /**
     * Checks if a left click action is pending.
     * @return true if a left click action is pending, false otherwise.
     */
    public static boolean isPendingLeftClick() {
        return pendingLeftClick;
    }

    /**
     * Handles the main logic for the Zone Wand during each client tick.
     * Processes input events and sends appropriate payloads to the server.
     * @param client The Minecraft client instance.
     */
    private void tick(@NotNull MinecraftClient client) {
        if (client.player == null || client.world == null) return;

        // Check if the player is holding the Zone Wand
        ItemStack held = client.player.getMainHandStack();
        if (held.isEmpty() || held.getItem() != OHSEItems.ZONE_WAND) return;

        // Handle input states
        boolean leftNow   = client.options.attackKey.isPressed() || pendingLeftClick;
        pendingLeftClick = false; // reset
        boolean rightNow  = client.options.useKey.isPressed();
        boolean middleNow = client.options.pickItemKey.isPressed() || pendingMiddleClick;
        pendingMiddleClick = false; // reset

        // Detect edge clicks
        boolean leftClick   = leftNow && !prevLeft;
        boolean rightClick  = rightNow && !prevRight;
        boolean middleClick = middleNow && !prevMiddle;

        // Update previous states
        prevLeft = leftNow;
        prevRight = rightNow;
        prevMiddle = middleNow;

        // Handle scroll events
        if (pendingScrollDir != 0) {
            int type = pendingScrollDir;
            pendingScrollDir = 0;
            ClientPlayNetworking.send(new ZoneWandMiddleScrollCLIENTPayload(type));
        }

        // Ignore if no valid click is detected
        if (!(leftClick || rightClick || middleClick)) return;
        if (leftClick && rightClick) return;

        // Determine click type
        int clickType = middleClick ? 2 : (rightClick ? 1 : 0);

        // Perform raycast to determine hit position
        HitResult hit = client.player.raycast(256.0, 0.0f, false);
        Optional<BlockPos> posOpt = Optional.empty();
        int faceId = -1;

        if (hit.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHitResult = (BlockHitResult) hit;
            faceId = blockHitResult.getSide().getId();
            Vec3d pos3d = hit.getPos();
            BlockPos pos = new BlockPos(
                    (int) Math.floor(pos3d.x),
                    (int) Math.floor(pos3d.y),
                    (int) Math.floor(pos3d.z)
            );
            // Adjust position if the hit side is UP
            if (blockHitResult.getSide() == UP) {
                pos = pos.down();
            }
            posOpt = Optional.of(pos);
        }

        // Send click payload to the server
        ClientPlayNetworking.send(new ZoneWandClickPayload(clickType, faceId, posOpt));
    }

    /**
     * Initializes the handler for the Zone Wand "Place ARef" action.
     * Registers a global receiver for the corresponding payload.
     */
    private void initWandPlaceARefHandler() {
        ClientPlayNetworking.registerGlobalReceiver(ZoneWandPlaceARefPayload.ID, (payload, ctx) -> MinecraftClient.getInstance().execute(() -> {
            int x = payload.pos().getX();
            int y = payload.pos().getY();
            int z = payload.pos().getZ();

            if (MinecraftClient.getInstance().player != null) {
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

    /**
     * Initializes the handler for the Zone Wand "Remove ARef" action.
     * Registers a global receiver for the corresponding payload.
     */
    private void initWandRemoveARefHandler() {
        ClientPlayNetworking.registerGlobalReceiver(ZoneWandRemoveARefPayload.ID, (payload, ctx) -> MinecraftClient.getInstance().execute(() -> {
            BlockPos pos = payload.pos();
            var mc = MinecraftClient.getInstance();
            if (mc.player != null) {
                ZoneManager.removeLastRef();
            }
        }));
    }

    /**
     * Initializes the handler for the Zone Wand scroll action.
     * Registers a global receiver for the corresponding payload.
     */
    private void initWandScrollHandler() {
        ClientPlayNetworking.registerGlobalReceiver(ZoneWandMiddleScrollCLIENTPayload.ID, (payload, ctx) -> MinecraftClient.getInstance().execute(() -> {
            int dir = payload.direction();
            var mc = MinecraftClient.getInstance();
            if (ZoneManager.isEditingPositiveY()) {
                var oldLargestY = ZoneManager.getLargestY();
                if (oldLargestY + dir < ZoneManager.getSmallestY()) {
                    assert mc.player != null;
                    mc.player.sendMessage(Text.literal("[OHSE] Cannot set Top Y below Down Y"), false);
                    return;
                }
                ZoneManager.setLargestY(oldLargestY + dir);
            } else {
                var oldSmallestY = ZoneManager.getSmallestY();
                if (oldSmallestY + dir > ZoneManager.getLargestY()) {
                    assert mc.player != null;
                    mc.player.sendMessage(Text.literal("[OHSE] Cannot set Down Y above Top Y"), false);
                    return;
                }
                ZoneManager.setSmallestY(oldSmallestY + dir);
            }
        }));
    }

    /**
     * Initializes the handler for the Zone Wand middle click action.
     * Registers a global receiver for the corresponding payload.
     */
    private void initWandMiddleClickHandler() {
        ClientPlayNetworking.registerGlobalReceiver(ZoneWandMiddleClickPayload.ID, (payload, ctx) -> MinecraftClient.getInstance().execute(() -> {
            var mc = MinecraftClient.getInstance();
            if (mc.player != null) {
                ZoneManager.setEditingPositiveY(!ZoneManager.isEditingPositiveY());
                mc.player.sendMessage(Text.literal("[OHSE] Now editing " + (ZoneManager.isEditingPositiveY() ? "Top" : "Down") + " Y"), false);
            }
        }));
    }
}
