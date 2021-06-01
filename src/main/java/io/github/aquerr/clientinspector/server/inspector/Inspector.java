package io.github.aquerr.clientinspector.server.inspector;

import io.github.aquerr.clientinspector.server.config.Configuration;
import io.github.aquerr.clientinspector.server.log.LogHandler;
import io.github.aquerr.clientinspector.server.packet.ClientInspectorPacketRegistry;
import io.github.aquerr.clientinspector.server.packet.ModListPacket;
import io.github.aquerr.clientinspector.server.packet.RequestModListPacket;
import io.github.aquerr.clientinspector.server.packet.ServerPacketAwaiter;
import net.minecraft.command.ICommandManager;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.NetworkManager;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.network.handshake.NetworkDispatcher;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

public class Inspector
{
    private static final Logger LOGGER = LogManager.getLogger(Inspector.class);
    private static final String PLAYER_PLACEHOLDER = "%PLAYER%";

    private static final Inspector INSTANCE = new Inspector();

    public static Inspector getInstance()
    {
        return INSTANCE;
    }

    private final Configuration configuration;
    private final LogHandler logHandler;

    private Inspector()
    {
        this.configuration = Configuration.getInstance();
        this.logHandler = LogHandler.getInstance();
    }

    public void inspectWithMods(final EntityPlayerMP player, final Collection<String> mods)
    {
        inspect(player, mods);
    }

    public void requestAndVerifyModListFromPlayer(EntityPlayerMP player)
    {
        LOGGER.info("Sending mod-list request to client...");
        ServerPacketAwaiter.getInstance().awaitForPacketFromPlayer(player, ModListPacket.class, 10);
        ClientInspectorPacketRegistry.INSTANCE.sendTo(new RequestModListPacket(), player);
    }

    public void noModListPacketReceived(EntityPlayerMP entityPlayerMP)
    {
        entityPlayerMP.getServerWorld().addScheduledTask(() ->
        {
            try
            {
                LogHandler.getInstance().logPlayerNoModsListResponsePacket(entityPlayerMP);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        });

        executeCommandsOnPlayer(entityPlayerMP, this.configuration.getCommandsToRunIfModListNotReceived());

        // Preform normal scan
        inspect(entityPlayerMP, getPlayerMods(entityPlayerMP));
    }

    private void inspect(final EntityPlayerMP player, final Collection<String> mods)
    {
        LOGGER.info("Inspecting player " + player);
        LOGGER.info("Inspecting mods: " + Arrays.toString(mods.toArray()));

        final Set<String> modsNamesToDetect = this.configuration.getModsToDetect();
        final Set<String> detectedModsNames = new HashSet<>();
        for (final String playerModName : mods)
        {
            for (final String modNameToDetect : modsNamesToDetect)
            {
                final Pattern pattern = Pattern.compile(modNameToDetect, Pattern.CASE_INSENSITIVE);
                if (pattern.matcher(playerModName).matches())
                {
                    detectedModsNames.add(playerModName);
                }
            }
        }

        if (detectedModsNames.size() != 0)
        {
            // IO operation -> run in separate thread.
            CompletableFuture.runAsync(() ->
            {
                try
                {
                    this.logHandler.logPlayerWithMods(player, detectedModsNames);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            });
            executeCommandsOnPlayer(player, this.configuration.getCommandsToRun());
        }
    }

    private static void executeCommandsOnPlayer(EntityPlayerMP player, List<String> commandsToRun)
    {
        final MinecraftServer minecraftServer = player.getServer();
        final ICommandManager commandManager = minecraftServer.getCommandManager();
        for (final String command : commandsToRun)
        {
            //Execute commands as console
            commandManager.executeCommand(minecraftServer, command.replaceAll(PLAYER_PLACEHOLDER, player.getName()));
        }
    }

    private static Set<String> getPlayerMods(final EntityPlayerMP entityPlayerMP)
    {
        NetHandlerPlayServer networkHandlerPlayServer = entityPlayerMP.connection;
        NetworkManager networkManager = networkHandlerPlayServer.netManager;
        NetworkDispatcher networkDispatcher = NetworkDispatcher.get(networkManager);
        Map<String, String> modList = networkDispatcher.getModList();
        return modList.keySet();
    }
}
