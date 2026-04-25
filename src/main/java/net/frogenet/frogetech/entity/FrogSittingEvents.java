package net.frogenet.frogetech.entity;

import net.frogenet.frogetech.block.entity.AbstractQuakMachineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public class FrogSittingEvents {

    @SubscribeEvent
    public void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getHand() != InteractionHand.MAIN_HAND) return;
        if (!event.getEntity().isShiftKeyDown()) return;
        if (!(event.getTarget() instanceof Frog vanillaFrog)) return;

        Level level = event.getLevel();
        if (level.isClientSide()) return;

        CompoundTag data = vanillaFrog.getPersistentData();
        if (!data.contains("FrogSittingX")) return;

        BlockPos sittingPos = new BlockPos(
                data.getInt("FrogSittingX"),
                data.getInt("FrogSittingY"),
                data.getInt("FrogSittingZ")
        );

        if (level.getBlockEntity(sittingPos) instanceof AbstractQuakMachineBlockEntity machine) {
            machine.releaseSittingFrog(level);
        } else {
            vanillaFrog.setNoAi(false);
            data.remove("FrogSittingX");
            data.remove("FrogSittingY");
            data.remove("FrogSittingZ");
        }

        event.setCanceled(true);
    }
}
