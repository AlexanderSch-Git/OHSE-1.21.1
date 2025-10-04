package as.sirhephaistos.ohse.client;

import as.sirhephaistos.ohse.client.utilityClasses.BetterPosition;
import as.sirhephaistos.ohse.config.OHSEConfig;
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
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

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
    public static void onScroll(double vertical, boolean isCtrlDown,boolean isShiftDown) {
        if (vertical > 0) pendingScrollDir =  1;
        else if (vertical < 0) pendingScrollDir = -1;
        pendingScrollDir = isCtrlDown ? pendingScrollDir *  OHSEConfig.get().ctrlMultiplier: pendingScrollDir;
        pendingScrollDir = isShiftDown ? pendingScrollDir * OHSEConfig.get().shiftMultiplier: pendingScrollDir;
    }

    // Used to simulate clicks actions programmatically
    private static boolean[] pendingClicks = new boolean[3];
    /**
     * Sets the pending (button) click state.
     * This is useful for triggering left-click actions programmatically.
     * @param button 0 for left, 1 for right, 2 for middle.
     * @param state true to simulate a  click, false otherwise.
     */
    public static void setPendingClick(int button, boolean state) {
        pendingClicks[button] = state;
    }
    /** Checks if a  (button) click action is pending.
     * @param button 0 for left, 1 for right, 2 for middle.
     * @return true if a  (button) click action is pending, false otherwise.
     */
    @SuppressWarnings("unused")
    public static boolean isPendingClick(int button) {
        return pendingClicks[button];
    }
    /**
     * Resets all pending (button) click states to false.
     */
    public static void resetPendingClicks() {
        pendingClicks = new boolean[3];
    }
    /**
     * Checks if exactly one (button) click action is pending.
     * @return The index of the pending click (0 for left, 1 for right, 2 for middle), or -1 if none or multiple are pending.
     */
    public static int getSingleActiveClick() {
        int index = -1, count = 0;
        for (int i = 0; i < pendingClicks.length; i++) {
            if (pendingClicks[i]) {
                index = i;
                count++;
            }
        }
        return count == 1 ? index : -1;
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

        // Handle scroll events
        if (pendingScrollDir != 0) {
            int type = pendingScrollDir;
            pendingScrollDir = 0;
            ClientPlayNetworking.send(new ZoneWandMiddleScrollCLIENTPayload(type));
            return; // Only process one action per tick
        }

        int clickType = getSingleActiveClick();
        resetPendingClicks();
        if (clickType == -1) {
            return;
        }

        // Perform raycast to determine hit position
        HitResult hit = client.player.raycast(OHSEConfig.get().maxRaycastDistance,0.0f, false);
        if (hit.getType() == HitResult.Type.BLOCK) {
            BlockHitResult bhr = (BlockHitResult) hit;
            // Send click payload to the server
            Vec3d pos3d = ((BlockHitResult) hit).getBlockPos().toCenterPos(); // better use the exact hit position for more precision
            ClientPlayNetworking.send(new ZoneWandClickPayload(clickType,bhr.getSide().getId(), pos3d.getX(), pos3d.getY(), pos3d.getZ()));
        }else if (clickType == 2) {
            // Middle click with no block hit toggles Y editing mode
            ClientPlayNetworking.send(new ZoneWandClickPayload(clickType, -1, 0, 0, 0));
        }
    }

    /**
     * Initializes the handler for the Zone Wand "Place ARef" action.
     * Registers a global receiver for the corresponding payload.
     */
    private void initWandPlaceARefHandler() {
        ClientPlayNetworking.registerGlobalReceiver(ZoneWandPlaceARefPayload.ID, (payload, ctx) -> MinecraftClient.getInstance().execute(() -> {
            if (MinecraftClient.getInstance().player != null) {
                try {
                    ZoneManager.addRef(new BetterPosition(payload.hitX(), payload.hitY(), payload.hitZ(),payload.faceId()));
                    ZoneManager.setAllYtoSmallestY();
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
            if (MinecraftClient.getInstance().player != null) {
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
            var mc = MinecraftClient.getInstance();assert mc.player != null;
            int dir = payload.direction();
            if (ZoneManager.positionIsEmpty()) {
                mc.player.sendMessage(Text.literal("[OHSE] No position defined yet."), false);
                return;
            }
            if (ZoneManager.positionsSize()<3) {
                mc.player.sendMessage(Text.literal("[OHSE] Cannot edit Y when more less 3 positions are defined."), false);
                return;}
            if (ZoneManager.isEditingPositiveY()) {
                int newOffset = ZoneManager.getYOffset() + dir;
                if (ZoneManager.getSmallestY()+ newOffset < ZoneManager.getSmallestY()) {
                    mc.player.sendMessage(Text.literal("[OHSE] Cannot set Top Y below Down Y"), false);
                    return;
                }
                ZoneManager.setYOffset(newOffset);
            } else {
                double newSmallestY = ZoneManager.getSmallestY()+ dir;
                if (ZoneManager.getYOffset() < 0) ZoneManager.setYOffset(0);
                if (newSmallestY > ZoneManager.getSmallestY()) {
                    if (ZoneManager.getYOffset() == 0) {
                        ZoneManager.setEditingPositiveY(true);
                        mc.player.sendMessage(Text.literal("[OHSE] Zone collapsed, switching to Top Y editing"), false);
                        return;
                    }
                    ZoneManager.setYOffset(ZoneManager.getYOffset()  - dir);
                }
                if (newSmallestY < -64) {
                    mc.player.sendMessage(Text.literal("[OHSE] Cannot set Down Y above Top Y"), false);
                    return;
                }
                if (newSmallestY > ZoneManager.getSmallestY() + ZoneManager.getYOffset()+1) {
                    mc.player.sendMessage(Text.literal("[OHSE] Cannot set Down Y above top Y"), false);
                    return;
                }
                if(ZoneManager.getSmallestY() == 0){
                    ZoneManager.setEditingPositiveY(true);
                    mc.player.sendMessage(Text.literal("[OHSE] Zone collapsed, switching to Top Y editing"), false);
                    return;
                }
                ZoneManager.setSmallestY(newSmallestY);
                ZoneManager.setAllYtoSmallestY();
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
