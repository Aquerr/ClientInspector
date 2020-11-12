package io.github.aquerr.clientinspector.inspector;

import io.github.aquerr.clientinspector.config.Config;
import io.github.aquerr.clientinspector.log.LogHandler;
import io.github.aquerr.clientinspector.util.ForgePlayerUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Inspector
{
    private static final Logger LOGGER = LoggerFactory.getLogger(Inspector.class);
    private static final String PLAYER_PLACEHOLDER = "%PLAYER%";

    private final Config config;
    private final LogHandler logHandler;

    public Inspector(final Config config, final LogHandler logHandler)
    {
        this.config = config;
        this.logHandler = logHandler;
    }

    public void inspect(final Player player)
    {
        LOGGER.info("Inspecting player " + player);
        final Set<String> playerModsNames = ForgePlayerUtil.getPlayerMods(player);
        final Set<String> modsNamesToDetect = this.config.getModsToDetect();
        final Set<String> detectedModsNames = new HashSet<>();
        for (final String playerModName : playerModsNames)
        {
            for (final String modNameToDetect : modsNamesToDetect)
            {
                if (StringUtils.equalsIgnoreCase(playerModName, modNameToDetect))
                {
                    detectedModsNames.add(playerModName);
                }
            }
        }

        if (detectedModsNames.size() != 0)
        {
            try
            {
                this.logHandler.logPlayerWithMods(player, detectedModsNames);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            executeCommandsOnPlayer(player, this.config.getCommandsToRun());
        }
    }

    private void executeCommandsOnPlayer(Player player, List<String> commandsToRun)
    {
        for (final String command : commandsToRun)
        {
            //Execute commands as console
            Sponge.getCommandManager().process(Sponge.getServer().getConsole(), command.replaceAll(PLAYER_PLACEHOLDER, player.getName()));
        }
    }
}
