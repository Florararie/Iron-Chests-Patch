package eu.pb4.ironchestpatch.impl;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;

/**
 * A chest screen unmodded clients can actually open.
 *
 * <p>Vanilla clients can only display a 9-column container, while Iron Chests has up to 14x9 slots.
 * Small inventories use exactly the number of vanilla chest rows they need (the Christmas chest is
 * a three-row chest, not a six-row one). Larger inventories are paged, with the page size chosen so
 * pages stay full wherever possible. The navigation row uses a font-glyph background from the pack
 * to remove the unused slot outlines, so it reads as part of the native chest panel; the same glyph
 * covers the few cells a 42-slot page leaves unused in its last row.
 */
public class PolymerChestMenu extends AbstractContainerMenu {
    public static final int NAV_SIZE = 9;
    private static final int PREV_SLOT = 0;
    private static final int NEXT_SLOT = NAV_SIZE - 1;

    private final Container container;
    private final ChestLayout layout;
    private final PagedContainerView view;
    private final SimpleContainer navigation;
    private int page;

    public PolymerChestMenu(int syncId, Inventory playerInventory, Container container) {
        this(syncId, playerInventory, container, ChestLayout.of(container.getContainerSize()));
    }

    private PolymerChestMenu(int syncId, Inventory playerInventory, Container container, ChestLayout layout) {
        super(layout.menuType(), syncId);
        this.container = container;
        this.layout = layout;
        this.view = new PagedContainerView(container, layout.pageSize());
        this.navigation = layout.navigation() ? new SimpleContainer(NAV_SIZE) : null;
        container.startOpen(playerInventory.player);

        for (int slot = 0; slot < layout.contentSlots(); slot++) {
            int row = slot / 9;
            int column = slot % 9;
            this.addSlot(new PagedSlot(this.view, slot, 8 + column * 18, 18 + row * 18));
        }

        if (layout.navigation()) {
            for (int i = 0; i < NAV_SIZE; i++) {
                this.addSlot(new Slot(this.navigation, i, 8 + i * 18, layout.navigationY()) {
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return false;
                    }

                    @Override
                    public boolean mayPickup(Player player) {
                        return false;
                    }
                });
            }
        }

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                this.addSlot(new Slot(playerInventory, column + row * 9 + 9, 8 + column * 18, layout.playerY() + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            this.addSlot(new Slot(playerInventory, column, 8 + column * 18, layout.playerY() + 58));
        }

        this.refreshNavigation();
    }

    public static Component title(Container container, Component name) {
        ChestLayout layout = ChestLayout.of(container.getContainerSize());
        return ChestTitle.decorate(name, layout);
    }

    public int pageCount() {
        return ChestLayout.pageCount(this.container.getContainerSize(), this.layout.pageSize());
    }

    public int page() {
        return this.page;
    }

    /** The real chest inventory, used by vanilla's opener counter to recognise this menu. */
    public Container container() {
        return this.container;
    }

    private void changePage(int delta) {
        int next = Math.max(0, Math.min(this.pageCount() - 1, this.page + delta));
        if (next == this.page) {
            return;
        }
        this.page = next;
        this.view.setPage(this.page);
        this.refreshNavigation();
        // Contents differ per page, so vanilla's per-slot comparison resyncs the screen next tick.
        this.broadcastChanges();
    }

    private void refreshNavigation() {
        if (this.navigation == null) {
            return;
        }

        for (int i = 0; i < NAV_SIZE; i++) {
            this.navigation.setItem(i, ItemStack.EMPTY);
        }

        String pageText = "Page " + (this.page + 1) + " / " + this.pageCount();
        if (this.page > 0) {
            this.navigation.setItem(PREV_SLOT, button(
                    "ui_prev",
                    "Previous page",
                    ChatFormatting.WHITE,
                    List.of(Component.literal(pageText).withStyle(ChatFormatting.GRAY))));
        }
        if (this.page < this.pageCount() - 1) {
            this.navigation.setItem(NEXT_SLOT, button(
                    "ui_next",
                    "Next page",
                    ChatFormatting.WHITE,
                    List.of(Component.literal(pageText).withStyle(ChatFormatting.GRAY))));
        }
    }

    private static ItemStack button(String model, String name, ChatFormatting color, List<Component> lore) {
        ItemStack stack = new ItemStack(Items.PAPER);
        stack.set(DataComponents.ITEM_MODEL, Identifier.fromNamespaceAndPath("ironchestpatch", model));
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(name).withStyle(color));
        if (!lore.isEmpty()) {
            stack.set(DataComponents.LORE, new ItemLore(lore));
        }
        return stack;
    }

    @Override
    public void clicked(int slotIndex, int button, ContainerInput clickType, Player player) {
        if (slotIndex >= this.layout.contentSlots() && slotIndex < this.layout.playerStart()) {
            int navSlot = slotIndex - this.layout.contentSlots();
            if (navSlot == PREV_SLOT) {
                this.changePage(-1);
            } else if (navSlot == NEXT_SLOT) {
                this.changePage(1);
            }
            // Force the server's carried stack back to the client; it predicted picking up the button.
            this.broadcastChanges();
            return;
        }
        super.clicked(slotIndex, button, clickType, player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (slot == null || !slot.hasItem() || slot instanceof PagedSlot paged && paged.isPadding()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();

        if (index < this.layout.contentSlots()) {
            if (!this.moveItemStackTo(stack, this.layout.playerStart(), this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (index < this.layout.playerStart()) {
            return ItemStack.EMPTY;
        } else if (!this.moveItemStackTo(stack, 0, this.layout.contentSlots(), false)) {
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        if (stack.getCount() == original.getCount()) {
            return ItemStack.EMPTY;
        }
        slot.onTake(player, stack);
        return original;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.container.stillValid(player);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.container.stopOpen(player);
    }

    private static final class PagedSlot extends Slot {
        private final PagedContainerView view;
        private final int slotIndex;

        private PagedSlot(PagedContainerView container, int slotIndex, int x, int y) {
            super(container, slotIndex, x, y);
            this.view = container;
            this.slotIndex = slotIndex;
        }

        private boolean isPadding() {
            return !this.view.inRange(this.slotIndex);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return !this.isPadding() && this.view.canPlaceItem(this.slotIndex, stack);
        }

        @Override
        public boolean mayPickup(Player player) {
            return !this.isPadding() && super.mayPickup(player);
        }
    }
}
