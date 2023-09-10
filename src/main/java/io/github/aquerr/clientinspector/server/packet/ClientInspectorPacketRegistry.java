package io.github.aquerr.clientinspector.server.packet;

import io.github.aquerr.clientinspector.ClientInspector;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

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
        INSTANCE.registerMessage(MESSAGE_ID++, ModListPacketResponse.class, ModListPacketResponse::toBytes, ModListPacketResponse::fromBytes, ModListPacketResponse::handlePacket, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        INSTANCE.registerMessage(MESSAGE_ID++, RequestModListPacket.class, RequestModListPacket::toBytes, RequestModListPacket::fromBytes, RequestModListPacket::handlePacket, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }
}
