package eu.pb4.ironchestpatch;

import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.fabricmc.api.ModInitializer;
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
        LOGGER.info("Iron Chests Polymer patch loaded: chests, upgrades and keys are now server-side only.");
    }
}
