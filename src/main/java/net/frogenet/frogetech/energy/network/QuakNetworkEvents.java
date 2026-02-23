package net.frogenet.frogetech.energy.network;


import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

public class QuakNetworkEvents {

    @SubscribeEvent
    public void onLevelTick(LevelTickEvent.Post event) {
        if (event.getLevel().isClientSide()) return;
        QuakNetworkManager.get(event.getLevel()).tick();
    }

    @SubscribeEvent
    public void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel().isClientSide()) return;
        QuakNetworkManager.onLevelUnload((Level)  event.getLevel());
    }
}
