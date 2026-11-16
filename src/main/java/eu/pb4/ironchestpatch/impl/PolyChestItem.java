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

/**
 * Client representation of an Iron Chests chest item.
 *
 * <p>Names and lore are written as literals because unmodded clients have no Iron Chests language
 * file, and the server-side tooltip would otherwise arrive as raw translation keys.
 */
public record PolyChestItem(Item clientItem, Identifier model, String name, List<Component> lore) implements PolymerItem {
    @Override
    public Item getPolymerItem(ItemStack itemStack, PacketContext context) {
        return this.clientItem;
    }

    /**
     * The tier's own item model from the pack.
     *
     * <p>The client resolves an item's model from its id, and the vanilla item the chest is sent as
     * has no model named after the chest. Pinning the id here is what gives the inventory and the
     * advancement icons the real chest art instead of a missing-texture placeholder.
     */
    @Override
    public Identifier getPolymerItemModel(ItemStack itemStack, PacketContext context, HolderLookup.Provider lookup) {
        return this.model;
    }

    @Override
    public ItemStack getPolymerItemStack(ItemStack itemStack, TooltipFlag tooltipType, PacketContext context, HolderLookup.Provider lookup) {
        ItemStack out = PolymerItemUtils.createItemStack(itemStack, tooltipType, context, lookup);
        out.set(DataComponents.ITEM_NAME, Component.literal(this.name).withStyle(ChatFormatting.WHITE));
        out.set(DataComponents.LORE, new ItemLore(this.lore));
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
