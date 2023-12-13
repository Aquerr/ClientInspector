package io.github.aquerr.clientinspector.server.inspector;

import com.google.common.collect.ImmutableList;
import io.github.aquerr.clientinspector.server.config.Configuration;
import io.github.aquerr.clientinspector.server.log.LogHandler;
import io.github.aquerr.clientinspector.server.packet.ClientInspectorPacketRegistry;
import io.github.aquerr.clientinspector.server.packet.RequestModListPacket;
import io.github.aquerr.clientinspector.server.packet.ServerPacketAwaiter;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.ServerPlayNetHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.network.FMLConnectionData;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.fml.network.PacketDistributor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

    public void inspectWithMods(final ServerPlayerEntity player, final Collection<String> mods)
    {
        inspect(player, mods);
    }

    public void requestAndVerifyModListFromPlayer(ServerPlayerEntity player)
    {
        LOGGER.info("Sending mod-list request to client...");
        ServerPacketAwaiter.getInstance().awaitForPacketFromPlayer(player);
        ClientInspectorPacketRegistry.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new RequestModListPacket());
    }

    public void handleNoModListPacketReceived(ServerPlayerEntity entityPlayerMP)
    {
        entityPlayerMP.getServer().deferTask(() ->
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
        });

        executeCommandsOnPlayer(entityPlayerMP, this.configuration.getCommandsToRunIfModListNotReceived());

        // Preform normal scan
        inspect(entityPlayerMP, getPlayerMods(entityPlayerMP));
    }

    private void inspect(final ServerPlayerEntity player, final Collection<String> mods)
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

    private List<String> prepareCommands(List<String> commandsToRun, ServerPlayerEntity player)
    {
        return commandsToRun.stream()
                .map(command -> command.replaceAll(PLAYER_PLACEHOLDER, player.getName().getString()))
                .collect(Collectors.toList());
    }

    private void executeCommandsOnPlayer(ServerPlayerEntity player, List<String> commandsToRun)
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
        minecraftServer.deferTask(() -> {
            final Commands commandManager = minecraftServer.getCommandManager();
            for (final String command : commands)
            {
                //Execute commands as console
                commandManager.handleCommand(minecraftServer.getCommandSource(), command);
            }
        });
    }

    private static Collection<String> getPlayerMods(final ServerPlayerEntity entityPlayerMP)
    {
        ServerPlayNetHandler networkHandlerPlayServer = entityPlayerMP.connection;
        NetworkManager networkManager = networkHandlerPlayServer.netManager;
        FMLConnectionData fmlConnectionData = NetworkHooks.getConnectionData(networkManager);
        ImmutableList<String> modList = fmlConnectionData.getModList();
        return modList;
    }
}
