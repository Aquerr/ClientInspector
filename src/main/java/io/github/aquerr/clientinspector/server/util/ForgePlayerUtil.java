package io.github.aquerr.clientinspector.server.util;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.NetworkManager;
import net.minecraftforge.fml.common.network.handshake.NetworkDispatcher;

import java.util.Map;
import java.util.Set;

public class ForgePlayerUtil
{
    public static Set<String> getPlayerMods(final EntityPlayerMP entityPlayerMP)
    {
        NetHandlerPlayServer networkHandlerPlayServer = entityPlayerMP.connection;
        NetworkManager networkManager = networkHandlerPlayServer.netManager;
        NetworkDispatcher networkDispatcher = NetworkDispatcher.get(networkManager);
        Map<String, String> modList = networkDispatcher.getModList();
        return modList.keySet();
    }
}
