package io.github.aquerr.clientinspector.server;

import io.github.aquerr.clientinspector.ClientInspector;
import io.github.aquerr.clientinspector.server.config.Configuration;
import io.github.aquerr.clientinspector.server.listener.PlayerConnectListener;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.loading.FMLPaths;

public final class ServerProxy
{
    public static void init()
    {
        Configuration.init(FMLPaths.CONFIGDIR.get().resolve(ClientInspector.ID).resolve("config.toml"));
        MinecraftForge.EVENT_BUS.register(new PlayerConnectListener());
    }
}
