/*------------------------------------------------------------------------------
 Copyright (c) CovertJaguar, 2011-2018
 http://railcraft.info

 This code is the property of CovertJaguar
 and may only be used with explicit written
 permission unless otherwise specified on the
 license page at http://railcraft.info/wiki/info:license.
 -----------------------------------------------------------------------------*/
package mods.railcraft.common.util.inventory.manipulators;

import mods.railcraft.common.util.inventory.iterators.IInvSlot;
import mods.railcraft.common.util.inventory.iterators.InventoryIterator;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

import static mods.railcraft.common.util.inventory.InvTools.*;

/**
 * @author CovertJaguar <http://www.railcraft.info>
 */
public class ItemHandlerInventoryManipulator extends InventoryManipulator<IInvSlot> {

    private final IItemHandler inv;

    protected ItemHandlerInventoryManipulator(IItemHandler inv) {
        this.inv = inv;
    }

    @Override
    public Iterator<IInvSlot> iterator() {
        return InventoryIterator.getForge(inv).iterator();
    }

    @Override
    protected ItemStack addStack(ItemStack stack, boolean doAdd) {
        if (isEmpty(stack))
            return emptyStack();
        stack = stack.copy();
        List<IInvSlot> filledSlots = new ArrayList<>(inv.getSlots());
        List<IInvSlot> emptySlots = new ArrayList<>(inv.getSlots());
        for (IInvSlot slot : InventoryIterator.getForge(inv)) {
            if (slot.canPutStackInSlot(stack)) {
                if (isEmpty(slot.getStack()))
                    emptySlots.add(slot);
                else
                    filledSlots.add(slot);
            }
        }

        int injected = 0;
        injected = tryPut(filledSlots, stack, injected, doAdd);
        injected = tryPut(emptySlots, stack, injected, doAdd);
        decSize(stack, injected);
        if (isEmpty(stack))
            return emptyStack();
        return stack;
    }

    private int tryPut(List<IInvSlot> slots, ItemStack stack, int injected, boolean doAdd) {
        if (injected >= sizeOf(stack))
            return injected;
        for (IInvSlot slot : slots) {
            ItemStack stackToInsert = stack.copy();
            int amountToInsert = sizeOf(stack) - injected;
            setSize(stackToInsert, amountToInsert);
            ItemStack remainder = inv.insertItem(slot.getIndex(), stackToInsert, !doAdd);
            if (isEmpty(remainder))
                return sizeOf(stack);
            injected += amountToInsert - sizeOf(remainder);
            if (injected >= sizeOf(stack))
                return injected;
        }
        return injected;
    }

    @Override
    protected List<ItemStack> removeItem(Predicate<ItemStack> filter, int maxAmount, boolean doRemove) {
        int amountNeeded = maxAmount;
        List<ItemStack> outputList = new ArrayList<>();
        for (IInvSlot slot : this) {
            if (amountNeeded <= 0)
                break;
            ItemStack stack = slot.getStack();
            if (!isEmpty(stack) && slot.canTakeStackFromSlot(stack) && filter.test(stack)) {
                ItemStack removed = inv.extractItem(slot.getIndex(), amountNeeded, !doRemove);
                if (!isEmpty(removed)) {
                    amountNeeded -= sizeOf(removed);
                    outputList.add(removed);
                }
            }
        }
        return outputList;
    }
}
