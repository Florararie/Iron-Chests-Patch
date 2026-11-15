package eu.pb4.ironchestpatch.mixin;

import com.gathertocraft.ironchest.blocks.GenericChestBlock;
import eu.pb4.ironchestpatch.impl.PolymerChestMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Swaps the mod's wide chest menu for a menu unmodded clients can open.
 *
 * <p>The injection is at RETURN so the mod's own "blocked by a solid block above" and locked-chest
 * checks still decide whether a menu is offered at all.
 */
@Mixin(GenericChestBlock.class)
public class GenericChestBlockMixin {
    @Inject(method = "getMenuProvider", at = @At("RETURN"), cancellable = true)
    private void polymer$replaceMenu(BlockState state, Level level, BlockPos pos, CallbackInfoReturnable<MenuProvider> cir) {
        if (cir.getReturnValue() == null) {
            return;
        }
        if (level.getBlockEntity(pos) instanceof Container container) {
            cir.setReturnValue(new SimpleMenuProvider(
                    (syncId, inventory, player) -> new PolymerChestMenu(syncId, inventory, container),
                    PolymerChestMenu.title(container, state.getBlock().getName())));
        }
    }
}
