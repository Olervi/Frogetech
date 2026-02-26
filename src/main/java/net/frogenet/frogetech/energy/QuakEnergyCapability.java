package net.frogenet.frogetech.energy;


import net.frogenet.frogetech.Frogetech;
import net.frogenet.frogetech.block.entity.ModBlockEntities;
import net.minecraft.core.Direction;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

@EventBusSubscriber(modid = Frogetech.MODID, bus = EventBusSubscriber.Bus.MOD)
public class QuakEnergyCapability {

    public static final BlockCapability<IQuakStorage, Direction> QUAK_ENERGY =
            BlockCapability.createSided(
                    Frogetech.rl("quak_energy"),
                    IQuakStorage.class
            );

    public static final ItemCapability<IQuakStorage, Void> QUAK_ENERGY_ITEM =
            ItemCapability.createVoid(
                    Frogetech.rl("quak_energy_item"),
                    IQuakStorage.class
            );

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event){
        //Coal Gen

        event.registerBlockEntity(
                QUAK_ENERGY,
                ModBlockEntities.COAL_GENERATOR_BE.get(),
                (be, side) -> {
                    //If Facing exists
//                    if( side != null && side == be.getOutputSide()) return be.energyStorage;
//                    return null;
                    return be.energyStorage;
                }
        );

        //Furnace

        event.registerBlockEntity(
                QUAK_ENERGY,
                ModBlockEntities.TOAD_TOASTER_BE.get(),
                (be, side) -> {
                    return be.energyStorage;
                }
        );
    }
}
