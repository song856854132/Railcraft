/*------------------------------------------------------------------------------
 Copyright (c) CovertJaguar, 2011-2017
 http://railcraft.info

 This code is the property of CovertJaguar
 and may only be used with explicit written
 permission unless otherwise specified on the
 license page at http://railcraft.info/wiki/info:license.
 -----------------------------------------------------------------------------*/
package mods.railcraft.common.plugins.forge;

import com.google.common.collect.ForwardingList;
import com.mojang.authlib.GameProfile;
import mods.railcraft.common.util.inventory.InvTools;
import mods.railcraft.common.util.misc.Game;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.util.WeightedSpawnerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author CovertJaguar <http://www.railcraft.info/>
 */
public final class NBTPlugin {
    @Nullable
    public static NBTTagCompound makeGameProfileTag(@Nullable GameProfile profile) {
        if (profile == null || (profile.getName() == null && profile.getId() == null))
            return null;
        NBTTagCompound nbt = new NBTTagCompound();
        NBTUtil.writeGameProfile(nbt, profile);
        return nbt;
    }

    public static GameProfile readGameProfileTag(NBTTagCompound data) {
        if (data.hasKey("Name")) {
            GameProfile ret = NBTUtil.readGameProfileFromNBT(data);
            return ret == null ? new GameProfile(null, PlayerPlugin.UNKNOWN_PLAYER_NAME) : ret;
        }
        String ownerName = PlayerPlugin.UNKNOWN_PLAYER_NAME;
        if (data.hasKey("name"))
            ownerName = data.getString("name");
        UUID ownerUUID = null;
        if (data.hasKey("id"))
            ownerUUID = UUID.fromString(data.getString("id"));
        return new GameProfile(ownerUUID, ownerName);
    }

    public static <T extends Enum<T>> void writeEnumOrdinal(NBTTagCompound data, String tag, Enum<T> e) {
        assert e.ordinal() < Byte.MAX_VALUE;
        data.setByte(tag, (byte) e.ordinal());
    }

    public static <T extends Enum<T>> T readEnumOrdinal(NBTTagCompound data, String tag, T[] enumConstants, T defaultValue) {
        if (data.hasKey(tag)) {
            byte ordinal = data.getByte(tag);
            return enumConstants[ordinal];
        }
        return defaultValue;
    }

    public static <T extends Enum<T>> void writeEnumName(NBTTagCompound data, String tag, Enum<T> e) {
        data.setString(tag, e.name());
    }

    public static <T extends Enum<T>> T readEnumName(NBTTagCompound data, String tag, T defaultValue) {
        if (data.hasKey(tag)) {
            String name = data.getString(tag);
            return Enum.valueOf(defaultValue.getClass().asSubclass(Enum.class), name);
        }
        return defaultValue;
    }

    public static void writeUUID(NBTTagCompound data, String tag, @Nullable UUID uuid) {
        if (uuid == null)
            return;
        NBTTagCompound nbtTag = NBTUtil.createUUIDTag(uuid);
        data.setTag(tag, nbtTag);
    }

    @Nullable
    public static UUID readUUID(NBTTagCompound data, String tag) {
        if (data.hasKey(tag)) {
            NBTTagCompound nbtTag = data.getCompoundTag(tag);
            if (nbtTag.hasKey("most")) {
                return new UUID(nbtTag.getLong("most"), nbtTag.getLong("least"));
            } else {
                return NBTUtil.getUUIDFromTag(nbtTag);
            }
        }
        return null;
    }

    public static void writeBlockPos(NBTTagCompound data, String tag, BlockPos pos) {
        data.setIntArray(tag, new int[]{pos.getX(), pos.getY(), pos.getZ()});
    }

    @Nullable
    public static BlockPos readBlockPos(NBTTagCompound data, String tag) {
        if (data.hasKey(tag)) {
            if (data.hasKey(tag, NBT.TAG_INT_ARRAY)) {
                int[] c = data.getIntArray(tag);
                return new BlockPos(c[0], c[1], c[2]);
            } else {
                return NBTUtil.getPosFromTag(data.getCompoundTag(tag));
            }
        }
        return null;
    }

    public static void writeItemStack(NBTTagCompound data, String tag, @Nullable ItemStack stack) {
        NBTTagCompound nbt = new NBTTagCompound();
        if (!InvTools.isEmpty(stack))
            stack.writeToNBT(nbt);
        data.setTag(tag, nbt);
    }

    public static ItemStack readItemStack(NBTTagCompound data, String tag) {
        if (data.hasKey(tag)) {
            NBTTagCompound nbt = data.getCompoundTag(tag);
            return new ItemStack(nbt);
        }
        return InvTools.emptyStack();
    }

    public static WeightedSpawnerEntity obtainEntityTagSafe(World world, ItemStack stack, GameProfile player) {
        NBTTagCompound rootTag = stack.getTagCompound();

        if (rootTag == null || !rootTag.hasKey("EntityTag", 10)) {
            return new WeightedSpawnerEntity();
        }
        NBTTagCompound entityTag = rootTag.getCompoundTag("EntityTag");
        Entity entity = EntityList.createEntityFromNBT(entityTag, world);
        if (entity == null) {
            return new WeightedSpawnerEntity();
        }

        if (Game.isHost(world) && (entity.ignoreItemEntityData() && !PlayerPlugin.isPlayerOp(player))) {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setString("id", entityTag.getString("id")); // security concerns
            return new WeightedSpawnerEntity(1, tag);
        }

        NBTTagCompound target = entity.writeToNBT(new NBTTagCompound());

        target.removeTag("UUIDMost");
        target.removeTag("UUIDLeast");

        return new WeightedSpawnerEntity(1, target);
    }

    public static ItemStack copyTag(ItemStack original, ItemStack tagFrom) {
        ItemStack copy = InvTools.copy(original);
        copy.setTagCompound(tagFrom.getTagCompound());
        return copy;
    }

    public enum EnumNBTType {

        END(NBTTagEnd.class),
        BYTE(NBTTagByte.class),
        SHORT(NBTTagShort.class),
        INT(NBTTagInt.class),
        LONG(NBTTagLong.class),
        FLOAT(NBTTagFloat.class),
        DOUBLE(NBTTagDouble.class),
        BYTE_ARRAY(NBTTagByteArray.class),
        STRING(NBTTagString.class),
        LIST(NBTTagList.class),
        COMPOUND(NBTTagCompound.class),
        INT_ARRAY(NBTTagIntArray.class);
        public static final EnumNBTType[] VALUES = values();
        private static final Map<Class<? extends NBTBase>, EnumNBTType> classToType = new HashMap<>();
        public final Class<? extends NBTBase> classObject;

        EnumNBTType(Class<? extends NBTBase> c) {
            this.classObject = c;
        }

        static {
            for (EnumNBTType each : VALUES) {
                classToType.put(each.classObject, each);
            }
        }

        public static EnumNBTType fromClass(Class<? extends NBTBase> c) {
            return classToType.get(c);
        }

    }

    public static <T extends NBTBase> List<T> getNBTList(NBTTagCompound nbt, String tag, EnumNBTType type) {
        NBTTagList nbtList = nbt.getTagList(tag, type.ordinal());
        return new NBTList<>(nbtList);
    }

    public static <T extends NBTBase> List<T> asList(NBTTagList list) {
        return new NBTList<>(list);
    }

    private static final class NBTList<T extends NBTBase> extends ForwardingList<T> {

        private final List<T> backingList;

        @SuppressWarnings("unchecked")
        NBTList(NBTTagList nbtList) {
            backingList = (List<T>) nbtList.tagList;
        }

        @Override
        protected List<T> delegate() {
            return backingList;
        }

    }
}
