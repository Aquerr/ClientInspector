package io.github.aquerr.clientinspector.server.config;

import io.github.aquerr.clientinspector.ClientInspector;
import net.minecraftforge.common.config.Configuration;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Config
{
    private final Configuration configuration;

    private List<String> commandsToRun;
    private Set<String> modsToDetect;

    private static final Path configFilePath = Paths.get("config/" + ClientInspector.ID + ".cfg");

    private static Config INSTANCE;

    public static Config getInstance()
    {
        return INSTANCE;
    }

    public static void init()
    {
        INSTANCE = new Config();
    }

    private Config()
    {
        configuration = new Configuration(configFilePath.toFile());

        reload();
        configuration.save();
    }

    public List<String> getCommandsToRun()
    {
        return commandsToRun;
    }

    public Set<String> getModsToDetect()
    {
        return modsToDetect;
    }

    public void reload()
    {
        this.configuration.load();

        this.commandsToRun = new LinkedList<>(Arrays.asList(this.configuration.getStringList("commands-to-run", Configuration.CATEGORY_GENERAL, new String[0], "# Set of commands that should be run against the player after detecing a mod from the above list.\n" +
                "    # Use %PLAYER% placeholder to target the player. This placeholder will be replaced with the actual player by the plugin.\n" +
                "    # Note: Don't put the slash \"/\" in front of the command.\n" +
                "    # Note: Make sure commands are written on separate lines.\n" +
                "    # E.g. commands-to-run <\n" +
                "    # ban %PLAYER%\n" +
                "    # jail %PLAYER% specialjail\n" +
                "    # >")));
        this.modsToDetect = new HashSet<>(Arrays.asList(this.configuration.getStringList("mods-to-detect", Configuration.CATEGORY_GENERAL, new String[0], "# Mods that plugin should search for in the connecting player's mod-list.\n" +
                "    # Regex is supported and it preforms checks case-insensitive.\n" +
                "    # E.g. Pattern \".*XRay.*\" will match all mods names with word \"xray\".\n" +
                "    # E.g. mods-to-detect <\n" +
                "    # forge\n" +
                "    # xray\n" +
                "    # .*xray.*\n" +
                "    # .*cheats.*\n" +
                "    # >")));
    }
}
