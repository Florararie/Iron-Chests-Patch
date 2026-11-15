package eu.pb4.ironchestpatch.impl;

import eu.pb4.polymer.core.api.item.PolymerCreativeModeTabUtils;
import eu.pb4.polymer.core.api.utils.PolymerSyncedObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.CreativeModeTab;

/**
 * Keeps mod creative tabs off the vanilla-client tab list while still exposing them to clients that
 * speak Polymer.
 */
public final class PolymerTabs {
    private PolymerTabs() {}

    public static void hideAndServe(CreativeModeTab tab) {
        if (tab == null) {
            return;
        }

        // Never send the unknown tab as a vanilla registry entry.
        PolymerSyncedObject.setSyncedObject(BuiltInRegistries.CREATIVE_MODE_TAB, tab, (object, context) -> null);

        // Polymer clients still get the tab and its contents.
        PolymerCreativeModeTabUtils.LIST_EVENT.register((player, builder) -> builder.add(tab));
    }
}
