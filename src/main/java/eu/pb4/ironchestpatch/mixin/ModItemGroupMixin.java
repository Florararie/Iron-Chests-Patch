package eu.pb4.ironchestpatch.mixin;

import com.gathertocraft.ironchest.IronChestsCommon;
import com.gathertocraft.ironchest.registry.ModItemGroup;
import eu.pb4.ironchestpatch.impl.PolymerTabs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.CreativeModeTab;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModItemGroup.class)
public class ModItemGroupMixin {
    @Inject(method = "registerItemGroup", at = @At("TAIL"))
    private static void polymer$creativeTab(CallbackInfo ci) {
        CreativeModeTab tab = BuiltInRegistries.CREATIVE_MODE_TAB.getValue(IronChestsCommon.TAB.identifier());
        PolymerTabs.hideAndServe(tab);
    }
}
