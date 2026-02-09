package net.frogenet.frogetech.screen.custom;

import net.frogenet.frogetech.Frogetech;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ToadToasterScreen extends AbstractContainerScreen<ToadToasterMenu> {


    private static final ResourceLocation GUI_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Frogetech.MODID, "textures/gui/toad_toaster/toad_toaster_gui.png");
    private static final ResourceLocation ARROW_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Frogetech.MODID, "textures/gui/arrow_progress.png");

    public ToadToasterScreen(ToadToasterMenu menu, Inventory pInventory, Component title) {
        super(menu, pInventory, title);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float v, int i, int i1) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        guiGraphics.blit(GUI_TEXTURE, x,y,0,0,imageWidth,imageHeight);

        renderProgressArrow(guiGraphics, x,y);
    }

    private void renderProgressArrow(GuiGraphics guiGraphics, int x, int y) {
        if(menu.isCrafting()){
            guiGraphics.blit(
                    ARROW_TEXTURE,
                    x + 73, y+35,
                    0,0,
                    menu.getScaledArrowProgress(),16,
                    24,16);
        }
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        this.renderTooltip(pGuiGraphics,pMouseX,pMouseY);
    }
}
