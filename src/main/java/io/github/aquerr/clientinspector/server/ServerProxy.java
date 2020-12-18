package io.github.aquerr.clientinspector.server;

import io.github.aquerr.clientinspector.Proxy;
import io.github.aquerr.clientinspector.server.config.Configuration;
import io.github.aquerr.clientinspector.server.listener.PlayerConnectListener;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ServerProxy implements Proxy
{
    @Override
    public void preInit(FMLPreInitializationEvent event)
    {
        Configuration.init();
        MinecraftForge.EVENT_BUS.register(new PlayerConnectListener());
    }

    @Override
    public void init(FMLInitializationEvent event)
    {

    }

    @Override
    public void postInit(FMLPostInitializationEvent event)
    {

    }
}
