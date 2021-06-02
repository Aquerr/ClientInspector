package io.github.aquerr.clientinspector.server.packet;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.loading.moddiscovery.ModDiscoverer;
import net.minecraftforge.fml.loading.moddiscovery.ModInfo;
import net.minecraftforge.fml.network.NetworkEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class RequestModListPacket implements ClientInspectorPacket
{
    private static final Logger LOGGER = LogManager.getLogger(RequestModListPacket.class);

    public static RequestModListPacket fromBytes(PacketBuffer buf)
    {
        return new RequestModListPacket();
    }

    public static PacketBuffer toBytes(RequestModListPacket requestModListPacket, PacketBuffer buffer)
    {
        return buffer;
    }

    public static void handlePacket(RequestModListPacket modListPacket, Supplier<NetworkEvent.Context> contextSupplier)
    {
        LOGGER.info("Sending mod-list packet to the server...");
        ModDiscoverer modDiscoverer = new ModDiscoverer(Collections.emptyMap());

        // Thanks to forge team for making this utility static method :D
        final List<String> modList = modDiscoverer.discoverMods().getLoadingModList()
                        .getMods()
                        .stream()
                        .map(ModInfo::getModId)
                        .collect(Collectors.toList());

        LOGGER.info("Found mod-list: " + Arrays.toString(modList.toArray()));
        ClientInspectorPacketRegistry.INSTANCE.sendToServer(new ModListPacket(modList));
    }
}
