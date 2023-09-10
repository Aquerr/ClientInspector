package io.github.aquerr.clientinspector.server.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.loading.moddiscovery.BackgroundScanHandler;
import net.minecraftforge.fml.loading.moddiscovery.ModDiscoverer;
import net.minecraftforge.fml.loading.moddiscovery.ModFile;
import net.minecraftforge.fml.loading.moddiscovery.ModFileInfo;
import net.minecraftforge.fml.loading.moddiscovery.ModsFolderLocator;
import net.minecraftforge.forgespi.language.IModFileInfo;
import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.forgespi.locating.IModFile;
import net.minecraftforge.forgespi.locating.IModLocator;
import net.minecraftforge.network.NetworkEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class RequestModListPacket implements ClientInspectorPacket
{
    private static final Logger LOGGER = LogManager.getLogger(RequestModListPacket.class);

    public static RequestModListPacket fromBytes(FriendlyByteBuf buf)
    {
        return new RequestModListPacket();
    }

    public static FriendlyByteBuf toBytes(RequestModListPacket requestModListPacket, FriendlyByteBuf buffer)
    {
        return buffer;
    }

    public static void handlePacket(RequestModListPacket modListPacket, Supplier<NetworkEvent.Context> contextSupplier)
    {
        LOGGER.info("Sending mod-list packet to the server...");
        ModDiscoverer modDiscoverer = new ModDiscoverer(prepareArguments());

        ModsFolderLocator modsFolderLocator = new ModsFolderLocator();
        modsFolderLocator.initArguments(Collections.emptyMap());

        LinkedList<IModFileInfo> modFiles = new LinkedList<>();
        modFiles.addAll(modsFolderLocator.scanMods().stream().map(IModLocator.ModFileOrException::file)
                .map(IModFile::getModFileInfo)
                .toList());

        BackgroundScanHandler scanHandler = modDiscoverer.discoverMods().stage2Validation();
        modFiles.addAll(scanHandler.getLoadingModList().getModFiles().stream()
                .map(ModFileInfo::getFile)
                .map(ModFile::getModFileInfo)
                .toList()
        );

        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug("Found mod files: {}", modFiles.stream()
                    .map(IModFileInfo::getFile)
                    .map(IModFile::getFilePath)
                    .toList());
        }

        Set<String> modIds = modFiles.stream()
                .map(IModFileInfo::getMods)
                .flatMap(Collection::stream)
                .map(IModInfo::getModId)
                .collect(Collectors.toSet());

        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug("Found mod-list: {}", Arrays.toString(modIds.toArray()));
        }
        ClientInspectorPacketRegistry.INSTANCE.sendToServer(new ModListPacketResponse(new ArrayList<>(modIds)));
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
