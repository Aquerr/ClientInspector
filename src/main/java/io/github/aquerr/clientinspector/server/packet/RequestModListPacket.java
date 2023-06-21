package io.github.aquerr.clientinspector.server.packet;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.loading.moddiscovery.BackgroundScanHandler;
import net.minecraftforge.fml.loading.moddiscovery.ModDiscoverer;
import net.minecraftforge.fml.loading.moddiscovery.ModFileInfo;
import net.minecraftforge.fml.loading.moddiscovery.ModsFolderLocator;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.forgespi.language.IModFileInfo;
import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.forgespi.locating.IModFile;
import net.minecraftforge.userdev.ClasspathLocator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
        ModDiscoverer modDiscoverer = new ModDiscoverer(prepareArguments());

        ModsFolderLocator modsFolderLocator = new ModsFolderLocator();
        modsFolderLocator.initArguments(Collections.emptyMap());
        ClasspathLocator classpathLocator = new ClasspathLocator();
        classpathLocator.initArguments(Collections.emptyMap());

        LinkedList<IModFile> modFiles = new LinkedList<>();
        modFiles.addAll(modsFolderLocator.scanMods());
        modFiles.addAll(classpathLocator.scanMods());

        BackgroundScanHandler scanHandler = modDiscoverer.discoverMods();
        modFiles.addAll(scanHandler.getLoadingModList().getModFiles().stream()
                .map(ModFileInfo::getFile)
                .collect(Collectors.toList()));

        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug("Found mod files: {}", modFiles.stream()
                    .map(IModFile::getFilePath)
                    .collect(Collectors.toList()));
        }

        List<String> modIds = modFiles.stream()
                .map(IModFile::getModFileInfo)
                .map(IModFileInfo::getMods)
                .flatMap(Collection::stream)
                .map(IModInfo::getModId)
                .collect(Collectors.toList());

        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug("Found mod-list: {}", Arrays.toString(modIds.toArray()));
        }
    }

    private static Map<String, ?> prepareArguments()
    {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("mods", Collections.emptyList());
        arguments.put("modLists", Collections.emptyList());
        arguments.put("mavenRoots", Collections.emptyList());
        return arguments;
    }
}
