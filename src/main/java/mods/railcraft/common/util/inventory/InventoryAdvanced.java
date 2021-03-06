/*------------------------------------------------------------------------------
 Copyright (c) CovertJaguar, 2011-2018
 http://railcraft.info

 This code is the property of CovertJaguar
 and may only be used with explicit written
 permission unless otherwise specified on the
 license page at http://railcraft.info/wiki/info:license.
 -----------------------------------------------------------------------------*/
package mods.railcraft.common.util.inventory;

import mods.railcraft.common.blocks.RailcraftTileEntity;
import mods.railcraft.common.util.inventory.wrappers.IInventoryComposite;
import mods.railcraft.common.util.inventory.wrappers.IInventoryAdapter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.IInventoryChangedListener;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Creates a standalone instance of IInventory.
 * <p/>
 * Useful for hiding parts of an inventory from outsiders.
 *
 * @author CovertJaguar <http://www.railcraft.info>
 */
public class InventoryAdvanced extends InventoryBasic implements IInventoryAdapter, IInventoryComposite {

    public static final InventoryAdvanced ZERO_SIZE_INV = new InventoryAdvanced(0);

    private @Nullable Callback callback;
    private int inventoryStackLimit = 64;

    public InventoryAdvanced(int size, @Nullable String name) {
        super(name == null ? "Standalone" : name, false, size);
    }

    public InventoryAdvanced(int size) {
        this(size, null);
    }

    public InventoryAdvanced callbackInv(IInventory callback) {
        return callback(new CallbackInv(callback));
    }

    public InventoryAdvanced callbackTile(RailcraftTileEntity callback) {
        return callback(new CallbackTile(() -> callback));
    }

    public InventoryAdvanced callbackTile(Supplier<RailcraftTileEntity> callback) {
        return callback(new CallbackTile(callback));
    }

    public InventoryAdvanced callback(Callback callback) {
        this.callback = callback;
        addInventoryChangeListener(callback);
        return this;
    }

    public InventoryAdvanced phantom() {
        inventoryStackLimit = 127;
        return this;
    }

    @Override
    public Object getBackingObject() {
        return this;
    }

    @Override
    public int getNumSlots() {
        return getSizeInventory();
    }

    @Override
    public boolean hasCustomName() {
        return (callback != null && callback.hasCustomName()) || super.hasCustomName();
    }

    @Override
    public String getName() {
        if (callback != null && callback.hasCustomName()) {
            return callback.getName();
        }
        return super.getName();
    }

    public void setInventoryStackLimit(int inventoryStackLimit) {
        this.inventoryStackLimit = inventoryStackLimit;
    }

    @Override
    public int getInventoryStackLimit() {
        return inventoryStackLimit;
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer entityplayer) {
        return callback == null || callback.isUsableByPlayer(entityplayer);
    }

    @Override
    public void openInventory(EntityPlayer player) {
        if (callback != null) {
            callback.openInventory(player);
        }
    }

    @Override
    public void closeInventory(EntityPlayer player) {
        if (callback != null) {
            callback.closeInventory(player);
        }
    }

    public void writeToNBT(String tag, NBTTagCompound data) {
        InvTools.writeInvToNBT(this, tag, data);
    }

    public void readFromNBT(String tag, NBTTagCompound data) {
        NBTTagList list = data.getTagList(tag, 10);
        for (byte entry = 0; entry < list.tagCount(); entry++) {
            NBTTagCompound itemTag = list.getCompoundTagAt(entry);
            int slot = itemTag.getByte(InvTools.TAG_SLOT);
            if (slot >= 0 && slot < getSizeInventory()) {
                ItemStack stack = InvTools.readItemFromNBT(itemTag);
                setInventorySlotContents(slot, stack);
            }
        }
    }

    @Override
    public void clear() {
        super.clear();
        markDirty();
    }

    @Override
    public Stream<ItemStack> streamStacks() {
        return inventoryContents.stream().filter(InvTools::nonEmpty);
    }

    public abstract static class Callback implements IInventoryChangedListener {

        public boolean isUsableByPlayer(EntityPlayer player) {
            return true;
        }

        public void openInventory(EntityPlayer player) {
        }

        public void closeInventory(EntityPlayer player) {
        }

        public String getName() {
            return "Standalone";
        }

        public Boolean hasCustomName() {
            return false;
        }

    }

    public static class CallbackInv extends Callback {

        private final IInventory inv;

        public CallbackInv(IInventory inv) {
            this.inv = inv;
        }

        @Override
        public boolean isUsableByPlayer(EntityPlayer player) {
            return inv.isUsableByPlayer(player);
        }

        @Override
        public void openInventory(EntityPlayer player) {
            inv.openInventory(player);
        }

        @Override
        public void closeInventory(EntityPlayer player) {
            inv.closeInventory(player);
        }

        @Override
        public void onInventoryChanged(IInventory invBasic) {
            inv.markDirty();
        }

        @Override
        public String getName() {
            return inv.getName();
        }

        @Override
        public Boolean hasCustomName() {
            return inv.hasCustomName();
        }

    }

    public static class CallbackTile extends Callback {

        private final Supplier<RailcraftTileEntity> tile;

        public CallbackTile(Supplier<RailcraftTileEntity> tile) {
            this.tile = tile;
        }

        public Optional<RailcraftTileEntity> tile() {
            return Optional.ofNullable(tile.get());
        }

        @Override
        public void onInventoryChanged(IInventory invBasic) {
            tile().ifPresent(TileEntity::markDirty);
        }

        @Override
        public String getName() {
            return tile().map(RailcraftTileEntity::getName).orElse("");
        }

        @Override
        public Boolean hasCustomName() {
            return tile().map(RailcraftTileEntity::hasCustomName).orElse(false);
        }
    }
}
