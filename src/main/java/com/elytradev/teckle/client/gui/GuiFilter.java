/*
 *    Copyright 2017 Benjamin K (darkevilmac)
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.elytradev.teckle.client.gui;

import com.elytradev.teckle.common.container.ContainerFilter;
import com.elytradev.teckle.common.network.messages.FilterColourChangeMessage;
import com.elytradev.teckle.common.tile.TileFilter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.ResourceLocation;

/**
 * Created by darkevilmac on 4/12/2017.
 */
public class GuiFilter extends GuiContainer {

    public static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation("teckle", "textures/gui/filter.png");
    public final TileFilter filter;
    private GuiColourPicker colourPicker;

    public GuiFilter(TileFilter tile, EntityPlayer player) {
        super(new ContainerFilter(tile, player));
        this.filter = tile;
    }

    @Override
    public void initGui() {
        super.initGui();

        buttonList.clear();
        colourPicker = new GuiColourPicker(1, guiLeft + 115, guiTop + 61);
        buttonList.add(colourPicker);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
        GlStateManager.color(1, 1, 1);
        Minecraft.getMinecraft().getTextureManager().bindTexture(BACKGROUND_TEXTURE);
        drawTexturedModalRect((width - xSize) / 2, (height - ySize) / 2, 0, 0, xSize, ySize);
        GlStateManager.enableLighting();
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button == colourPicker) {
            if (filter.colour == null) {
                filter.colour = EnumDyeColor.byMetadata(0);
            } else {
                if (filter.colour.getMetadata() == 15) {
                    filter.colour = null;
                } else {
                    filter.colour = EnumDyeColor.byMetadata(filter.colour.getMetadata() + 1);
                }
            }

            new FilterColourChangeMessage(filter.getPos(), filter.colour).sendToServer();
        }
    }

    public class GuiColourPicker extends GuiButton {

        public GuiColourPicker(int buttonId, int x, int y) {
            super(buttonId, x, y, 9, 9, "");
        }

        @Override
        public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
            if (this.visible) {
                mc.getTextureManager().bindTexture(new ResourceLocation("teckle", "textures/gui/filter.png"));
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                int yOffset = 0;
                int xOffset = 176;
                this.drawTexturedModalRect(this.x, this.y, xOffset, yOffset, this.width, this.height);

                if (GuiFilter.this.filter.colour != null) {
                    float[] sheepColour = EntitySheep.getDyeRgb(GuiFilter.this.filter.colour);
                    GlStateManager.pushMatrix();
                    GlStateManager.color(sheepColour[0], sheepColour[1], sheepColour[2]);
                    this.drawTexturedModalRect(this.x + 1, this.y + 1, xOffset + 10, yOffset + 1, this.width - 2, this.height - 2);
                    GlStateManager.popMatrix();
                }
            }
        }
    }
}
