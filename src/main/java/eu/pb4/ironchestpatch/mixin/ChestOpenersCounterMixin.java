package eu.pb4.ironchestpatch.mixin;

import eu.pb4.ironchestpatch.impl.PolymerChestMenu;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Makes vanilla's chest opener counter recognise the paged menu.
 *
 * <p>{@code ChestBlockEntity} counts viewers by asking whether a nearby player's open menu belongs to
 * it. The vanilla check only understands {@code ChestMenu}; without this, the paged menu is invisible
 * to the counter, the open count desynchronises after the scheduled recheck, the close block event is
 * never sent (the lid stays open) and hand trucks think the chest is still being viewed.
 */
@Mixin(targets = "net.minecraft.world.level.block.entity.ChestBlockEntity$1")
public abstract class ChestOpenersCounterMixin {
    @Inject(method = "isOwnContainer", at = @At("RETURN"), cancellable = true)
    private void polymer$recognisePagedMenu(Player player, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ()) {
            return;
        }
        ChestBlockEntity chest = ((ChestOpenersCounterAccessor) this).polymer$chest();
        if (player.containerMenu instanceof PolymerChestMenu menu && menu.container() == chest) {
            cir.setReturnValue(true);
        }
    }
}
