package net.frogenet.frogetech.screen.custom;

import net.frogenet.frogetech.item.ModItems;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

public abstract class AbstractQuakMachineScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {

    protected static final int BASE_WIDTH = 176;
    protected static final int PANEL_WIDTH = 44;
    protected static final int PANEL_HEIGHT = 50;
    protected static final int ICON_PADDING = 4;
    protected static final int ICON_SIZE = 16;

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

    protected int getIconX() {
        return leftPos + BASE_WIDTH + ICON_PADDING;
    }

    protected int getIconY() {
        return topPos;
    }

    protected int getCurrentPanelWidth() {
        return (int) (PANEL_WIDTH * panelAnimation);
    }

    protected int getCurrentPanelHeight() {
        return (int) (PANEL_HEIGHT * panelAnimation);
    }

    protected boolean isOverIcon(int mx, int my) {
        int iconX = getIconX();
        int iconY = getIconY();
        return mx >= iconX && mx <= iconX + ICON_SIZE + ICON_PADDING * 2 &&
                my >= iconY && my <= iconY + ICON_SIZE + ICON_PADDING * 2;
    }

    protected boolean isPanelFullyOpen() {
        return panelAnimation >= 1.0f;
    }
    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button == 0 && isOverIcon((int) mx, (int) my)) {
            isPanelOpen = !isPanelOpen;
            return true;
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {

        ((AbstractQuakMachineMenu) menu).panelOpen = isPanelFullyOpen();

        if (isPanelOpen && panelAnimation < 1.0f) {
            panelAnimation = Math.min(1.0f, panelAnimation + ANIMATION_SPEED);
        } else if (!isPanelOpen && panelAnimation > 0.0f) {
            panelAnimation = Math.max(0.0f, panelAnimation - ANIMATION_SPEED);
        }
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mx, int my) {
        renderMainBg(guiGraphics, partialTick, mx, my);
        renderIcon(guiGraphics, mx, my);
        if (panelAnimation > 0.0f) {
            renderPanel(guiGraphics, mx, my);
        }
    }

    protected abstract void renderMainBg(GuiGraphics g, float partialTick, int mx, int my);

    private void renderIcon(GuiGraphics g, int mx, int my) {
        int iconX = getIconX();
        int iconY = getIconY();
        int iconBoxSize = ICON_SIZE/* + ICON_PADDING * 2*/;


        // Icon Hintergrund – kleines Kästchen
        g.fill(iconX - 1, iconY - 1,
                iconX + iconBoxSize + 1, iconY + iconBoxSize + 1,
                0xFF444444); // Rahmen

        g.fill(iconX, iconY,
                iconX + iconBoxSize, iconY + iconBoxSize,
                isPanelOpen ? 0xFF666666 : 0xFF333333); // Hintergrund – heller wenn offen

        // Gunk Ball Textur
        g.renderFakeItem(ModItems.GUNK_BALL.toStack(),
                iconX + ICON_PADDING, iconY + ICON_PADDING);

        // Tooltip
        if (isOverIcon(mx, my)) {
            g.renderTooltip(font,
                    Component.literal(isPanelOpen ? "Close Upgrades" : "Open Upgrades"),
                    mx, my);
        }
    }

    private void renderPanel(GuiGraphics g, int mx, int my) {
        int panelX = getIconX();
        int panelY = getIconY() + ICON_SIZE + ICON_PADDING * 2 + 2; // Direkt unter Icon
        int currentW = getCurrentPanelWidth();
        int currentH = getCurrentPanelHeight();

        if (currentW <= 0 || currentH <= 0) return;

        // Panel Rahmen
        g.fill(panelX - 1, panelY - 1,
                panelX + currentW + 1, panelY + currentH + 1,
                0xFF444444);

        // Panel Hintergrund
        g.fill(panelX, panelY,
                panelX + currentW, panelY + currentH,
                0xCC1A1A1A);

        // Akzentlinie oben (Orange wie beim HUD Overlay)
        g.fill(panelX, panelY,
                panelX + currentW, panelY + 2,
                0xFFFF8C00);

        // Inhalt nur wenn voll offen
        if (isPanelFullyOpen()) {
            renderPanelContent(g, panelX, panelY, mx, my);
        }
    }

    protected void renderPanelContent(GuiGraphics g, int panelX, int panelY, int mx, int my) {
        // "Upgrade" Label
        g.drawString(font,
                Component.literal("Upgrade"),
                panelX + 4, panelY + 6,
                0xFFFF8C00, false);

        // Slot-Rahmen um den Upgrade-Slot (Menu-Position: x=184, y=58)
        int slotScreenX = leftPos + 184 - 1;
        int slotScreenY = topPos + 58 - 1;
        g.fill(slotScreenX, slotScreenY,
                slotScreenX + 20, slotScreenY + 20,
                0xFF555555); // Slot Rahmen
    }

}
