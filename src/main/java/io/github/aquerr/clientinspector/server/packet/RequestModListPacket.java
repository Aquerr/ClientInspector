package io.github.aquerr.clientinspector.server.packet;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.loading.moddiscovery.ModFile;
import net.minecraftforge.fml.loading.moddiscovery.ModsFolderLocator;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.forgespi.language.IModFileInfo;
import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.forgespi.locating.IModFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
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
        ModsFolderLocator modsFolderLocator = new ModsFolderLocator();
        modsFolderLocator.initArguments(Collections.emptyMap());

        List<ModFile> modFiles = new LinkedList<>(modsFolderLocator.scanMods()).stream()
                .map(ModFile.class::cast)
                .collect(Collectors.toList());
        modFiles.forEach(ModFile::identifyMods);

        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug("Found mod files: {}", modFiles.stream()
                    .map(IModFile::getFilePath)
                    .collect(Collectors.toList()));
        }

        List<String> modsIds = getModsIds(modFiles);

        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug("Found mod-list: {}", Arrays.toString(modsIds.toArray()));
        }
        ClientInspectorPacketRegistry.INSTANCE.sendToServer(new ModListPacketResponse(modsIds));
    }

    private static List<String> getModsIds(List<ModFile> modFiles)
    {
        List<String> modsIds = new ArrayList<>();
        if (modFiles.isEmpty())
            return modsIds;

        for (final IModFile modFile : modFiles)
        {
            IModFileInfo modFileInfo = modFile.getModFileInfo();
            if (modFileInfo == null)
            {
                LOGGER.warn("Mod File '{}' does not contain mod file info!", modFile.getFileName());
                continue;
            }

            modsIds.addAll(modFileInfo.getMods()
                            .stream()
                            .map(IModInfo::getModId)
                    .collect(Collectors.toSet()));
        }

        return modsIds;
    }
}
