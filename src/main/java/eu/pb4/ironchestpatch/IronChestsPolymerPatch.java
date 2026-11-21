package eu.pb4.ironchestpatch;

import com.gathertocraft.ironchest.IronChestsCommon;
import com.gathertocraft.ironcore.IronCoreCommon;
import eu.pb4.ironchestpatch.impl.PolymerTabs;
import eu.pb4.ironchestpatch.impl.VanillaCopperChests;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class IronChestsPolymerPatch implements ModInitializer {
    public static final String MOD_ID = "ironchest-polymer-patch";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        // Chest models and screen art ship inside this jar, so Polymer adds them to the server pack
        // it generates instead of asking players to install a separate one.
        PolymerResourcePackUtils.addModAssets(MOD_ID);

        // The patched mods' own assets have to go in too. Their language files name the chests, the
        // creative tabs and the advancements, which is what turns "block.ironchest.diamond_chest"
        // back into "Diamond Chest"; their item models are what the client uses for the inventory
        // and advancement icons. Without these a vanilla client has the ids but none of the art.
        if (!PolymerResourcePackUtils.addModAssets(IronChestsCommon.MOD_ID)) {
            LOGGER.warn("Iron Chests assets were not found on the classpath; names and icons will be missing from the pack.");
        }
        if (!PolymerResourcePackUtils.addModAssets(IronCoreCommon.MOD_ID)) {
            LOGGER.warn("Iron Core Utilities assets were not found on the classpath; names and icons will be missing from the pack.");
        }


        // The chests themselves are display entities; this is what lets them stand on a
        // chest-shaped vanilla block without the client drawing a chest on top of them.
        PolymerResourcePackUtils.RESOURCE_PACK_CREATION_EVENT.register(VanillaCopperChests::write);
        VanillaCopperChests.register();

        // A tab icon is an item stack, and item components only exist once the registries are
        // bound, so the client-side icons are swapped in when the server starts.
        ServerLifecycleEvents.SERVER_STARTED.register(server -> PolymerTabs.applyClientIcons());

        LOGGER.info("Iron Chests Polymer patch loaded: chests, upgrades and keys are now server-side only.");
    }
}
