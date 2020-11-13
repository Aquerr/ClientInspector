package io.github.aquerr.clientinspector.config;

import io.github.aquerr.clientinspector.ClientInspector;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.asset.Asset;
import org.spongepowered.api.util.TypeTokens;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Config
{
    private final HoconConfigurationLoader hoconConfigurationLoader;
    private ConfigurationNode configNode;

    private List<String> commandsToRun;
    private Set<String> modsToDetect;

    public static Config createConfig(final Path configDir)
    {
        final Path configPath = configDir.resolve("config.conf");
        try
        {
            Files.createDirectories(configDir);
            final Asset asset = Sponge.getAssetManager().getAsset(ClientInspector.getInstance(), "config.conf").orElse(null);
            if (asset != null)
            {
                asset.copyToFile(configPath, false, true);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return new Config(configPath);
    }

    private Config(Path configPath)
    {
        this.hoconConfigurationLoader = HoconConfigurationLoader.builder().setPath(configPath).build();
        try
        {
            this.configNode = this.hoconConfigurationLoader.load();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        load();
    }

    public List<String> getCommandsToRun()
    {
        return commandsToRun;
    }

    public Set<String> getModsToDetect()
    {
        return modsToDetect;
    }

    private void load()
    {
        try
        {
            this.commandsToRun = this.configNode.getNode("commands-to-run").getList(TypeTokens.STRING_TOKEN, Collections.emptyList());
            this.modsToDetect = new HashSet<>(this.configNode.getNode("mods-to-detect").getList(TypeTokens.STRING_TOKEN, Collections.emptyList()));
        }
        catch (ObjectMappingException e)
        {
            e.printStackTrace();
        }
    }
}
