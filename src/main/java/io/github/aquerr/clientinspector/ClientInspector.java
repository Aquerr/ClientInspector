package io.github.aquerr.clientinspector;

import io.github.aquerr.clientinspector.server.ServerProxy;
import io.github.aquerr.clientinspector.server.packet.ClientInspectorPacketRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
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
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::serverSetup);
    }

    private void setup(final FMLCommonSetupEvent event)
    {
        LOGGER.info("Initializing " + ID);
        ClientInspectorPacketRegistry.registerPackets();
    }

    private void serverSetup(final FMLDedicatedServerSetupEvent event)
    {
        LOGGER.info("Initializing server side {}", ID);
        ServerProxy.init();
        LOGGER.info("Mod load completed!");
    }
}
