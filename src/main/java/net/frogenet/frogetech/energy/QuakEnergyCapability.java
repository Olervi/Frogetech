package net.frogenet.frogetech.energy;


import net.frogenet.frogetech.Frogetech;
import net.minecraft.core.Direction;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

@EventBusSubscriber(modid = Frogetech.MODID, bus = EventBusSubscriber.Bus.MOD)
public class QuakEnergyCapability {

    public static final BlockCapability<IQuakStorage, Direction> QUAK_ENERGY =
            BlockCapability.createSided(
                    Frogetech.rl("quak_energy"),
                    IQuakStorage.class
            );

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event){
        //TBD
    }
}
