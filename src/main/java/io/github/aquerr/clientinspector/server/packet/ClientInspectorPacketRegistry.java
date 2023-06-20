package io.github.aquerr.clientinspector.server.packet;

import io.github.aquerr.clientinspector.ClientInspector;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;

public class ClientInspectorPacketRegistry
{
    private static final String PROTOCOL_VERSION = "1";
    private static int MESSAGE_ID = 1;

    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(ClientInspector.ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void registerPackets()
    {
        INSTANCE.registerMessage(MESSAGE_ID++, ModListPacket.class, ModListPacket::toBytes, ModListPacket::fromBytes, ModListPacket::handlePacket, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        INSTANCE.registerMessage(MESSAGE_ID++, RequestModListPacket.class, RequestModListPacket::toBytes, RequestModListPacket::fromBytes, RequestModListPacket::handlePacket, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }
}
