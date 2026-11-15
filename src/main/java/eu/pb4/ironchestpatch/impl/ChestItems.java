package eu.pb4.ironchestpatch.impl;

import com.gathertocraft.ironchest.blocks.ChestTypes;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

/** Builds the client-side representation for each chest tier item. */
public final class ChestItems {
    private ChestItems() {}

    public static PolyChestItem create(ChestTypes type) {
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(type.size() + " slots (" + type.rowCount() + " rows x " + type.rowLength() + " columns)")
                .withStyle(ChatFormatting.GRAY));
        String trait = trait(type);
        if (trait != null) {
            lore.add(Component.literal(trait).withStyle(ChatFormatting.DARK_GRAY));
        }
        // Vanilla clients only have one chest icon, so tiers are distinguished by name and lore.
        return new PolyChestItem(Items.CHEST, name(type), List.copyOf(lore));
    }

    public static String name(ChestTypes type) {
        return switch (type) {
            case NETHERITE -> "Netherite Chest";
            case OBSIDIAN -> "Obsidian Chest";
            case CRYSTAL -> "Crystal Chest";
            case DIAMOND -> "Diamond Chest";
            case EMERALD -> "Emerald Chest";
            case GOLD -> "Gold Chest";
            case IRON -> "Iron Chest";
            case COPPER -> "Copper Chest";
            case CHRISTMAS -> "Christmas Chest";
            case DIRT -> "Dirt Chest 9000!";
            default -> "Chest";
        };
    }

    private static String trait(ChestTypes type) {
        return switch (type) {
            case DIRT -> "Stores dirt only";
            case CRYSTAL -> "See-through";
            case OBSIDIAN, NETHERITE -> "Blast-resistant";
            case CHRISTMAS -> "Vanilla-sized, festive";
            default -> null;
        };
    }
}
