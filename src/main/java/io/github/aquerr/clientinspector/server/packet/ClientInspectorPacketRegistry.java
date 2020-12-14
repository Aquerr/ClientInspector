package io.github.aquerr.clientinspector.server.packet;

import io.github.aquerr.clientinspector.ClientInspector;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class ClientInspectorPacketRegistry
{
    public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(ClientInspector.ID);

    public static void registerPackets()
    {
        INSTANCE.registerMessage(new ModListPacket.ModListPacketHandler(), ModListPacket.class, 0, Side.SERVER);
        INSTANCE.registerMessage(new ModListPacket.ModListPacketHandler(), ModListPacket.class, 0, Side.CLIENT);
        INSTANCE.registerMessage(new RequestModListPacket.RequestModListPacketHandler(), RequestModListPacket.class, 2, Side.CLIENT);
    }
}
