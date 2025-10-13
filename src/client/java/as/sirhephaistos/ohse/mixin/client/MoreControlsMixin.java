package as.sirhephaistos.ohse.mixin.client;
import as.sirhephaistos.ohse.client.WandClient;
import as.sirhephaistos.ohse.registry.OHSEItems;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.client.Keyboard.class)
public class MoreControlsMixin {
    /**
     * Intercepts keyboard events to handle PageUp/PageDown/Arrows/Delete
     * when holding the OHSE zone wand and no GUI is open.
     *
     * Method signature (Yarn 1.21.1): onKey(JIIII)V
     */
    @Inject(method = "onKey", at = @At("HEAD"), cancellable = true)
    private void ohse$onKey(long window, int key, int scancode, int action, int mods, CallbackInfo ci) {
        // we only care about PRESS/REPEAT (repeat utile pour flèches & PgUp/PgDn)
        if (action != GLFW.GLFW_PRESS && action != GLFW.GLFW_REPEAT) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.currentScreen != null) return;

        var held = mc.player.getMainHandStack();
        if (held.isEmpty() || held.getItem() != OHSEItems.ZONE_WAND) return;

        boolean isRepeat = (action == GLFW.GLFW_REPEAT);

        switch (key) {
            case GLFW.GLFW_KEY_PAGE_UP -> {
                // ex: monter Ymax / layer haut
                WandClient.onKey("PAGE_UP", isRepeat, mods,mc.player);
                ci.cancel();
            }
            case GLFW.GLFW_KEY_PAGE_DOWN -> {
                // ex: descendre Ymax / layer haut
                WandClient.onKey("PAGE_DOWN", isRepeat, mods,mc.player);
                ci.cancel();
            }
            case GLFW.GLFW_KEY_DELETE -> {
                // ex: supprimer le dernier point / sélection
                // (seulement sur PRESS pour éviter spam)
                if (!isRepeat) {
                    WandClient.onKey("DELETE", false, mods,mc.player);
                    ci.cancel();
                }
            }
            case GLFW.GLFW_KEY_UP -> {
                // ex: déplacer le focus sommet +Z / switch outil
                WandClient.onKey("ARROW_UP", isRepeat, mods,mc.player);
                ci.cancel();
            }
            case GLFW.GLFW_KEY_DOWN -> {
                WandClient.onKey("ARROW_DOWN", isRepeat, mods,mc.player);
                ci.cancel();
            }
            case GLFW.GLFW_KEY_LEFT -> {
                WandClient.onKey("ARROW_LEFT", isRepeat, mods,mc.player);
                ci.cancel();
            }
            case GLFW.GLFW_KEY_RIGHT -> {
                WandClient.onKey("ARROW_RIGHT", isRepeat, mods,mc.player);
                ci.cancel();
            }
            default -> { /* ignore */ }
        }
    }
}
