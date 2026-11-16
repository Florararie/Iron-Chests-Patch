package eu.pb4.ironchestpatch.impl;

import eu.pb4.polymer.core.impl.InternalServerRegistry;
import eu.pb4.polymer.core.mixin.CreativeModeTabAccessor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Makes the mod's creative tabs reach clients that do not have the mod.
 *
 * <p>Polymer iterates every registered tab when it builds the tab packet, but it only sends the
 * <em>definition</em> - the packet that makes the client create the tab at all - for tabs it knows
 * about in {@code PolymerCreativeModeTabUtils.REGISTRY}. A mod's own tab is in
 * {@code BuiltInRegistries.CREATIVE_MODE_TAB}, not in that registry, so Polymer sends its contents and
 * skips its definition; the client then drops the contents because it has no such tab. Registering is
 * therefore required, and this is what a tab without the mod appears to be missing.
 *
 * <p>Vanilla {@code registerPolymerCreativeModeTab} refuses any id the vanilla registry already
 * contains, so registration goes through the registry it wraps. Keeping the tab's real id matters for
 * modded clients: the client creates a serving-side tab only when its own registry has no entry for
 * that id, so a client with the mod keeps using the mod's own tab instead of growing a duplicate.
 *
 * <p>The icon still needs replacing. Polymer writes {@code group.getIconItem()} into the definition
 * packet with the plain item stack codec, and a mod item - here a chest block item - is not something a
 * client without the mod can resolve, so it decodes to nothing and the tab button comes out blank.
 */
public final class PolymerTabs {
    private static final Logger LOGGER = LoggerFactory.getLogger("ironchest-polymer-patch/tabs");
    private static final List<CreativeModeTab> TABS = new ArrayList<>();

    private PolymerTabs() {
    }

    /**
     * Serves one of the mod's tabs to Polymer clients. Called right after the mod registers it.
     */
    public static void track(CreativeModeTab tab) {
        TABS.add(tab);

        var key = BuiltInRegistries.CREATIVE_MODE_TAB.getResourceKey(tab).orElse(null);
        if (key == null) {
            LOGGER.warn("Creative tab {} is not registered, so it cannot be served", tab.getDisplayName().getString());
            return;
        }

        InternalServerRegistry.ITEM_GROUPS.set(key.identifier(), tab);
        LOGGER.info("Serving creative tab {} to Polymer clients", key.identifier());
    }

    /**
     * Replaces every tab icon a client without the mod cannot decode.
     *
     * <p>Runs on {@code SERVER_STARTED}: item components are not bound during mod initialisation, and
     * reading an item's registry id before that throws.
     */
    public static void applyClientIcons() {
        for (var tab : TABS) {
            var current = tab.getIconItem();
            if (current.isEmpty() || isClientSafe(current)) {
                continue;
            }

            var replacement = clientIcon(current);
            ((CreativeModeTabAccessor) tab).setIconItemStack(replacement);
            LOGGER.info("Creative tab icon {} -> {}", BuiltInRegistries.ITEM.getKey(current.getItem()),
                    BuiltInRegistries.ITEM.getKey(replacement.getItem()));
        }
    }

    private static boolean isClientSafe(ItemStack stack) {
        var id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return id == null || id.getNamespace().equals("minecraft");
    }

    private static ItemStack clientIcon(ItemStack stack) {
        var id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return switch (id == null ? "" : id.getNamespace()) {
            case "ironchest" -> new ItemStack(Items.CHEST);
            case "ironcore" -> new ItemStack(Items.IRON_INGOT);
            default -> stack;
        };
    }
}
