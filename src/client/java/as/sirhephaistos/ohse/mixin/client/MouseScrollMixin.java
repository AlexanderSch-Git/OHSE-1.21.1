package as.sirhephaistos.ohse.mixin.client;

import as.sirhephaistos.ohse.client.WandClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class MouseScrollMixin {

    @Inject(method = "onMouseScroll", at = @At("HEAD"), cancellable = true)
    private void ohse$onMouseScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;     // pas en jeu
        if (mc.currentScreen != null) return;                   // laisser les GUIs scroller
        if (!isHoldingWand(mc)) return;                         // pas la wand en main
        if (vertical == 0.0) return;

        // ton hook (pour envoyer payload scroll)
        as.sirhephaistos.ohse.client.WandClient.onScroll(vertical);

        // ⛔ bloque le comportement vanilla (changement de slot, zoom spectator, etc.)
        ci.cancel();
    }

    @Unique
    private static boolean isHoldingWand(MinecraftClient mc) {
        assert mc.player != null;
        var main = mc.player.getMainHandStack();
        var off  = mc.player.getOffHandStack();
        return (!main.isEmpty() && main.getItem() == as.sirhephaistos.ohse.registry.OHSEItems.ZONE_WAND)
                || (!off.isEmpty()  && off.getItem()  == as.sirhephaistos.ohse.registry.OHSEItems.ZONE_WAND);
    }
}

