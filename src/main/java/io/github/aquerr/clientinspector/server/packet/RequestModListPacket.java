package io.github.aquerr.clientinspector.server.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.discovery.ContainerType;
import net.minecraftforge.fml.common.discovery.ModCandidate;
import net.minecraftforge.fml.common.discovery.ModDiscoverer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.libraries.LibraryManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class RequestModListPacket implements IMessage
{
    @Override
    public void fromBytes(ByteBuf buf)
    {

    }

    @Override
    public void toBytes(ByteBuf buf)
    {

    }

    public static class RequestModListPacketHandler implements IMessageHandler<RequestModListPacket, ModListPacket>
    {
        private static final Logger LOGGER = LogManager.getLogger(RequestModListPacketHandler.class);

        @Override
        public ModListPacket onMessage(RequestModListPacket message, MessageContext ctx)
        {
            LOGGER.info("Sending mod-list packet to the server...");

            ModDiscoverer modDiscoverer = new ModDiscoverer();

            // Thanks to forge team for making this utility static method :D
            final List<File> modFiles = LibraryManager.gatherLegacyCanidates(Minecraft.getMinecraft().gameDir);
            for (final File file : modFiles)
            {
                modDiscoverer.addCandidate(new ModCandidate(file, file, ContainerType.JAR));
            }

            List<ModContainer> testMods = modDiscoverer.identifyMods();

            LOGGER.info("Found mod-list: " + Arrays.toString(testMods.toArray()));

//            Minecraft.getMinecraft().addScheduledTask(() -> {
//                ClientInspectorPacketRegistry.INSTANCE.sendToServer(new ModListPacket(testMods.stream().map(ModContainer::getModId).collect(Collectors.toList())));
//            });

            return new ModListPacket(testMods.stream().map(ModContainer::getModId).collect(Collectors.toList()));
        }
    }
}
