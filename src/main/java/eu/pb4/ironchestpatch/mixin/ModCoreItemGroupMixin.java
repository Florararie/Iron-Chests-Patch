package eu.pb4.ironchestpatch.mixin;

import com.gathertocraft.ironcore.IronCoreCommon;
import com.gathertocraft.ironcore.registry.ModCoreItemGroup;
import eu.pb4.ironchestpatch.impl.PolymerTabs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.CreativeModeTab;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModCoreItemGroup.class)
public class ModCoreItemGroupMixin {
    @Inject(method = "registerItemGroup", at = @At("TAIL"))
    private static void polymer$creativeTab(CallbackInfo ci) {
        CreativeModeTab tab = BuiltInRegistries.CREATIVE_MODE_TAB.getValue(IronCoreCommon.TAB.identifier());
        PolymerTabs.track(tab);
    }
}
