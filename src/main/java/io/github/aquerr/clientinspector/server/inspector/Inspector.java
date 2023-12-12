package io.github.aquerr.clientinspector.server.inspector;

import com.google.common.collect.ImmutableList;
import io.github.aquerr.clientinspector.server.config.Configuration;
import io.github.aquerr.clientinspector.server.log.LogHandler;
import io.github.aquerr.clientinspector.server.packet.ClientInspectorPacketRegistry;
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

import static java.lang.String.format;

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

        final Set<String> notAllowedMods = findNotAllowedMods(mods);

        if (!notAllowedMods.isEmpty())
        {
            // IO operation -> run in separate thread.
            CompletableFuture.runAsync(() ->
            {
                try
                {
                    this.logHandler.logPlayerWithNotAllowedMods(player, notAllowedMods);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            });
            executeCommandsOnPlayer(player, this.configuration.getCommandsToRun());
        }
    }

    private Set<String> findNotAllowedMods(final Collection<String> mods)
    {
        final Set<String> modNameDetectionPatterns = this.configuration.getModsToDetect();
        final boolean shouldTreatModsToDetectAsWhitelist = this.configuration.shouldTreatModsToDetectAsWhitelist();
        return findNotAllowedMods(mods, modNameDetectionPatterns, shouldTreatModsToDetectAsWhitelist);
    }

    private Set<String> findNotAllowedMods(Collection<String> mods, Set<String> modNameDetectionPatterns, boolean isWhiteList)
    {
        final Set<String> detectedModsNames = new HashSet<>();
        boolean notAllowedMod = isWhiteList;
        for (String mod : mods)
        {
            for (final String modNamePattern : modNameDetectionPatterns)
            {
                final Pattern pattern = Pattern.compile(modNamePattern, Pattern.CASE_INSENSITIVE);
                if (pattern.matcher(mod).matches())
                {
                    notAllowedMod = !isWhiteList;
                    break;
                }
            }

            if (notAllowedMod)
            {
                detectedModsNames.add(mod);
            }
        }

        return detectedModsNames;
    }

    private List<String> prepareCommands(List<String> commandsToRun, ServerPlayer player)
    {
        return commandsToRun.stream()
                .map(command -> command.replaceAll(PLAYER_PLACEHOLDER, player.getName().getString()))
                .toList();
    }

    private void executeCommandsOnPlayer(ServerPlayer player, List<String> commandsToRun)
    {
        List<String> commands = prepareCommands(configuration.getCommandsToRun(), player);

        try
        {
            logHandler.logMessage(format("Executing commands %s on player '%s'", Arrays.toString(commandsToRun.toArray()), player.getName().getString()));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        final MinecraftServer minecraftServer = player.getServer();
        minecraftServer.doRunTask(new TickTask(10, () -> {
            final Commands commandManager = minecraftServer.getCommands();
            for (final String command : commands)
            {
                //Execute commands as console
                commandManager.performPrefixedCommand(minecraftServer.createCommandSourceStack(), command);
            }
        }));
    }

    private Collection<String> getPlayerMods(final ServerPlayer entityPlayerMP)
    {
        ServerGamePacketListenerImpl networkHandlerPlayServer = entityPlayerMP.connection;
        ConnectionData connectionData = NetworkHooks.getConnectionData(networkHandlerPlayServer.connection);
        ImmutableList<String> modList = connectionData.getModList();
        return modList;
    }
}
