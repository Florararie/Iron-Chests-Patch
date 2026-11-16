package eu.pb4.ironchestpatch.impl;

import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.core.api.item.PolymerItemUtils;
import java.util.List;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/**
 * Client representation of an Iron Core item (an upgrade, a key or a hand truck).
 *
 * <p>These items keep their real server-side behaviour (upgrading a chest in place, locking it,
 * lifting it); only the icon, name and lore are virtualised.
 *
 * <p>{@code model} is an item model definition shipped by this patch, used where the vanilla item
 * the stack is sent as cannot show the real art. The hand trucks need it: Iron Core's own definition
 * is a {@code minecraft:condition} on the {@code ironcore:carried_block} component, and an unmodded
 * client cannot resolve a component it has no registry entry for, so that definition is dropped and
 * the plain model is used instead.
 */
public record PolyCoreItem(Item clientItem, @Nullable Identifier model, String name, List<Component> lore)
        implements PolymerItem {
    @Override
    public Item getPolymerItem(ItemStack itemStack, PacketContext context) {
        return this.clientItem;
    }

    /**
     * The model this stack is drawn with. Without an explicit one, the inherited behaviour is what
     * every other Iron Core item needs: it returns the stack's own {@code item_model} component, i.e.
     * the pack definition named after the real server-side item. Returning {@code null} here instead
     * would drop that component and fall back to the vanilla stand-in's model, which is how the
     * container key ended up drawn as a trial key.
     */
    @Override
    public @Nullable Identifier getPolymerItemModel(ItemStack itemStack, PacketContext context, HolderLookup.Provider lookup) {
        return this.model != null ? this.model : PolymerItem.super.getPolymerItemModel(itemStack, context, lookup);
    }

    @Override
    public ItemStack getPolymerItemStack(ItemStack itemStack, TooltipFlag tooltipType, PacketContext context, HolderLookup.Provider lookup) {
        ItemStack out = PolymerItemUtils.createItemStack(itemStack, tooltipType, context, lookup);
        out.set(DataComponents.ITEM_NAME, Component.literal(this.name).withStyle(ChatFormatting.WHITE));
        if (!this.lore.isEmpty()) {
            out.set(DataComponents.LORE, new ItemLore(this.lore));
        }
        return out;
    }

    @Override
    public boolean isPolymerBlockInteraction(BlockState state, ServerPlayer player, InteractionHand hand, ItemStack stack, ServerLevel world, BlockHitResult blockHitResult, InteractionResult actionResult) {
        return true;
    }

    @Override
    public boolean isIgnoringBlockInteractionPlaySoundExceptedEntity(BlockState state, ServerPlayer player, InteractionHand hand, ItemStack stack, ServerLevel world, BlockHitResult blockHitResult) {
        return true;
    }
}
