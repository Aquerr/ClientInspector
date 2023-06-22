package io.github.aquerr.clientinspector.server.packet;

import io.github.aquerr.clientinspector.ClientInspector;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;

public final class ClientInspectorPacketRegistry
{
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(ClientInspector.ID, "main"),
            () -> PROTOCOL_VERSION,
            version -> version.equals(PROTOCOL_VERSION),
            version -> version.equals(PROTOCOL_VERSION)
    );

    private ClientInspectorPacketRegistry()
    {

    }

    public static void registerPackets()
    {
        int messageId = 0;
        INSTANCE.registerMessage(++messageId, ModListPacket.class, ModListPacket::toBytes, ModListPacket::fromBytes, ModListPacket::handlePacket, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        INSTANCE.registerMessage(++messageId, RequestModListPacket.class, RequestModListPacket::toBytes, RequestModListPacket::fromBytes, RequestModListPacket::handlePacket, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }
}
