package io.github.aquerr.clientinspector.server.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.loading.moddiscovery.BackgroundScanHandler;
import net.minecraftforge.fml.loading.moddiscovery.ClasspathLocator;
import net.minecraftforge.fml.loading.moddiscovery.ModDiscoverer;
import net.minecraftforge.fml.loading.moddiscovery.ModInfo;
import net.minecraftforge.fml.loading.moddiscovery.ModsFolderLocator;
import net.minecraftforge.forgespi.language.IModFileInfo;
import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.forgespi.locating.IModFile;
import net.minecraftforge.forgespi.locating.IModLocator;
import net.minecraftforge.network.NetworkEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
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
        ModDiscoverer modDiscoverer = new ModDiscoverer(Collections.emptyMap());

//        Launcher.INSTANCE.

//         Thanks to forge team for making this utility static method :D
//        final List<String> modList = modDiscoverer.discoverMods().getLoadingModList()
//                        .getMods()
//                        .stream()
//                        .map(ModInfo::getModId)
//                        .collect(Collectors.toList());


        ModsFolderLocator modsFolderLocator = new ModsFolderLocator();
        modsFolderLocator.initArguments(Collections.emptyMap());
        ClasspathLocator classpathLocator = new ClasspathLocator();
        classpathLocator.initArguments(Collections.emptyMap());

        LinkedList<IModLocator.ModFileOrException> modFiles = new LinkedList<>();
        modFiles.addAll(modsFolderLocator.scanMods());
        modFiles.addAll(classpathLocator.scanMods());

        BackgroundScanHandler scanHandler = new BackgroundScanHandler(modDiscoverer.discoverMods().stage2Validation().getModFiles());

        List<ModInfo> modInfos = scanHandler.getLoadingModList().getMods();
        LOGGER.info(modInfos);

        List<String> modNames = modFiles.stream()
                .filter(modFileOrException -> modFileOrException.ex() == null)
                .map(IModLocator.ModFileOrException::file)
                .map(IModFile::getModFileInfo)
                .map(IModFileInfo::getMods)
                .flatMap(Collection::stream)
                .map(IModInfo::getModId)
                .collect(Collectors.toList());

        LOGGER.info("Found mod-list: " + Arrays.toString(modNames.toArray()));
        ClientInspectorPacketRegistry.INSTANCE.sendToServer(new ModListPacket(modNames));
    }
}
