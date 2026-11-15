package eu.pb4.ironchestpatch.mixin;

import com.gathertocraft.ironchest.IronChestsCommon;
import com.gathertocraft.ironchest.blocks.ChestTypes;
import com.gathertocraft.ironchest.registry.ModItems;
import eu.pb4.ironchestpatch.impl.ChestItems;
import eu.pb4.polymer.core.api.item.PolymerItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModItems.class)
public class ModItemsMixin {
    @Inject(method = "registerItems", at = @At("TAIL"))
    private static void polymer$overlayItems(CallbackInfo ci) {
        for (ChestTypes type : ChestTypes.PLAYABLE) {
            Item item = BuiltInRegistries.ITEM.getValue(
                    Identifier.fromNamespaceAndPath(IronChestsCommon.MOD_ID, type.registryId));
            if (item != null) {
                PolymerItem.registerOverlay(item, ChestItems.create(type));
            }
        }
    }
}
