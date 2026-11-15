package eu.pb4.ironchestpatch.mixin;

import com.gathertocraft.ironchest.blocks.ChestTypes;
import com.gathertocraft.ironchest.registry.ModScreenHandlerType;
import eu.pb4.polymer.core.api.other.PolymerMenuUtils;
import net.minecraft.world.inventory.MenuType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** The mod's wide menus are never sent to clients, so hide them from registry sync. */
@Mixin(ModScreenHandlerType.class)
public class ModScreenHandlerTypeMixin {
    @Inject(method = "registerScreenHandlers", at = @At("TAIL"))
    private static void polymer$hideMenus(CallbackInfo ci) {
        for (ChestTypes type : ChestTypes.PLAYABLE) {
            MenuType<?> menuType = type.getMenuType();
            if (menuType != null) {
                PolymerMenuUtils.registerType(menuType);
            }
        }
    }
}
