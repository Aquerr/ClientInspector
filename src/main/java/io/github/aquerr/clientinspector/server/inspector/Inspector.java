package io.github.aquerr.clientinspector.server.inspector;

import com.google.common.collect.ImmutableList;
import io.github.aquerr.clientinspector.server.config.Configuration;
import io.github.aquerr.clientinspector.server.log.LogHandler;
import io.github.aquerr.clientinspector.server.packet.ClientInspectorPacketRegistry;
import io.github.aquerr.clientinspector.server.packet.ModListPacketResponse;
import io.github.aquerr.clientinspector.server.packet.RequestModListPacket;
import io.github.aquerr.clientinspector.server.packet.ServerPacketAwaiter;
import net.minecraft.commands.Commands;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraftforge.network.ConnectionData;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PacketDistributor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

public final class Inspector
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

    public void inspectWithMods(final ServerPlayer player, final Collection<String> mods)
    {
        inspect(player, mods);
    }

    public void requestAndVerifyModListFromPlayer(ServerPlayer player)
    {
        LOGGER.info("Sending mod-list request to client...");
        ServerPacketAwaiter.getInstance().awaitForPacketFromPlayer(player);
        ClientInspectorPacketRegistry.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new RequestModListPacket());
    }

    public void handleNoModListPacketReceived(ServerPlayer entityPlayerMP)
    {
        entityPlayerMP.getServer().doRunTask(new TickTask(10, () ->
        {
            try
            {
                final String message = "Did not receive response mod list packet from '" + entityPlayerMP.getName().getString() + "'";
                LogHandler.getInstance().logMessage(message);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }));

        executeCommandsOnPlayer(entityPlayerMP, this.configuration.getCommandsToRunIfModListNotReceived());

        // Preform normal scan
        inspect(entityPlayerMP, getPlayerMods(entityPlayerMP));
    }

    private void inspect(final ServerPlayer player, final Collection<String> mods)
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

    private static void executeCommandsOnPlayer(ServerPlayer player, List<String> commandsToRun)
    {
        final MinecraftServer minecraftServer = player.getServer();
        minecraftServer.doRunTask(new TickTask(10, () -> {
            final Commands commandManager = minecraftServer.getCommands();
            for (final String command : commandsToRun)
            {
                //Execute commands as console
                commandManager.performPrefixedCommand(minecraftServer.createCommandSourceStack(), command.replaceAll(PLAYER_PLACEHOLDER, player.getName().getString()));
            }
        }));
    }

    private static Collection<String> getPlayerMods(final ServerPlayer entityPlayerMP)
    {
        ServerGamePacketListenerImpl networkHandlerPlayServer = entityPlayerMP.connection;
        ConnectionData connectionData = NetworkHooks.getConnectionData(networkHandlerPlayServer.connection);
        ImmutableList<String> modList = connectionData.getModList();
        return modList;
    }
}
