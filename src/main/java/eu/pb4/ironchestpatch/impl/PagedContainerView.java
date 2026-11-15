package eu.pb4.ironchestpatch.impl;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Exposes one page of a chest inventory as a container.
 *
 * <p>Slots keep a stable index while the page changes, so the vanilla menu can refresh the whole
 * view instead of rebuilding its slot list. The page size is chosen by {@link ChestLayout}.
 */
public final class PagedContainerView implements Container {
    private final Container parent;
    private final int pageSize;
    private int page;

    public PagedContainerView(Container parent, int pageSize) {
        this.parent = parent;
        this.pageSize = pageSize;
    }

    public Container parent() {
        return this.parent;
    }

    public int page() {
        return this.page;
    }

    public int pageSize() {
        return this.pageSize;
    }

    public void setPage(int page) {
        this.page = Math.max(0, page);
    }

    private int realIndex(int slot) {
        return this.page * this.pageSize + slot;
    }

    public boolean inRange(int slot) {
        if (slot < 0 || slot >= this.pageSize) {
            return false;
        }
        int index = this.realIndex(slot);
        return index >= 0 && index < this.parent.getContainerSize();
    }

    @Override
    public int getContainerSize() {
        return this.pageSize;
    }

    @Override
    public boolean isEmpty() {
        for (int slot = 0; slot < this.getContainerSize(); slot++) {
            if (!this.getItem(slot).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return this.inRange(slot) ? this.parent.getItem(this.realIndex(slot)) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        return this.inRange(slot) ? this.parent.removeItem(this.realIndex(slot), amount) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return this.inRange(slot) ? this.parent.removeItemNoUpdate(this.realIndex(slot)) : ItemStack.EMPTY;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (this.inRange(slot)) {
            this.parent.setItem(this.realIndex(slot), stack);
        }
    }

    @Override
    public void setChanged() {
        this.parent.setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return this.parent.stillValid(player);
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return this.inRange(slot) && this.parent.canPlaceItem(this.realIndex(slot), stack);
    }

    @Override
    public void clearContent() {
        for (int slot = 0; slot < this.getContainerSize(); slot++) {
            this.setItem(slot, ItemStack.EMPTY);
        }
    }
}
