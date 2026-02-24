package net.frogenet.frogetech.screen.custom;

import net.frogenet.frogetech.Frogetech;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class CoalGeneratorScreen extends AbstractContainerScreen<CoalGeneratorMenu> {

    private static final ResourceLocation GUI =
            ResourceLocation.fromNamespaceAndPath(Frogetech.MODID,
                    "textures/gui/coal_generator/coal_generator_gui.png");

    public CoalGeneratorScreen(CoalGeneratorMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mx, int my) {
        guiGraphics.blit(GUI, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);

        float burnProg = menu.getBurnProgress();
        int flameHeight = (int) (14 * burnProg);
        if (flameHeight > 0) {
            guiGraphics.blit(GUI,
                    leftPos + 80,
                    topPos + 56 + (14 - flameHeight),
                    176,
                    14 - flameHeight,
                    14,
                    flameHeight,
                    256, 256);
        }
        float energyProg = menu.getEnergyProgress();
        int barHeight = (int) (52 * energyProg);
        if (barHeight > 0) {
            guiGraphics.blit(GUI,
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
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);

        if (mouseX >= leftPos + 152 && mouseX <= leftPos + 167 &&
                mouseY >= topPos + 18 && mouseY <= topPos + 70) {
            guiGraphics.renderTooltip(font,
                    Component.literal(menu.getEnergy() + "/" +
                            menu.getMaxEnergy() + "Qt"),
                    mouseX, mouseY);
        }
    }
}
