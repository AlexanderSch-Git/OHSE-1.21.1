package as.sirhephaistos.ohse.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.lwjgl.glfw.GLFW;
import net.minecraft.client.util.InputUtil;

/**
 * A mixin class to modify the behavior of the Mouse class in Minecraft.
 * Specifically intercepts mouse scroll events to implement custom functionality
 * when the player is holding a specific item (the wand).
 */
@Mixin(Mouse.class)
public class MouseScrollMixin {

    /**
     * Intercepts the `onMouseScroll` method to add custom behavior for mouse scroll events.
     * Cancels the default behavior if specific conditions are met.
     *
     * @param window     The window handle.
     * @param horizontal The horizontal scroll amount.
     * @param vertical   The vertical scroll amount.
     * @param ci         The callback information to control method execution.
     */
    @Inject(method = "onMouseScroll", at = @At("HEAD"), cancellable = true)
    private void ohse$onMouseScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;     // Not in-game
        if (mc.currentScreen != null) return;                 // Allow scrolling in GUIs
        if (!isHoldingWand(mc)) return;                       // Wand not in hand
        if (vertical == 0.0) return;

        // Detect if Ctrl or Shift is held
        long win = mc.getWindow().getHandle();
        boolean isCtrlDown  = InputUtil.isKeyPressed(win, GLFW.GLFW_KEY_LEFT_CONTROL)
                || InputUtil.isKeyPressed(win, GLFW.GLFW_KEY_RIGHT_CONTROL);
        boolean isShiftDown = InputUtil.isKeyPressed(win, GLFW.GLFW_KEY_LEFT_SHIFT)
                || InputUtil.isKeyPressed(win, GLFW.GLFW_KEY_RIGHT_SHIFT);

        // Custom hook to handle scroll payload
        as.sirhephaistos.ohse.client.WandClient.onScroll(vertical, isCtrlDown, isShiftDown);

        // Cancel vanilla behavior (e.g., slot change, spectator zoom)
        ci.cancel();
    }

    /**
     * Checks if the player is holding the wand item in either hand.
     *
     * @param mc The Minecraft client instance.
     * @return True if the wand is in the main or off-hand, false otherwise.
     */
    @Unique
    private static boolean isHoldingWand(MinecraftClient mc) {
        assert mc.player != null;
        var main = mc.player.getMainHandStack();
        var off  = mc.player.getOffHandStack();
        return (!main.isEmpty() && main.getItem() == as.sirhephaistos.ohse.registry.OHSEItems.ZONE_WAND)
                || (!off.isEmpty()  && off.getItem()  == as.sirhephaistos.ohse.registry.OHSEItems.ZONE_WAND);
    }
}
