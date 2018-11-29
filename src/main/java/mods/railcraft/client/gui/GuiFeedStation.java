/*------------------------------------------------------------------------------
 Copyright (c) CovertJaguar, 2011-2018
 http://railcraft.info

 This code is the property of CovertJaguar
 and may only be used with explicit written
 permission unless otherwise specified on the
 license page at http://railcraft.info/wiki/info:license.
 -----------------------------------------------------------------------------*/
package mods.railcraft.client.gui;

import mods.railcraft.common.blocks.machine.equipment.TileFeedStation;
import mods.railcraft.common.core.RailcraftConstants;
import mods.railcraft.common.gui.containers.ContainerFeedStation;
import mods.railcraft.common.plugins.forge.LocalizationPlugin;
import net.minecraft.entity.player.InventoryPlayer;

public class GuiFeedStation extends TileGui {

    private final String feed = LocalizationPlugin.translate("gui.railcraft.feed.station.feed");

    public GuiFeedStation(InventoryPlayer playerInv, TileFeedStation tile) {
        super(tile, new ContainerFeedStation(playerInv, tile), RailcraftConstants.GUI_TEXTURE_FOLDER + "gui_single_slot.png");
        xSize = 176;
        ySize = 140;
        TileFeedStation tile1 = tile;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
        fontRenderer.drawString(feed, 85, 29, 0x404040);
    }

}
