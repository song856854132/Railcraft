/* 
 * Copyright (c) CovertJaguar, 2014 http://railcraft.info
 * 
 * This code is the property of CovertJaguar
 * and may only be used with explicit written
 * permission unless otherwise specified on the
 * license page at http://railcraft.info/wiki/info:license.
 */
package mods.railcraft.common.items;

import mods.railcraft.api.carts.CartTools;
import mods.railcraft.api.core.IOwnable;
import mods.railcraft.api.signals.SignalAspect;
import mods.railcraft.common.blocks.machine.TileMultiBlock;
import mods.railcraft.common.blocks.machine.TileMultiBlock.MultiBlockStateReturn;
import mods.railcraft.common.blocks.signals.IDualHeadSignal;
import mods.railcraft.common.blocks.signals.TileSignalBase;
import mods.railcraft.common.plugins.forge.*;
import mods.railcraft.common.util.misc.Game;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.EnumSet;
import java.util.List;

/**
 * @author CovertJaguar <http://www.railcraft.info>
 */
public class ItemMagnifyingGlass extends ItemRailcraft implements IActivationBlockingItem {

    public ItemMagnifyingGlass() {
        setMaxDamage(0);
        setMaxStackSize(1);
        setUnlocalizedName("railcraft.tool.magnifying.glass");
        setFull3D();

        setCreativeTab(CreativePlugin.RAILCRAFT_TAB);
    }

    @Override
    public void initializeDefinintion() {
        MinecraftForge.EVENT_BUS.register(this);
        LootPlugin.addLoot(RailcraftItems.magGlass, 1, 1, LootPlugin.Type.WORKSHOP);
    }

    @Override
    public void defineRecipes() {
        CraftingPlugin.addRecipe(new ItemStack(this),
                " G",
                "S ",
                'S', "stickWood",
                'G', "paneGlassColorless"
        );
    }

    @SuppressWarnings("unused")
    @SubscribeEvent
    public void onEntityInteract(EntityInteractEvent event) {
        EntityPlayer thePlayer = event.entityPlayer;

        Entity entity = event.target;

        ItemStack stack = thePlayer.getCurrentEquippedItem();
        if (stack != null && stack.getItem() instanceof ItemMagnifyingGlass)
            thePlayer.swingItem();

        if (Game.isNotHost(thePlayer.worldObj))
            return;

        if (stack != null && stack.getItem() instanceof ItemMagnifyingGlass)
            if (entity instanceof EntityMinecart) {
                EntityMinecart cart = (EntityMinecart) entity;
                ChatPlugin.sendLocalizedChatFromServer(thePlayer, "railcraft.gui.mag.glass.placedby", LocalizationPlugin.getEntityLocalizationTag(cart), CartTools.getCartOwner(cart));
                event.setCanceled(true);
            }
    }

    @Override
    public EnumActionResult onItemUseFirst(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
        if (Game.isNotHost(world))
            return EnumActionResult.PASS;
        TileEntity t = world.getTileEntity(pos);
        EnumActionResult returnValue = EnumActionResult.PASS;
        if (t instanceof IOwnable) {
            IOwnable ownable = (IOwnable) t;
            ChatPlugin.sendLocalizedChatFromServer(player, "railcraft.gui.mag.glass.placedby", ownable.getDisplayName(), ownable.getOwner());
            returnValue = EnumActionResult.SUCCESS;
        }
        if (t instanceof TileMultiBlock) {
            TileMultiBlock tile = (TileMultiBlock) t;
            if (tile.isStructureValid())
                ChatPlugin.sendLocalizedChatFromServer(player, "railcraft.multiblock.state.valid");
            else
                for (MultiBlockStateReturn returnState : EnumSet.complementOf(EnumSet.of(MultiBlockStateReturn.VALID))) {
                    List<Integer> pats = tile.patternStates.get(returnState);
                    if (!pats.isEmpty())
                        ChatPlugin.sendLocalizedChatFromServer(player, returnState.message, pats.toString());
                }
            returnValue = EnumActionResult.SUCCESS;
        }
        if (t instanceof IDualHeadSignal) {
            IDualHeadSignal signal = (IDualHeadSignal) t;
            SignalAspect top = signal.getTopAspect();
            SignalAspect bottom = signal.getBottomAspect();
            ChatPlugin.sendLocalizedChatFromServer(player, "railcraft.gui.mag.glass.aspect.dual", top.getLocalizationTag(), bottom.getLocalizationTag());
            returnValue = EnumActionResult.SUCCESS;
        } else if (t instanceof TileSignalBase) {
            ChatPlugin.sendLocalizedChatFromServer(player, "railcraft.gui.mag.glass.aspect", ((TileSignalBase) t).getSignalAspect().getLocalizationTag());
            returnValue = EnumActionResult.SUCCESS;
        }
        return returnValue;
    }
}
