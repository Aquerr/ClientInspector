package io.github.aquerr.clientinspector.server.inspector;

import com.google.common.collect.ImmutableList;
import io.github.aquerr.clientinspector.server.config.Configuration;
import io.github.aquerr.clientinspector.server.log.LogHandler;
import io.github.aquerr.clientinspector.server.packet.ClientInspectorPacketRegistry;
import io.github.aquerr.clientinspector.server.packet.ModListPacket;
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

    public void inspectWithMods(final ServerPlayerEntity player, final Collection<String> mods)
    {
        inspect(player, mods);
    }

    public void requestAndVerifyModListFromPlayer(ServerPlayerEntity player)
    {
        LOGGER.info("Sending mod-list request to client...");
        ServerPacketAwaiter.getInstance().awaitForPacketFromPlayer(player, ModListPacket.class, 10);
        ClientInspectorPacketRegistry.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new RequestModListPacket());
    }

    public void noModListPacketReceived(ServerPlayerEntity entityPlayerMP)
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

    private static void executeCommandsOnPlayer(ServerPlayerEntity player, List<String> commandsToRun)
    {
        final MinecraftServer minecraftServer = player.getServer();
        final Commands commandManager = minecraftServer.getCommandManager();
        for (final String command : commandsToRun)
        {
            //Execute commands as console
            commandManager.handleCommand(minecraftServer.getCommandSource(), command.replaceAll(PLAYER_PLACEHOLDER, player.getName().getString()));
        }
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
