package as.sirhephaistos.ohse.mixin.client;

import as.sirhephaistos.ohse.registry.OHSEItems;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * A mixin class to modify the behavior of the Mouse class in Minecraft.
 * Specifically intercepts mouse button events to implement custom functionality
 * when the player is holding a specific item (the wand).
 */
@Mixin(Mouse.class)
public class MouseButtonMixin {

    /**
     * Intercepts the `onMouseButton` method to add custom behavior for mouse button events.
     * Cancels the default behavior if specific conditions are met.
     *
     * @param window The window handle.
     * @param button The mouse button that was pressed or released.
     * @param action The action performed (press or release).
     * @param mods   Modifier keys pressed during the event.
     * @param ci     The callback information to control method execution.
     */
    @Inject(method = "onMouseButton", at = @At("HEAD"), cancellable = true)
    private void ohse$onMouseButton(long window, int button, int action, int mods, CallbackInfo ci) {
        // Handle middle mouse button press
        if (button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE && action == GLFW.GLFW_PRESS) {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player != null && mc.currentScreen == null) {
                var held = mc.player.getMainHandStack();
                var main = mc.player.getMainHandStack();
                var off  = mc.player.getOffHandStack();
                System.out.println("OHSE mouse button=" + button +
                        " target=" + mc.crosshairTarget +
                        " mainHasWand=" + (!main.isEmpty() && main.getItem() == OHSEItems.ZONE_WAND) +
                        " offHasWand=" + (!off.isEmpty() && off.getItem() == OHSEItems.ZONE_WAND));
                if (!held.isEmpty() && held.getItem() == OHSEItems.ZONE_WAND) {
                    ci.cancel();
                    as.sirhephaistos.ohse.client.WandClient.setPendingClick(2, true);
                }
            }
        }
        // Handle left mouse button press
        else if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && action == GLFW.GLFW_PRESS) {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player != null && mc.currentScreen == null) {
                var held = mc.player.getMainHandStack();
                if (!held.isEmpty() && held.getItem() == OHSEItems.ZONE_WAND) {
                    ci.cancel();
                    as.sirhephaistos.ohse.client.WandClient.setPendingClick(0, true);
                }
            }
        }
        // Handle right mouse button press
        else if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT && action == GLFW.GLFW_PRESS) {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player != null && mc.currentScreen == null) {
                var held = mc.player.getMainHandStack();
                if (!held.isEmpty() && held.getItem() == OHSEItems.ZONE_WAND) {
                    ci.cancel();
                    as.sirhephaistos.ohse.client.WandClient.setPendingClick(1, true);
                }
            }
        }
    }
}
