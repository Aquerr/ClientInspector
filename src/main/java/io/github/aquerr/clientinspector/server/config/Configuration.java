package io.github.aquerr.clientinspector.server.config;

import com.google.common.io.ByteSource;
import com.google.common.io.Resources;
import com.typesafe.config.*;
import io.github.aquerr.clientinspector.ClientInspector;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Configuration
{
    private static final Path configFilePath = Paths.get("config/" + ClientInspector.ID + ".cfg");

    private static Configuration INSTANCE;

    private List<String> commandsToRun;
    private Set<String> modsToDetect;
    private Config config;

    public static Configuration getInstance()
    {
        return INSTANCE;
    }

    public static void init()
    {
        if (INSTANCE != null)
            return;

        try
        {
            final String configString = IOUtils.toString(Configuration.class.getClassLoader().getResourceAsStream("assets/clientinspector/config1.conf"), StandardCharsets.UTF_8);
            final Config configResource = ConfigFactory.parseString(configString);
//            configResource.atPath()


//            System.out.println(config.getList("commands-to-run").get(0).render());
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }


//        final com.typesafe.config.Config config = ConfigFactory.empty().withValue("elo", ConfigValueFactory.fromAnyRef("brzuszek"));
//        final String configString = config.root().render(ConfigRenderOptions.defaults().setJson(false));
//        try
//        {
//            Files.write(Paths.get("config/" + ClientInspector.ID + "/" + ClientInspector.ID + ".conf"), configString.getBytes(StandardCharsets.UTF_8));
//        }
//        catch (IOException e)
//        {
//            e.printStackTrace();
//        }
    }

    private Configuration()
    {
//        configuration = new net.minecraftforge.common.config.Configuration(configFilePath.toFile());

        reload();
//        configuration.save();
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
//        this.configuration.load();

//        this.commandsToRun = new LinkedList<>(Arrays.asList(this.configuration.getStringList("commands-to-run", net.minecraftforge.common.config.Configuration.CATEGORY_GENERAL, new String[0], "# Set of commands that should be run against the player after detecing a mod from the above list.\n" +
//                "    # Use %PLAYER% placeholder to target the player. This placeholder will be replaced with the actual player by the plugin.\n" +
//                "    # Note: Don't put the slash \"/\" in front of the command.\n" +
//                "    # Note: Make sure commands are written on separate lines.\n" +
//                "    # E.g. commands-to-run <\n" +
//                "    # ban %PLAYER%\n" +
//                "    # jail %PLAYER% specialjail\n" +
//                "    # >")));
//        this.modsToDetect = new HashSet<>(Arrays.asList(this.configuration.getStringList("mods-to-detect", net.minecraftforge.common.config.Configuration.CATEGORY_GENERAL, new String[0], "# Mods that plugin should search for in the connecting player's mod-list.\n" +
//                "    # Regex is supported and it preforms checks case-insensitive.\n" +
//                "    # E.g. Pattern \".*XRay.*\" will match all mods names with word \"xray\".\n" +
//                "    # E.g. mods-to-detect <\n" +
//                "    # forge\n" +
//                "    # xray\n" +
//                "    # .*xray.*\n" +
//                "    # .*cheats.*\n" +
//                "    # >")));
    }
}
