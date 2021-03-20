package io.github.aquerr.clientinspector.server.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigRenderOptions;
import io.github.aquerr.clientinspector.ClientInspector;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Configuration
{
    private static final Path configFilePath = Paths.get("config/" + ClientInspector.ID + ".conf");

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

        INSTANCE = new Configuration();
    }

    private Configuration()
    {
        reload();
        save();
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
        if(Files.notExists(configFilePath))
        {
            try
            {
                final String resource = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("assets/" + ClientInspector.ID + "/config.conf"), StandardCharsets.UTF_8);
                Files.write(configFilePath, resource.getBytes(StandardCharsets.UTF_8));
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }


        final Config configResource = ConfigFactory.parseResourcesAnySyntax("assets/" + ClientInspector.ID + "/config.conf");
        this.config = ConfigFactory.parseFile(configFilePath.toFile()).withFallback(configResource);

        this.commandsToRun = this.config.getStringList("commands-to-run");
        this.modsToDetect = new HashSet<>(this.config.getStringList("mods-to-detect"));
    }

    private void save()
    {
        final String configString = this.config.root().render(ConfigRenderOptions.defaults().setJson(false).setComments(true).setOriginComments(false).setFormatted(true));
        try
        {
            Files.write(configFilePath, configString.getBytes(StandardCharsets.UTF_8));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
