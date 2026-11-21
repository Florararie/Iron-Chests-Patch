package eu.pb4.ironchestpatch.impl;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;

/**
 * The vanilla block an unmodded client is sent for an Iron Chests chest.
 *
 * <p>It is a single waxed copper chest. A chest block is what gives the carrier the chest's own
 * collision box, so a chest collides like a chest instead of like a full block, and it is
 * waterloggable, so a chest in water shows water around it. Waxed and unwaxed copper chests share
 * one sprite, and the patch re-draws both, so either state would do; the waxed one is used because
 * its look is fixed by definition and so the carrier state never means anything else.
 *
 * <p>The type is always {@link ChestType#SINGLE}. That is what keeps the client's own chest entity
 * asking for the single sprite, {@code textures/entity/chest/copper.png} - the only copper chest
 * sprite this patch blanks - while the half sprites stay untouched for the double chests vanilla
 * still draws itself.
 *
 * <p>Waterlogged follows the chest it stands in for. A client derives fluid from the block state,
 * so the carrier has to say what the real chest says: always dry would leave a chest in water
 * looking dry, always waterlogged would flood every chest on dry land. Iron Chests' chests extend
 * {@link ChestBlock} and so carry its waterlogged property, which is copied across unchanged.
 */
public final class ChestCarrier {
    /** The dry carrier. */
    public static final BlockState STATE = create(false);

    /** The same carrier for a chest that is under water. */
    private static final BlockState WATERLOGGED = create(true);

    private static BlockState create(boolean waterlogged) {
        return Blocks.WAXED_COPPER_CHEST.defaultBlockState()
                .setValue(ChestBlock.FACING, Direction.NORTH)
                .setValue(ChestBlock.TYPE, ChestType.SINGLE)
                .setValue(ChestBlock.WATERLOGGED, waterlogged);
    }

    /**
     * The carrier for one chest, waterlogged exactly when that chest is.
     *
     * <p>A chest holds fluid only if its block carries the property, so it is normally always
     * there; the check is for a mod that reworks the block without it.
     */
    public static BlockState of(BlockState chest) {
        return chest.hasProperty(ChestBlock.WATERLOGGED) && chest.getValue(ChestBlock.WATERLOGGED)
                ? WATERLOGGED
                : STATE;
    }

    private ChestCarrier() {
    }
}
