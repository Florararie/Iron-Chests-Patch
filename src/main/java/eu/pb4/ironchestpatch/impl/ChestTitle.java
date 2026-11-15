package eu.pb4.ironchestpatch.impl;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;

/**
 * Replaces the navigation row's slot texture with a plain native chest background.
 *
 * <p>Vanilla renders a container's labels before its slot contents. A custom-font glyph in the
 * title can therefore paint over the vanilla slot backgrounds while the page buttons are still
 * drawn on top as items. The texture is a cropped slice of the vanilla chest background, so the
 * GUI border and panel shading stay pixel-identical. For the six-row layout the glyph also carries
 * the blank cells after the 42nd slot in the last content row.
 */
public final class ChestTitle {
    private static final FontDescription GUI_FONT =
            new FontDescription.Resource(Identifier.fromNamespaceAndPath("ironchestpatch", "gui"));
    private static final Style GUI_STYLE = Style.EMPTY.withColor(0xFFFFFF).withFont(GUI_FONT);

    private ChestTitle() {}

    public static Component decorate(Component title, ChestLayout layout) {
        if (!layout.navigation()) {
            return title;
        }

        char glyph = switch (layout.rows()) {
            case 2 -> 'c';
            case 3 -> 'd';
            case 4 -> 'e';
            case 5 -> 'f';
            default -> 'g';
        };

        // "a" pulls the glyph to the GUI's left edge. The glyph advances 177px (176px of
        // texture plus its one-pixel right bearing), so "b" pulls the title back to x=8.
        return Component.empty()
                .append(Component.literal("a").withStyle(GUI_STYLE))
                .append(Component.literal(String.valueOf(glyph)).withStyle(GUI_STYLE))
                .append(Component.literal("b").withStyle(GUI_STYLE))
                .append(title);
    }
}
