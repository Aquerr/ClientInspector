package io.github.aquerr.clientinspector.server.packet;

import cpw.mods.modlauncher.Launcher;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.FMLServiceProvider;
import net.minecraftforge.fml.loading.moddiscovery.BackgroundScanHandler;
import net.minecraftforge.fml.loading.moddiscovery.ModDiscoverer;
import net.minecraftforge.fml.loading.moddiscovery.ModInfo;
import net.minecraftforge.fml.loading.moddiscovery.ModsFolderLocator;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.forgespi.language.IModFileInfo;
import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.forgespi.locating.IModFile;
import net.minecraftforge.userdev.ClasspathLocator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
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

        Launcher.INSTANCE.

        // Thanks to forge team for making this utility static method :D
//        final List<String> modList = modDiscoverer.discoverMods().getLoadingModList()
//                        .getMods()
//                        .stream()
//                        .map(ModInfo::getModId)
//                        .collect(Collectors.toList());


        ModsFolderLocator modsFolderLocator = new ModsFolderLocator();
        modsFolderLocator.initArguments(Collections.emptyMap());
        ClasspathLocator classpathLocator = new ClasspathLocator();
        classpathLocator.initArguments(Collections.emptyMap());

        LinkedList<IModFile> modFiles = new LinkedList<>();
        modFiles.addAll(modsFolderLocator.scanMods());
        modFiles.addAll(classpathLocator.scanMods());

        BackgroundScanHandler scanHandler = new BackgroundScanHandler(modDiscoverer.discoverMods().getModFiles());

        List<ModInfo> modInfos = scanHandler.getLoadingModList().getMods();

        List<String> modNames = modFiles.stream()
                .map(IModFile::getModFileInfo)
                .map(IModFileInfo::getMods)
                .flatMap(Collection::stream)
                .map(IModInfo::getModId)
                .collect(Collectors.toList());

        LOGGER.info("Found mod-list: " + Arrays.toString(modNames.toArray()));
        ClientInspectorPacketRegistry.INSTANCE.sendToServer(new ModListPacket(modNames));
    }
}
