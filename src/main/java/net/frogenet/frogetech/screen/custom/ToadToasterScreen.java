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

    public ToadToasterScreen(ToadToasterMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mx, int my) {
        guiGraphics.blit(GUI_TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);

        if (menu.isCrafting()) {
            guiGraphics.blit(ARROW_TEXTURE,
                    leftPos + 73, topPos + 35,
                    0, 0,
                    menu.getScaledArrowProgress(), 16,
                    24, 16);
        }

        float energyProg = menu.getEnergyProgress();
        int barHeight = (int) (52 * energyProg);
        if (barHeight > 0) {
            guiGraphics.blit(GUI_TEXTURE,
                    leftPos + 152,
                    topPos + 18 + (52 - barHeight),
                    176,
                    28 + (52 - barHeight),
                    16,
                    barHeight,
                    256, 256);
        }
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float partialTick) {
        super.render(g, mx, my, partialTick);
        renderTooltip(g, mx, my);

        if (mx >= leftPos + 152 && mx <= leftPos + 168 &&
                my >= topPos + 18 && my <= topPos + 70) {
            g.renderTooltip(font,
                    Component.literal(menu.getEnergy() + " / " +
                            menu.getMaxEnergy() + " Qt"),
                    mx, my);
        }
    }
}
