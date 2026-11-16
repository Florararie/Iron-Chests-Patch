package eu.pb4.ironchestpatch.impl;

import com.gathertocraft.ironchest.blocks.ChestTypes;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import eu.pb4.polymer.virtualentity.api.BlockWithElementHolder;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

/**
 * Shows a chest on unmodded clients as a barrier plus a display entity holding the chest model.
 *
 * <p>The previous carrier was a note block whose blockstate was patched into the pack to draw the
 * chest model. That put the chest art in the block renderer, which has two consequences the resource
 * pack cannot paper over: an opaque carrier culls the faces of every neighbouring block, so any inset
 * in the chest model is a window into a missing face, and two touching carriers fight over the shared
 * face plane.
 *
 * <p>Here the client block is a barrier: invisible, solid (so it still collides and still takes the
 * click that opens the menu) and non-occluding (so neighbours keep their faces). The chest itself is a
 * display entity showing the mod's own chest item model, which means the real chest with its lid and
 * latch at its real proportions, and an entity has no neighbour culling at all.
 *
 * <p>The lid is animated by swapping the item model rather than by a block entity renderer: the
 * vanilla chest item model takes an {@code openness} angle, so the patch ships one model per lid
 * position and the entity picks the one matching the chest's current openness each tick. A closed
 * chest uses the mod's own item model, so nothing is duplicated for the common case.
 *
 * <p>Trade-off: the client draws break progress for its own block, and a barrier draws no cracks, so
 * mining a chest shows no cracking overlay until it pops. The server still tracks and applies the
 * break normally.
 */
public record PolyChestBlock(ChestTypes type) implements PolymerBlock, BlockWithElementHolder {
    /**
     * Rotation of the client model for each {@link #facingIndex} slot, in entity yaw degrees.
     *
     * <p>The chest's facing is the side its lid hinge is on, so its latch points at the opposite side;
     * the model is authored with the latch toward the viewer at yaw 0. Because players expect the latch
     * to face them, the model is turned to look back at the placement direction.
     */
    private static final float[] FACING_YAW = {0f, 90f, 180f, 270f};

    /** Lid positions between closed and fully open, generated into the resource pack per tier. */
    private static final int OPEN_STEPS = 5;

    @Override
    public BlockState getPolymerBlockState(BlockState state, @Nullable PacketContext context) {
        return Blocks.BARRIER.defaultBlockState();
    }

    @Override
    public boolean tickElementHolder(ServerLevel world, BlockPos pos, BlockState initialState) {
        return true;
    }

    @Override
    public @Nullable ElementHolder createElementHolder(ServerLevel world, BlockPos pos, BlockState initialState) {
        return new ChestHolder(world, pos, this, this.model(initialState, 0));
    }

    private ItemDisplayElement model(BlockState state, int step) {
        ItemDisplayElement element = new ItemDisplayElement(this.stack(step));
        element.setItemDisplayContext(ItemDisplayContext.NONE);
        element.setDisplaySize(2f, 2f);
        element.setViewRange(1.5f);
        element.setTeleportDuration(0);
        element.setInvisible(true);
        element.setYaw(FACING_YAW[facingIndex(state)]);
        return element;
    }

    private ItemStack stack(int step) {
        ItemStack stack = new ItemStack(Items.CHEST);
        stack.set(DataComponents.ITEM_MODEL, step == 0
                ? Identifier.fromNamespaceAndPath("ironchest", this.type.registryId)
                : Identifier.fromNamespaceAndPath("ironchestpatch",
                        "chest_open_" + this.type.registryId + "_" + step));
        return stack;
    }

    private static int facingIndex(BlockState state) {
        if (!state.hasProperty(ChestBlock.FACING)) {
            return 2;
        }
        return switch (state.getValue(ChestBlock.FACING)) {
            case EAST -> 1;
            case SOUTH -> 2;
            case WEST -> 3;
            default -> 0;
        };
    }

    private static final class ChestHolder extends ElementHolder {
        private final ServerLevel world;
        private final BlockPos pos;
        private final PolyChestBlock chest;
        private final ItemDisplayElement element;
        private int step;

        ChestHolder(ServerLevel world, BlockPos pos, PolyChestBlock chest, ItemDisplayElement element) {
            this.world = world;
            this.pos = pos;
            this.chest = chest;
            this.element = element;
            this.addElement(element);
        }

        /**
         * Steps the lid one model position per tick toward the chest's current state, which is the same
         * five-tick ramp vanilla uses. Re-reading the state every tick keeps the lid honest if a chest
         * is opened or closed mid-animation, and moving one step at a time keeps the angle continuous
         * instead of snapping straight to the target.
         */
        @Override
        protected void onTick() {
            int target = this.isOpen() ? OPEN_STEPS : 0;
            if (this.step == target) {
                return;
            }
            this.step += Integer.signum(target - this.step);
            this.element.setItem(this.chest.stack(this.step));
        }

        /**
         * Vanilla keeps the animated lid angle on the client and derives it from the synced opener
         * count, so the opener count is what the server actually knows: a chest is open while any
         * player has it open.
         */
        private boolean isOpen() {
            return this.world.getBlockEntity(this.pos) instanceof ChestBlockEntity chest
                    && !chest.getEntitiesWithContainerOpen().isEmpty();
        }
    }
}
