package eu.pb4.ironchestpatch.impl;

import com.gathertocraft.ironchest.blocks.ChestTypes;
import com.gathertocraft.ironchest.blocks.blockentities.CrystalChestEntity;
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
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

/**
 * Shows a chest on unmodded clients as a vanilla copper chest plus a display entity holding the
 * chest model.
 *
 * <p>The previous carrier was a note block whose blockstate was patched into the pack to draw the
 * chest model. That put the chest art in the block renderer, which has two consequences the resource
 * pack cannot paper over: an opaque carrier culls the faces of every neighbouring block, so any inset
 * in the chest model is a window into a missing face, and two touching carriers fight over the shared
 * face plane.
 *
 * <p>Here the client block is a single waxed copper chest, chosen in {@link ChestCarrier} for its
 * chest-shaped collision and its fluid state, with the single sprite blanked in the pack so the
 * client's own chest entity draws nothing. It still takes the click that opens the menu, and a
 * waterlogged chest still shows its water. The chest itself is a display entity showing the mod's
 * own chest item model, which means the real chest with its lid and latch at its real proportions,
 * and an entity has no neighbour culling at all.
 *
 * <p>The lid is animated by swapping the item model rather than by a block entity renderer: the
 * vanilla chest item model takes an {@code openness} angle, so the patch ships one model per lid
 * position and the entity picks the one matching the chest's current openness each tick. A closed
 * chest uses the mod's own item model, so nothing is duplicated for the common case.
 *
 * <p>A crystal chest also shows what is in it: the first twelve non-empty stacks are drawn as item
 * display entities where the mod's own renderer puts them, a little above the chest's floor, and
 * they turn slowly like the mod's do. Like everything else here they are refreshed from the server,
 * so they follow the inventory as it changes.
 *
 * <p>Trade-off: the client draws break progress for its own block, and a chest's block model has no
 * geometry, so mining a chest shows no cracking overlay until it pops. The server still tracks and
 * applies the break normally.
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

    /** How many stacks a crystal chest shows, as in the mod's own renderer. */
    private static final int SPIN_SLOTS = 12;

    /**
     * Where a crystal chest's stacks sit, in block space, copied from the mod's renderer after its
     * own half-block scale: two columns on each side and three levels of stacked items.
     */
    private static final double[] SPIN_X = {0.275, 0.7};
    private static final double[] SPIN_Z = {0.35, 0.7};
    private static final double[] SPIN_Y = {0.15, 0.4, 0.65};

    @Override
    public BlockState getPolymerBlockState(BlockState state, @Nullable PacketContext context) {
        // The display entity turns the model, so the carrier only supplies the chest shape and the
        // fluid state, and it is always single so the client asks for the sprite the pack blanks.
        return ChestCarrier.of(state);
    }

    @Override
    public boolean tickElementHolder(ServerLevel world, BlockPos pos, BlockState initialState) {
        return true;
    }

    @Override
    public @Nullable ElementHolder createElementHolder(ServerLevel world, BlockPos pos, BlockState initialState) {
        if (initialState.hasProperty(ChestBlock.TYPE)
                && initialState.getValue(ChestBlock.TYPE) != ChestType.SINGLE) {
            // Only singles are blanked, so a half chest is still drawn by vanilla; drawing it here
            // as well would show a whole chest on each half.
            return null;
        }
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
        /** One display per shown stack for a crystal chest, null for every other tier. */
        private final @Nullable ItemDisplayElement[] items;
        /** What the displays currently show, so the inventory is only read into them when it changes. */
        private final ItemStack[] shownItems;
        private int step;
        private boolean shown = true;

        ChestHolder(ServerLevel world, BlockPos pos, PolyChestBlock chest, ItemDisplayElement element) {
            this.world = world;
            this.pos = pos;
            this.chest = chest;
            this.element = element;
            this.addElement(element);
            this.items = chest.type() == ChestTypes.CRYSTAL ? this.createItems() : null;
            this.shownItems = this.items == null ? new ItemStack[0] : new ItemStack[this.items.length];
            if (this.items != null) {
                java.util.Arrays.fill(this.shownItems, ItemStack.EMPTY);
            }
        }

        /**
         * Steps the lid one model position per tick toward the chest's current state, which is the same
         * five-tick ramp vanilla uses. Re-reading the state every tick keeps the lid honest if a chest
         * is opened or closed mid-animation, and moving one step at a time keeps the angle continuous
         * instead of snapping straight to the target.
         */
        @Override
        protected void onTick() {
            // A single vanilla copper chest can grow into a double one. Polymer keeps this holder
            // then, because the block itself did not change, and the half now draws its own art, so
            // the display has to stop drawing a whole chest over it.
            if (this.isHalf()) {
                if (this.shown) {
                    this.shown = false;
                    this.element.setItem(ItemStack.EMPTY);
                    this.hideItems();
                }
                return;
            }
            if (!this.shown) {
                this.shown = true;
                this.step = 0;
                this.element.setItem(this.chest.stack(0));
            }

            int target = this.isOpen() ? OPEN_STEPS : 0;
            if (this.step != target) {
                this.step += Integer.signum(target - this.step);
                this.element.setItem(this.chest.stack(this.step));
            }
            this.spin();
        }

        /**
         * Replaces the item displays with the first twelve stacks of a crystal chest, and turns them.
         *
         * <p>The mod's renderer reads the inventory and draws it itself, once per frame. A vanilla
         * client cannot do that for a server-side chest, so the stacks are sent once and only re-sent
         * when one of them actually changes; the turn is the one part that has to be sent continuously,
         * because a display entity keeps whatever rotation it was last told.
         */
        private void spin() {
            if (this.items == null
                    || !(this.world.getBlockEntity(this.pos) instanceof CrystalChestEntity crystal)) {
                return;
            }
            var stacks = crystal.getTopStacks();
            // One degree per tick, like the mod. 360 and 0 are the same orientation, so the wrap is
            // invisible and keeps the angle small enough to stay exact.
            float angle = this.world.getGameTime() % 360L;
            for (int i = 0; i < this.items.length; i++) {
                ItemStack stack = stacks.get(i);
                if (!ItemStack.matches(this.shownItems[i], stack)) {
                    this.shownItems[i] = stack.copy();
                    this.items[i].setItem(stack);
                }
                if (!stack.isEmpty()) {
                    this.items[i].setLeftRotation(new Quaternionf().rotateY((float) Math.toRadians(angle)));
                }
            }
        }

        /** Takes the contents away when a crystal chest grew into a double and vanilla took over. */
        private void hideItems() {
            if (this.items == null) {
                return;
            }
            for (int i = 0; i < this.items.length; i++) {
                this.items[i].setItem(ItemStack.EMPTY);
                this.shownItems[i] = ItemStack.EMPTY;
            }
        }

        /**
         * Builds the twelve item displays where the mod draws its stacks.
         *
         * <p>They are separate entities, one per stack, so they are placed relative to the block
         * centre the holder sits on and turned with the chest, exactly as the mod rotates its own
         * stack positions with the chest model.
         */
        private ItemDisplayElement[] createItems() {
            float yaw = FACING_YAW[facingIndex(this.world.getBlockState(this.pos))];
            var elements = new ItemDisplayElement[SPIN_SLOTS];
            for (int i = 0; i < elements.length; i++) {
                var item = new ItemDisplayElement();
                item.setItemDisplayContext(ItemDisplayContext.GROUND);
                item.setViewRange(1.5f);
                item.setTeleportDuration(0);
                item.setInvisible(true);
                // The mod scales its items to half a block and the ground context halves that again,
                // which is why they are a quarter block and stack on top of each other.
                item.setScale(new Vector3f(0.5f, 0.5f, 0.5f));
                // Without a size the display's own culling box is a point, so an item can vanish
                // while the chest that contains it is still on screen.
                item.setDisplaySize(0.5f, 0.5f);
                item.setTranslation(spinOffset(i, yaw));
                this.addElement(item);
                elements[i] = item;
            }
            return elements;
        }

        /**
         * Where one spinning stack belongs, relative to the block centre and turned with the chest.
         *
         * <p>The mod groups them three to a corner, in the order the loop visits the corners, so a
         * slot is its group divided by the stack height for the corner and the remainder for the
         * level. A chest is turned about its centre, so the offset is turned about the centre too.
         */
        private static Vector3f spinOffset(int slot, float yaw) {
            int group = slot / SPIN_Y.length;
            double x = SPIN_X[group % 2] - 0.5;
            double z = SPIN_Z[group / 2] - 0.5;
            double y = SPIN_Y[slot % SPIN_Y.length] - 0.5;
            double radians = Math.toRadians(-yaw);
            double cos = Math.cos(radians);
            double sin = Math.sin(radians);
            return new Vector3f((float) (x * cos + z * sin), (float) y, (float) (-x * sin + z * cos));
        }

        /** Vanilla copper chests form doubles; the mod's own chests are always single. */
        private boolean isHalf() {
            BlockState state = this.world.getBlockState(this.pos);
            return state.hasProperty(ChestBlock.TYPE) && state.getValue(ChestBlock.TYPE) != ChestType.SINGLE;
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
