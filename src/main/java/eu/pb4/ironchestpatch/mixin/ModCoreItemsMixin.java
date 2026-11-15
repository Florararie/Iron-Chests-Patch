package eu.pb4.ironchestpatch.mixin;

import com.gathertocraft.ironcore.IronCoreCommon;
import com.gathertocraft.ironcore.registry.ModCoreItems;
import eu.pb4.ironchestpatch.impl.CoreItems;
import eu.pb4.polymer.core.api.item.PolymerItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModCoreItems.class)
public class ModCoreItemsMixin {
    @Inject(method = "registerItems", at = @At("TAIL"))
    private static void polymer$overlayItems(CallbackInfo ci) {
        for (String id : CoreItems.IDS) {
            Item item = BuiltInRegistries.ITEM.getValue(
                    Identifier.fromNamespaceAndPath(IronCoreCommon.MOD_ID, id));
            if (item != null) {
                PolymerItem.registerOverlay(item, CoreItems.create(id));
            }
        }
    }
}
