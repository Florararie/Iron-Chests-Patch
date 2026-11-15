package eu.pb4.ironchestpatch.impl;

import com.gathertocraft.ironchest.blocks.ChestTypes;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import org.jspecify.annotations.Nullable;

/**
 * Sends each chest as a note block whose state selects a custom chest model from the companion
 * resource pack.
 *
 * <p>Vanilla chests are drawn by a block entity renderer that always picks one of a handful of
 * textures, so a resource pack cannot give a chest a custom look per block. A note block is a normal
 * block with a JSON model, no block entity, a full block collision shape and a wood sound, which
 * makes it a good carrier: the pack maps the otherwise unused {@code custom_head} instrument states
 * to the Iron Chests chest models, and the state's note and powered flags carry the tier and facing.
 *
 * <p>The block entity is never sent to clients; opening the chest still opens the server-side menu,
 * but the lid no longer animates and the chest open/close sound is gone. The companion resource pack
 * is required for the chest models to appear at all.
 */
public record PolyChestBlock(ChestTypes type) implements PolymerBlock {
    private static final int FACING_COUNT = 4;
    private static final int NOTE_STATES = 20;

    @Override
    public BlockState getPolymerBlockState(BlockState state, @Nullable PacketContext context) {
        int index = this.tierIndex() * FACING_COUNT + facingIndex(state);
        return Blocks.NOTE_BLOCK.defaultBlockState()
                .setValue(NoteBlock.INSTRUMENT, NoteBlockInstrument.CUSTOM_HEAD)
                .setValue(NoteBlock.NOTE, index % NOTE_STATES)
                .setValue(NoteBlock.POWERED, index >= NOTE_STATES);
    }

    @Override
    public BlockState getPolymerBreakEventBlockState(BlockState state, PacketContext context) {
        return this.getPolymerBlockState(state, context);
    }

    private int tierIndex() {
        return switch (this.type) {
            case DIRT -> 0;
            case COPPER -> 1;
            case IRON -> 2;
            case GOLD -> 3;
            case DIAMOND -> 4;
            case EMERALD -> 5;
            case CRYSTAL -> 6;
            case OBSIDIAN -> 7;
            case NETHERITE -> 8;
            case CHRISTMAS -> 9;
            default -> 0;
        };
    }

    private static int facingIndex(BlockState state) {
        if (!state.hasProperty(ChestBlock.FACING)) {
            return 0;
        }
        return switch (state.getValue(ChestBlock.FACING)) {
            case EAST -> 1;
            case SOUTH -> 2;
            case WEST -> 3;
            default -> 0;
        };
    }
}
