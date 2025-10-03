package as.sirhephaistos.ohse.mixin.client;

import as.sirhephaistos.ohse.client.WandClient;
import as.sirhephaistos.ohse.registry.OHSEItems;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.item.ItemStack;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class MouseButtonMixin {
    @Inject(method = "onMouseButton", at = @At("HEAD"), cancellable = true)
    private void ohse$onMouseButton(long window, int button, int action, int mods, CallbackInfo ci) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE && action == GLFW.GLFW_PRESS) {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player != null && mc.currentScreen == null) {
                var held = mc.player.getMainHandStack();
                if (!held.isEmpty() && held.getItem() == OHSEItems.ZONE_WAND) {
                    // bloque le pick block vanilla
                    ci.cancel();

                    // ➜ simule un "middle click pressed"
                    as.sirhephaistos.ohse.client.WandClient.setPendingMiddleClick(true);
                }
            }
        }// same but with left click (attack)
        else if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && action == GLFW.GLFW_PRESS) {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player != null && mc.currentScreen == null) {
                var held = mc.player.getMainHandStack();
                if (!held.isEmpty() && held.getItem() == OHSEItems.ZONE_WAND) {
                    // bloque le left click vanilla
                    ci.cancel();
                    as.sirhephaistos.ohse.client.WandClient.setPendingLeftClick(true);
                }
            }
        }
    }
}

