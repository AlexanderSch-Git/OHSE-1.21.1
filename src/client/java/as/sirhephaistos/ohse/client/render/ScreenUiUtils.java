package as.sirhephaistos.ohse.client.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public final class  ScreenUiUtils {
    private ScreenUiUtils() {}

    /* Draws a text on screen at given coordinates.
    *  @param client The Minecraft client instance. (player only)
    *  @param text The text to draw in java string format. (MC Text objects not supported)
    *  @param x X coordinate on screen (pixels)
    *  @param y Y coordinate on screen (pixels)
    *  @param r Red color component (0-255)
    *  @param g Green color component (0-255)
    *  @param b Blue color component (0-255)
    *  @param a Alpha (transparency) component (0-255)
    *  @param shadow Whether to draw a shadow behind the text for better visibility
    *  @param centered Whether to center the text horizontally at the given x coordinate
    *  @param scale Scale factor for the text size (1.0 = normal size)
    *  @param wrapWidth Maximum width in pixels before the text wraps to a new line
    *  @param wrap Whether to enable text wrapping
    *  @param bold Whether to render the text in bold style
    *  @param italic Whether to render the text in italic style
    *  @param underlined Whether to render the text with an underline
    *  @param strikethrough Whether to render the text with a strikethrough
    *  @param obfuscated Whether to render the text with obfuscated characters
    *  @param background Whether to draw a semi-transparent background rectangle behind the text for better visibility
    *  @param backgroundPadding Padding in pixels around the text when drawing the background rectangle
    *  @param backgroundColor Color of the background rectangle in ARGB format (0xAARRGGBB)
    *  @param backgroundAlpha Alpha (transparency) component of the background rectangle (0-255)
    *  @param shadowColor Color of the text shadow in ARGB format (0xAARRGGBB)
    *  @param shadowAlpha
    * */
    public static void drawTextOnScreen(
            @NotNull MinecraftClient mc,
            @NotNull String text,
            int x, int y,
            int r, int g, int b, int a,
            boolean shadow,
            boolean centered,
            float scale,
            int wrapWidth,
            boolean wrap,
            boolean bold,
            boolean italic,
            boolean underlined,
            boolean strikethrough,
            boolean obfuscated,
            boolean background,
            int backgroundPadding,
            int backgroundColor,
            int backgroundAlpha,
            int shadowColor,
            int shadowAlpha
    ) {
        var client = MinecraftClient.getInstance();
        if (client.player == null || client.textRenderer == null) return;
        var textRenderer = client.textRenderer;
        var mcText = Text.literal(text);
        if (mcText.getString().isEmpty()) return; /* Nothing to draw */
        if (bold) mcText = mcText.styled(style -> style.withBold(true));
        if (italic) mcText = mcText.styled(style -> style.withItalic(true));
        if (underlined) mcText = mcText.styled(style -> style.withUnderline(true));
        if (strikethrough) mcText = mcText.styled(style -> style.withStrikethrough(true));
        if (obfuscated) mcText = mcText.styled(style -> style.withObfuscated(true));
        int color = (a << 24) | (r << 16) | (g << 8) | b;
        //for now on ingore rest and place text in top-left corner
        //TODO implement all features
    }
}
