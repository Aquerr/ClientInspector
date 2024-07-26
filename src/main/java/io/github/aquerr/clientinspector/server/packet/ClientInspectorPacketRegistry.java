package io.github.aquerr.clientinspector.server.packet;

import io.github.aquerr.clientinspector.ClientInspector;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.Channel;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.SimpleChannel;

public final class ClientInspectorPacketRegistry
{
    private static final int PROTOCOL_VERSION = 1;

    public static SimpleChannel INSTANCE;

    private ClientInspectorPacketRegistry()
    {

    }

    public static void registerPackets()
    {
        INSTANCE = ChannelBuilder.named(
                        ResourceLocation.fromNamespaceAndPath(ClientInspector.ID, "main"))
                .acceptedVersions(Channel.VersionTest.exact(PROTOCOL_VERSION))
                .simpleChannel()
                .messageBuilder(ModListPacketResponse.class).consumer(ModListPacketResponse::handlePacket).add()
                .messageBuilder(RequestModListPacket.class).consumer(RequestModListPacket::handlePacket).add()
                .build();
    }
}
