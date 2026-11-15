package eu.pb4.ironchestpatch.impl;

import net.minecraft.world.inventory.MenuType;

/**
 * Chooses the chest GUI size and page size for a real container size.
 *
 * <p>Vanilla only has 9-column chest screens, so a page is always a whole number of rows. Small
 * chests use exactly the rows they need and no navigation row. Larger chests are paged with the
 * largest page size that divides the inventory evenly, which keeps every page full (or nearly full)
 * instead of showing a mostly empty last page.
 */
public record ChestLayout(int pageSize, int contentRows, int rows, boolean navigation) {
    public static ChestLayout of(int containerSize) {
        int size = Math.max(1, containerSize);
        int pageSize;
        boolean navigation;

        if (size <= 54) {
            pageSize = size;
            navigation = false;
        } else {
            pageSize = 45;
            // Prefer a page that divides the inventory exactly. 42 is useful for the 126-slot
            // chests; 36 and 27 keep the 108- and 81-slot chests free of empty page slots.
            for (int candidate = 45; candidate >= 18; candidate--) {
                if (size % candidate == 0) {
                    pageSize = candidate;
                    break;
                }
            }
            navigation = pageCount(size, pageSize) > 1;
        }

        int contentRows = (pageSize + 8) / 9;
        int rows = contentRows + (navigation ? 1 : 0);
        return new ChestLayout(pageSize, contentRows, rows, navigation);
    }

    public static int pageCount(int containerSize, int pageSize) {
        return Math.max(1, (containerSize + pageSize - 1) / pageSize);
    }

    /** Slots in the content area, including any padding cells at the end of the last row. */
    public int contentSlots() {
        return this.contentRows * 9;
    }

    public int playerStart() {
        return this.contentSlots() + (this.navigation ? 9 : 0);
    }

    public int navigationY() {
        return 18 + this.contentRows * 18;
    }

    public int playerY() {
        // Matches ChestMenu's own 18 + rows * 18 + 13 player-inventory offset.
        return 18 + this.rows * 18 + 13;
    }

    public MenuType<?> menuType() {
        return switch (this.rows) {
            case 1 -> MenuType.GENERIC_9x1;
            case 2 -> MenuType.GENERIC_9x2;
            case 3 -> MenuType.GENERIC_9x3;
            case 4 -> MenuType.GENERIC_9x4;
            case 5 -> MenuType.GENERIC_9x5;
            default -> MenuType.GENERIC_9x6;
        };
    }
}
