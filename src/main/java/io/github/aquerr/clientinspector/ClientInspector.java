package io.github.aquerr.clientinspector;

import io.github.aquerr.clientinspector.server.ServerProxy;
import io.github.aquerr.clientinspector.server.config.Configuration;
import io.github.aquerr.clientinspector.server.packet.ClientInspectorPacketRegistry;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(ClientInspector.ID)
public class ClientInspector
{
    public static final String ID = "clientinspector";

    private static final Logger LOGGER = LogManager.getLogger();

    public ClientInspector()
    {

        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);
    }

    private void setup(final FMLCommonSetupEvent event)
    {
        LOGGER.info("Initializing " + ID);

        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Configuration.SERVER_SPEC);

        ServerProxy.init();
        ClientInspectorPacketRegistry.registerPackets();

        LOGGER.info("Mod load completed!");
    }

    private void doClientStuff(final FMLClientSetupEvent event)
    {

    }
}
