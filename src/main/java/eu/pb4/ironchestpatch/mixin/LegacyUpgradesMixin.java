package eu.pb4.ironchestpatch.mixin;

import com.gathertocraft.ironchest.IronChestsCommon;
import com.gathertocraft.ironchest.legacy.LegacyUpgrades;
import eu.pb4.ironchestpatch.impl.LegacyUpgradeClient;
import eu.pb4.polymer.core.api.item.PolymerItem;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LegacyUpgrades.class)
public class LegacyUpgradesMixin {
    @Inject(method = "registerAll", at = @At("TAIL"))
    private static void polymer$overlayLegacyUpgrades(CallbackInfo ci) {
        for (Identifier id : List.copyOf(BuiltInRegistries.ITEM.keySet())) {
            if (!id.getNamespace().equals(IronChestsCommon.MOD_ID) || !id.getPath().endsWith("_upgrade")) {
                continue;
            }
            Item item = BuiltInRegistries.ITEM.getValue(id);
            if (item != null) {
                PolymerItem.registerOverlay(item, LegacyUpgradeClient.INSTANCE);
            }
        }
    }
}
