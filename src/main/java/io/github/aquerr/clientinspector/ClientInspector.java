package io.github.aquerr.clientinspector;

import io.github.aquerr.clientinspector.server.packet.ClientInspectorPacketRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;

@Mod(modid = ClientInspector.ID, name = ClientInspector.NAME, version = ClientInspector.VERSION, acceptedMinecraftVersions = "1.12.2", acceptableRemoteVersions = "*")
public class ClientInspector
{
    public static final String ID = "clientinspector",
            NAME = "Client Inspector",
            VERSION = "1.1.0";

    @SidedProxy(clientSide = "io.github.aquerr.clientinspector.client.ClientProxy", serverSide = "io.github.aquerr.clientinspector.server.ServerProxy", modId = ClientInspector.ID)
    public static Proxy PROXY;

    @Mod.Instance(ClientInspector.ID)
    private static ClientInspector INSTANCE;

    public static ClientInspector getInstance()
    {
        return INSTANCE;
    }

    private Logger logger;

    @Mod.EventHandler
    public void onPreInit(FMLPreInitializationEvent event)
    {
        INSTANCE = this;
        this.logger = event.getModLog();
        PROXY.preInit(event);
    }

    @Mod.EventHandler
    public void onInit(FMLInitializationEvent event)
    {
        PROXY.init(event);

        this.logger.info("Initializing " + NAME);

        ClientInspectorPacketRegistry.registerPackets();

        this.logger.info("Mod load completed!");
    }

    @Mod.EventHandler
    public void onPostInit(FMLPostInitializationEvent event)
    {
        PROXY.postInit(event);
    }

    public Logger getLogger()
    {
        return logger;
    }
}
