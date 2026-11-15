package eu.pb4.ironchestpatch.mixin;

import com.gathertocraft.ironcore.lock.KeyEditorPayloads;
import com.gathertocraft.ironcore.lock.KeyItem;
import java.util.UUID;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The key editor is a client-side Iron Core screen with its own menu type and payloads.
 *
 * <p>Sending it to a client without Iron Core would hand it an unknown menu type, so the editor is
 * only opened for clients that registered Iron Core's sync channel. Locking, unlocking and access
 * checks are unaffected; they never needed the client.
 */
@Mixin(KeyItem.class)
public class KeyItemMixin {
    @Inject(method = "openEntryEditor", at = @At("HEAD"), cancellable = true)
    private static void polymer$guardEditor(ServerPlayer player, UUID entryId, CallbackInfo ci) {
        if (!ServerPlayNetworking.canSend(player, KeyEditorPayloads.Sync.TYPE)) {
            player.sendOverlayMessage(Component.literal(
                    "Editing a key needs the Iron Core mod on the client; locks still work without it."));
            ci.cancel();
        }
    }
}
