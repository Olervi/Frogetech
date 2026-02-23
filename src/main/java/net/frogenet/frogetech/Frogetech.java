package net.frogenet.frogetech;

import com.mojang.logging.LogUtils;
import net.frogenet.frogetech.block.ModBlocks;
import net.frogenet.frogetech.block.entity.ModBlockEntities;
import net.frogenet.frogetech.energy.network.QuakNetworkEvents;
import net.frogenet.frogetech.entity.ModEntities;
import net.frogenet.frogetech.entity.client.FrogRenderer;
import net.frogenet.frogetech.entity.custom.FrogEntity;
import net.frogenet.frogetech.item.ModCreativeModeTabs;
import net.frogenet.frogetech.item.ModItems;
import net.frogenet.frogetech.screen.ModMenuTypes;
import net.frogenet.frogetech.screen.custom.PedestalScreen;
import net.frogenet.frogetech.screen.custom.ToadToasterScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.frogenet.frogetech.block.entity.renderer.PedestalBlockEntityRenderer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(Frogetech.MODID)
public class Frogetech {
    public static final String MODID = "frogetech";
    private static final Logger LOGGER = LogUtils.getLogger();

    public Frogetech(IEventBus modEventBus, ModContainer modContainer){
        modEventBus.addListener(this::commonSetup);

        NeoForge.EVENT_BUS.register(this);
        NeoForge.EVENT_BUS.register(new QuakNetworkEvents());

        ModCreativeModeTabs.register(modEventBus);
        ModMenuTypes.register(modEventBus);

        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModEntities.register(modEventBus);

        modEventBus.addListener(this::addCreative);
    }

    public static ResourceLocation rl (String path){
        return ResourceLocation.fromNamespaceAndPath(MODID,path);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {

    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS){
            event.accept(ModItems.GUNK_BALL);
        }
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS){
            event.accept(ModBlocks.SOLID_GUNK);
        }

    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {

    }

    @EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {

        @SubscribeEvent
        public static void registerBER(EntityRenderersEvent.RegisterRenderers event) {
            event.registerBlockEntityRenderer(ModBlockEntities.PEDESTAL_BE.get(), PedestalBlockEntityRenderer::new);

            event.registerEntityRenderer(ModEntities.FROG.get(), FrogRenderer::new);
        }

        @SubscribeEvent
        public static void registerScreens(RegisterMenuScreensEvent event){
            event.register(ModMenuTypes.PEDESTAL_MENU.get(), PedestalScreen::new);
            event.register(ModMenuTypes.TOAD_TOASTER_MENU.get(), ToadToasterScreen::new);
        }

        @SubscribeEvent
        public static void registerAttributes(EntityAttributeCreationEvent event) {
            event.put(ModEntities.FROG.get(), FrogEntity.createAttributes().build());
        }
    }
}
