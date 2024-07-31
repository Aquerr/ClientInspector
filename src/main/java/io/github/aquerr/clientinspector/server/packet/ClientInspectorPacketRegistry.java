package io.github.aquerr.clientinspector.server.packet;

import io.github.aquerr.clientinspector.ClientInspector;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.Channel;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.SimpleChannel;

public final class ClientInspectorPacketRegistry
{
    private static final int PROTOCOL_VERSION = 1;

    public static final SimpleChannel INSTANCE = ChannelBuilder.named(ResourceLocation.fromNamespaceAndPath(ClientInspector.ID, "main"))
            .networkProtocolVersion(1)
            .acceptedVersions(Channel.VersionTest.exact(PROTOCOL_VERSION))
            .simpleChannel();

    private ClientInspectorPacketRegistry()
    {

    }

    public static void registerPackets()
    {
        INSTANCE.messageBuilder(ModListPacketResponse.class, NetworkDirection.PLAY_TO_SERVER)
                .decoder(ModListPacketResponse::fromBytes)
                .encoder(ModListPacketResponse::toBytes)
                .consumerMainThread(ModListPacketResponse::handlePacket)
                .add();

        INSTANCE.messageBuilder(RequestModListPacket.class, NetworkDirection.PLAY_TO_CLIENT)
                .decoder(RequestModListPacket::fromBytes)
                .encoder(RequestModListPacket::toBytes)
                .consumerMainThread(RequestModListPacket::handlePacket)
                .add();
    }
}
