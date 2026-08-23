package com.moregolems.util;

import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** Gemeinsame Kisten-Hilfsfunktionen für die Golem-Arbeiter-KIs (Wolle einlagern, Karotten ein-/auslagern, ...). */
public final class ContainerUtil {

    private ContainerUtil() {}

    /** Füllt zuerst passende, bereits vorhandene Stapel auf, dann leere Slots. Gibt zurück, was nicht hineinpasste. */
    public static ItemStack insert(Container container, ItemStack stack) {
        ItemStack remaining = stack.copy();

        for (int i = 0; i < container.getContainerSize() && !remaining.isEmpty(); i++) {
            ItemStack slot = container.getItem(i);
            if (slot.isEmpty() || !ItemStack.isSameItemSameComponents(slot, remaining)) continue;
            int space = slot.getMaxStackSize() - slot.getCount();
            if (space <= 0) continue;
            int moved = Math.min(space, remaining.getCount());
            slot.grow(moved);
            remaining.shrink(moved);
            container.setChanged();
        }

        for (int i = 0; i < container.getContainerSize() && !remaining.isEmpty(); i++) {
            if (!container.getItem(i).isEmpty()) continue;
            container.setItem(i, remaining.copy());
            remaining = ItemStack.EMPTY;
            container.setChanged();
        }

        return remaining;
    }

    /** Entnimmt bis zu [maxAmount] von [item] aus [container] (über mehrere Slots verteilt möglich). */
    public static ItemStack extract(Container container, Item item, int maxAmount) {
        int remaining = maxAmount;
        ItemStack result = ItemStack.EMPTY;

        for (int i = 0; i < container.getContainerSize() && remaining > 0; i++) {
            ItemStack slot = container.getItem(i);
            if (slot.isEmpty() || slot.getItem() != item) continue;
            int take = Math.min(remaining, slot.getCount());
            if (result.isEmpty()) {
                result = slot.copyWithCount(take);
            } else {
                result.grow(take);
            }
            slot.shrink(take);
            remaining -= take;
            container.setChanged();
        }

        return result;
    }

    /** Summe aller Stapel von [item] in [container]. */
    public static int countItem(Container container, Item item) {
        int total = 0;
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (!stack.isEmpty() && stack.getItem() == item) total += stack.getCount();
        }
        return total;
    }

    /** true, wenn [container] ausschließlich [expectedCount] Stück von [item] enthält und sonst nichts. */
    public static boolean containsOnly(Container container, Item item, int expectedCount) {
        int total = 0;
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (stack.isEmpty()) continue;
            if (stack.getItem() != item) return false;
            total += stack.getCount();
        }
        return total == expectedCount;
    }
}
