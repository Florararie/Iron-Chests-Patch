package eu.pb4.ironchestpatch.impl;

import com.gathertocraft.ironchest.blocks.ChestTypes;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import eu.pb4.ironchestpatch.IronChestsPolymerPatch;
import eu.pb4.polymer.resourcepack.api.ResourcePackBuilder;
import eu.pb4.polymer.virtualentity.api.BlockWithElementHolder;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import java.util.Base64;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

/**
 * Keeps the chest carrier from being seen while vanilla copper chests keep looking like copper.
 *
 * <p>The carrier is a single waxed copper chest (see {@link ChestCarrier}), and a client builds the
 * block entity for a chest state itself, from the block update that places it. That entity is what
 * draws a chest, so the carrier's chest can only be hidden by blanking the sprite it asks for,
 * {@code textures/entity/chest/copper.png}.
 *
 * <p>Blanking that sprite would hide single vanilla copper chests too, so they are re-drawn here:
 * both the waxed and the unwaxed block get this patch's copper chest as a display entity, which is
 * the same art the mod's own copper chest shows. The half sprites are left alone, so a double copper
 * chest is still drawn by vanilla and nothing here draws over it.
 *
 * <p>Only {@link BlockWithElementHolder} is registered for these blocks, never a {@link
 * eu.pb4.polymer.core.api.block.PolymerBlock}: remapping the state of a vanilla copper chest would
 * feed its own state back into the carrier.
 */
public final class VanillaCopperChests {
    private static final Gson GSON = new Gson();

    /**
     * A 64x64 fully transparent PNG, the size of the sprite it replaces, so the chest model's UVs
     * line up with nothing at all.
     */
    private static final byte[] BLANK = Base64.getDecoder().decode(
            "iVBORw0KGgoAAAANSUhEUgAAAEAAAABACAYAAACqaXHeAAAAJ0lEQVR42u3BAQ0AAADCoPdPbQ43oAAAAAAAAAAAAAAAAAAAAIB3A0BAAAGveg7oAAAAAElFTkSuQmCC");

    /** The one copper chest sprite the carrier asks for, and the only one that is blanked. */
    private static final String SPRITE = "assets/minecraft/textures/entity/chest/copper.png";

    private VanillaCopperChests() {
    }

    /**
     * Draws vanilla copper chests with a display entity, because their own renderer draws nothing
     * once the single sprite is blanked.
     *
     * <p>Reusing {@link PolyChestBlock} is what makes this small: the chest these stand in for is
     * the mod's own copper chest, so the holder only has to rotate the model, follow the chest and
     * step the lid, all of which it already does for a placed chest.
     */
    public static void register() {
        var overlay = new Overlay(new PolyChestBlock(ChestTypes.COPPER));
        BlockWithElementHolder.registerOverlay(Blocks.COPPER_CHEST, overlay);
        BlockWithElementHolder.registerOverlay(Blocks.WAXED_COPPER_CHEST, overlay);
        IronChestsPolymerPatch.LOGGER.info("Vanilla copper chests are now drawn by display entities");
    }

    /** Blanks the single sprite and keeps both copper chest items visible without it. */
    public static void write(ResourcePackBuilder builder) {
        builder.addData(SPRITE, BLANK);

        // The two item models name the same sprite, so they are pointed at the mod's copper chest
        // art instead: the one the display entities above draw.
        var model = GSON.toJson(itemModel());
        builder.addStringData("assets/minecraft/items/copper_chest.json", model);
        builder.addStringData("assets/minecraft/items/waxed_copper_chest.json", model);
    }

    private static JsonObject itemModel() {
        var inner = new JsonObject();
        inner.addProperty("type", "minecraft:chest");
        inner.addProperty("texture", "ironchest:copper_chest");
        inner.addProperty("openness", 0.0f);
        inner.addProperty("chest_type", "single");

        var model = new JsonObject();
        model.addProperty("type", "minecraft:special");
        // The vanilla copper chest base keeps its own item transforms, so the icon sits in the
        // inventory the way players expect.
        model.addProperty("base", "minecraft:item/copper_chest");
        model.add("model", inner);

        var json = new JsonObject();
        json.add("model", model);
        return json;
    }

    /** The holder half of the override, kept separate from the block mapping on purpose. */
    private record Overlay(PolyChestBlock chest) implements BlockWithElementHolder {
        @Override
        public @Nullable ElementHolder createElementHolder(ServerLevel world, BlockPos pos, BlockState state) {
            return this.chest.createElementHolder(world, pos, state);
        }

        @Override
        public boolean tickElementHolder(ServerLevel world, BlockPos pos, BlockState state) {
            return true;
        }
    }
}
