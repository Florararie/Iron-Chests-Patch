package eu.pb4.ironchestpatch.mixin;

import com.gathertocraft.ironchest.blocks.ChestTypes;
import com.gathertocraft.ironchest.registry.ModBlockEntityType;
import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Keeps the chest block entities on the server.
 *
 * <p>Clients no longer receive a chest block entity: the placed look is a resource-pack block model
 * selected through the fake note block state, and sending a vanilla chest block entity at a note
 * block position would make the client's chest renderer run against a block it cannot handle.
 */
@Mixin(ModBlockEntityType.class)
public class ModBlockEntityTypeMixin {
    @Inject(method = "registerBlockEntities", at = @At("TAIL"))
    private static void polymer$hideBlockEntities(CallbackInfo ci) {
        for (ChestTypes type : ChestTypes.PLAYABLE) {
            PolymerBlockUtils.registerBlockEntity(type.getBlockEntityType());
        }
    }
}
