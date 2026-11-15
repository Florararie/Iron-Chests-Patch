package eu.pb4.ironchestpatch.impl;

import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

/** Client-side representation of Iron Core items, mirrored from ironcore's en_us lang file. */
public final class CoreItems {
    public static final List<String> IDS = List.of(
            "copper_upgrade",
            "iron_upgrade",
            "gold_upgrade",
            "diamond_upgrade",
            "emerald_upgrade",
            "crystal_upgrade",
            "obsidian_upgrade",
            "netherite_upgrade",
            "container_key",
            "hand_truck",
            "enhanced_hand_truck");

    private static final Map<String, Item> CLIENT_ITEMS = Map.ofEntries(
            Map.entry("copper_upgrade", Items.COPPER_INGOT),
            Map.entry("iron_upgrade", Items.IRON_INGOT),
            Map.entry("gold_upgrade", Items.GOLD_INGOT),
            Map.entry("diamond_upgrade", Items.DIAMOND),
            Map.entry("emerald_upgrade", Items.EMERALD),
            Map.entry("crystal_upgrade", Items.AMETHYST_SHARD),
            Map.entry("obsidian_upgrade", Items.OBSIDIAN),
            Map.entry("netherite_upgrade", Items.NETHERITE_INGOT),
            Map.entry("container_key", Items.TRIAL_KEY),
            Map.entry("hand_truck", Items.MINECART),
            Map.entry("enhanced_hand_truck", Items.CHEST_MINECART));

    private CoreItems() {}

    public static PolyCoreItem create(String id) {
        return new PolyCoreItem(CLIENT_ITEMS.get(id), name(id), lore(id));
    }

    private static String name(String id) {
        return switch (id) {
            case "copper_upgrade" -> "Copper Upgrade";
            case "iron_upgrade" -> "Iron Upgrade";
            case "gold_upgrade" -> "Gold Upgrade";
            case "diamond_upgrade" -> "Diamond Upgrade";
            case "emerald_upgrade" -> "Emerald Upgrade";
            case "crystal_upgrade" -> "Crystal Upgrade";
            case "obsidian_upgrade" -> "Obsidian Upgrade";
            case "netherite_upgrade" -> "Netherite Upgrade";
            case "container_key" -> "Container Key";
            case "hand_truck" -> "Hand Truck";
            case "enhanced_hand_truck" -> "Enhanced Hand Truck";
            default -> "Iron Core Item";
        };
    }

    private static List<Component> lore(String id) {
        if (id.endsWith("_upgrade")) {
            return List.of(
                    line(upgradeDescription(id), ChatFormatting.GREEN),
                    line(upgradeSizes(id), ChatFormatting.DARK_GRAY));
        }
        return switch (id) {
            case "container_key" -> List.of(
                    line("Shift+Right-Click a chest to lock or unlock it", ChatFormatting.GRAY),
                    line("Rename in an anvil to label this key", ChatFormatting.DARK_GRAY));
            case "hand_truck" -> List.of(
                    line("Sneak + Right-Click a chest to lift it", ChatFormatting.GRAY),
                    line("Carrying a chest keeps its contents", ChatFormatting.DARK_GRAY));
            case "enhanced_hand_truck" -> List.of(
                    line("Sneak + Right-Click a chest to lift it", ChatFormatting.GRAY),
                    line("Also moves spawners", ChatFormatting.DARK_GRAY));
            default -> List.of();
        };
    }

    private static String upgradeDescription(String id) {
        return switch (id) {
            case "copper_upgrade" -> "Upgrades Wooden chests to Copper";
            case "iron_upgrade" -> "Upgrades Copper chests to Iron";
            case "gold_upgrade" -> "Upgrades Iron chests to Gold";
            case "diamond_upgrade" -> "Upgrades Gold chests to Diamond";
            case "emerald_upgrade" -> "Upgrades Gold chests to Emerald";
            case "crystal_upgrade" -> "Upgrades Diamond or Emerald chests to Crystal";
            case "obsidian_upgrade" -> "Upgrades Diamond or Emerald chests to Obsidian";
            case "netherite_upgrade" -> "Upgrades Diamond, Emerald or Obsidian chests to Netherite";
            default -> "Upgrades a chest";
        };
    }

    private static String upgradeSizes(String id) {
        return switch (id) {
            case "copper_upgrade" -> "27 slots (9x3) -> 45 slots (9x5)";
            case "iron_upgrade" -> "45 slots (9x5) -> 54 slots (9x6)";
            case "gold_upgrade" -> "54 slots (9x6) -> 81 slots (9x9)";
            case "diamond_upgrade" -> "81 slots (9x9) -> 108 slots (12x9)";
            case "emerald_upgrade" -> "81 slots (9x9) -> 108 slots (12x9)";
            case "crystal_upgrade", "obsidian_upgrade" -> "108 slots (12x9) -> 108 slots (12x9)";
            case "netherite_upgrade" -> "108 slots (12x9) -> 126 slots (14x9)";
            default -> "";
        };
    }

    private static Component line(String text, ChatFormatting color) {
        return Component.literal(text).withStyle(color);
    }
}
