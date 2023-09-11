package io.github.aquerr.clientinspector.server.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.google.common.io.Resources;
import io.github.aquerr.clientinspector.ClientInspector;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.lang.String.format;

public final class Configuration
{
    private static final Path configFilePath = FMLPaths.CONFIGDIR.get().resolve(ClientInspector.ID).resolve("config.toml");

    private static Configuration INSTANCE;

    public static Configuration getInstance()
    {
        return INSTANCE;
    }

    private List<String> commandsToRunIfModListNotReceived;
    private List<String> commandsToRun;
    private Set<String> modsToDetect;

    private int modListAwaitTime;

    public static void init()
    {
        if (INSTANCE != null)
            return;

        INSTANCE = new Configuration();
    }

    private Configuration()
    {
        load();
    }

    public List<String> getCommandsToRunIfModListNotReceived()
    {
        return commandsToRunIfModListNotReceived;
    }

    public List<String> getCommandsToRun()
    {
        return commandsToRun;
    }

    public Set<String> getModsToDetect()
    {
        return modsToDetect;
    }

    public int getModListAwaitTime()
    {
        return modListAwaitTime;
    }

    private void load()
    {
        if (Files.notExists(configFilePath))
        {
            try
            {
                String configTemplateLocation = format("assets/%s/config.toml", ClientInspector.ID);
                Files.createDirectories(configFilePath.getParent());
                Files.createFile(configFilePath);
                Resources.copy(Resources.getResource(configTemplateLocation).toURI().toURL(), Files.newOutputStream(configFilePath));
            }
            catch (IOException | URISyntaxException e)
            {
                throw new IllegalStateException(e);
            }
        }

        CommentedFileConfig config = CommentedFileConfig.of(configFilePath);
        config.load();

        this.commandsToRun = getOrDefault(config, "commands_to_run", Collections.emptyList());
        this.modsToDetect = new HashSet<>(getOrDefault(config, "mods_to_detect", Collections.emptyList()));
        this.commandsToRunIfModListNotReceived = getOrDefault(config, "commands_to_run_when_modlist_not_received", Collections.emptyList());
        this.modListAwaitTime = getOrDefault(config, "mod_list_await_time", 10);

        config.close();
    }

    private <T> T getOrDefault(CommentedFileConfig config, String path, T defaultValue)
    {
        try
        {
            return config.get(path);
        }
        catch (Exception exception)
        {
            return defaultValue;
        }
    }
}
