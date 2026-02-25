package net.frogenet.frogetech.client;

import net.frogenet.frogetech.block.custom.CableBlock;
import net.frogenet.frogetech.block.entity.CableBlockEntity;
import net.frogenet.frogetech.item.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

public class QuakHudOverlay {

    @SubscribeEvent
    public void onRenderGuiLayer(RenderGuiLayerEvent.Post event) {
        if (!event.getName().equals(VanillaGuiLayers.CROSSHAIR)) return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        Level level = mc.level;
        if (player == null || level == null) return;

        ItemStack mainHand = player.getMainHandItem();
        if (!mainHand.is(ModItems.GUNK_BALL.get())) return;

        HitResult hit = mc.hitResult;
        if (hit == null || hit.getType() != HitResult.Type.BLOCK) return;

        BlockPos pos = ((BlockHitResult) hit).getBlockPos();
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof CableBlock)) return;

        if (!(level.getBlockEntity(pos) instanceof CableBlockEntity cable)) return;
        //QuakNetwork network = cable.getNetwork();
        //if(network == null) return;

        int throughput = cable.getCachedThroughput();

        renderOverlay(event.getGuiGraphics(), mc, throughput, pos);
    }

    private void renderOverlay(GuiGraphics g, Minecraft mc, int throughput, BlockPos pos) {
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        String title = "Quak Cable";
        String throughputText = throughput + "Qt/t";
        String posText = "(" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ")";

        int padding = 5;
        int lineHeight = 10;
        int boxWidth = 90;
        int boxHeight = padding * 2 + lineHeight * 3 + 4;

        int crossX = screenWidth / 2;
        int crossY = screenHeight / 2;
        int boxX = crossX + 16;
        int boxY = crossY - boxHeight - 4;

        g.fill(boxX - 1, boxY - 1,
                boxX + boxWidth + 1, boxY + boxHeight + 1,
                0x142814FF);
        g.fill(boxX, boxY,
                boxX + boxWidth, boxY + boxHeight,
                0xCC000000);

        g.fill(boxX, boxY,
                boxX + boxWidth, boxY + 2,
                0xFFFF8C00);

        int textX = boxX + padding;
        int textY = boxY + padding;

        g.drawString(mc.font, Component.literal(title),
                textX, textY, 0xFFFF8C00, false);
        textY += lineHeight + 2;

        g.drawString(mc.font, Component.literal("Flow: " + throughputText),
                textX, textY, 0xFFFFFFFF, false);
        textY += lineHeight;

        g.drawString(mc.font, Component.literal(posText),
                textX, textY, 0x888888FF, false);
    }
}
