package io.github.aquerr.clientinspector.server;

import io.github.aquerr.clientinspector.server.config.Configuration;
import io.github.aquerr.clientinspector.server.listener.PlayerConnectListener;
import net.minecraftforge.common.MinecraftForge;

public class ServerProxy
{
    public static void init()
    {
        Configuration.init();
        MinecraftForge.EVENT_BUS.register(new PlayerConnectListener());
    }
}
