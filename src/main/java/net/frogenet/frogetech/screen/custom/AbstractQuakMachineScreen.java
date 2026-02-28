package net.frogenet.frogetech.screen.custom;

import net.frogenet.frogetech.Frogetech;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

public abstract class AbstractQuakMachineScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {

    protected static final int BASE_WIDTH = 176;
    protected static final int PANEL_WIDTH = 38;
    protected static final int TAB_WIDTH = 12;
    protected static final int TAB_HEIGHT = 24;
    protected static final int TAB_Y_OFFSET = 8;

    protected static final ResourceLocation PANEL_TEXTURE =
            Frogetech.rl("textures/gui/upgrade_panel.png");
    protected static final float ANIMATION_SPEED = 0.15f;
    protected boolean isPanelOpen = false;
    protected float panelAnimation = 0.0f;

    public AbstractQuakMachineScreen(T menu, Inventory inv, Component title) {
        super(menu, inv, title);
    }

    @Override
    protected void init() {
        super.init();
        imageWidth = BASE_WIDTH;
    }

    protected int getCurrentPanelWidth() {
        return (int) (PANEL_WIDTH * panelAnimation);
    }

    protected boolean isOverTab(int mx, int my) {
        int tabX = leftPos + BASE_WIDTH;
        int tabY = topPos + TAB_Y_OFFSET;
        return mx >= tabX && mx <= tabX + TAB_WIDTH &&
                my >= tabY && my <= tabY + TAB_HEIGHT;
    }

    protected boolean isPanelFullyOpen() {
        return panelAnimation >= 1.0f;
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button == 0 && isOverTab((int) mx, (int) my)) {
            isPanelOpen = !isPanelOpen;
            return true;
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (isPanelOpen && panelAnimation < 1.0f) {
            panelAnimation = Math.min(1.0f, panelAnimation + ANIMATION_SPEED);
        } else if (!isPanelOpen && panelAnimation > 1.0f) {
            panelAnimation = Math.max(0.0f, panelAnimation - ANIMATION_SPEED);
        }
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mx, int my) {
        renderMainBg(guiGraphics, partialTick, mx, my);
        renderUpgradePanel(guiGraphics, mx, my);
    }

    protected abstract void renderMainBg(GuiGraphics g, float partialTick, int mx, int my);

    protected void renderUpgradePanel(GuiGraphics g, int mx, int my) {
        int panelX = leftPos + BASE_WIDTH;
        int tabY = topPos + TAB_Y_OFFSET;

        g.blit(PANEL_TEXTURE,
                panelX, tabY,
                isPanelOpen ? TAB_WIDTH : 0, 0,
                TAB_WIDTH, TAB_HEIGHT,
                64, 64);

        if (panelAnimation > 0.0f) {
            int currentWidth = getCurrentPanelWidth();

            g.blit(PANEL_TEXTURE,
                    panelX, topPos + TAB_Y_OFFSET + TAB_HEIGHT,
                    0, TAB_HEIGHT,
                    currentWidth, 60,
                    64, 64);

            if (isPanelFullyOpen()) {
                renderPanelContent(g, panelX, mx, my);
            }
        }
        if (isOverTab(mx, my)) {
            g.renderTooltip(font,
                    Component.literal(isPanelOpen ? "Close Upgrades" : "Open Upgrades"),
                    mx, my);
        }
    }

    protected void renderPanelContent(GuiGraphics g, int panelX, int mx, int my) {
        g.drawString(font,
                Component.literal("Upgrade"),
                panelX + 4, topPos + TAB_Y_OFFSET + TAB_HEIGHT + 4,
                0xFFFFFF, false);
    }

}
