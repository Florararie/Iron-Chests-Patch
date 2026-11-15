package eu.pb4.ironchestpatch.mixin;

import com.gathertocraft.ironchest.blocks.ChestTypes;
import com.gathertocraft.ironchest.registry.ModBlocks;
import eu.pb4.ironchestpatch.impl.PolyChestBlock;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModBlocks.class)
public class ModBlocksMixin {
    // Runs after the blocks are in the registry: Polymer resolves their ids while registering the
    // overlay, so it cannot happen inside the block factory itself.
    @Inject(method = "registerBlocks", at = @At("TAIL"))
    private static void polymer$overlayBlocks(CallbackInfo ci) {
        for (ChestTypes type : ChestTypes.PLAYABLE) {
            PolymerBlock.registerOverlay(type.getBlock(), new PolyChestBlock(type));
        }
    }
}
