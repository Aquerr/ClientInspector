package io.github.aquerr.clientinspector.server.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.google.common.io.Resources;
import io.github.aquerr.clientinspector.ClientInspector;

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
    private static Configuration INSTANCE;

    public static Configuration getInstance()
    {
        return INSTANCE;
    }

    private final Path configFilePath;

    private List<String> commandsToRunIfModListNotReceived;
    private List<String> commandsToRun;
    private Set<String> modsToDetect;
    private boolean treatModsToDetectAsWhitelist;
    private String notAllowedModsLogMessageFormat;

    private int modListAwaitTime;

    public static void init(Path configFilePath)
    {
        if (INSTANCE != null)
            return;

        INSTANCE = new Configuration(configFilePath);
    }

    private Configuration(Path configFilePath)
    {
        this.configFilePath = configFilePath;
        load();
    }

    public boolean shouldTreatModsToDetectAsWhitelist()
    {
        return this.treatModsToDetectAsWhitelist;
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

    public String getNotAllowedModsLogMessageFormat()
    {
        return notAllowedModsLogMessageFormat;
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
        this.treatModsToDetectAsWhitelist = getOrDefault(config, "treat_mods_to_detect_as_whitelist", false);
        this.notAllowedModsLogMessageFormat = getOrDefault(config, "not_allowed_mods_log_message_format", "[{0}] Player [name={1}, uuid={2}] connected from [{3}] with not allowed mods [{4}]");

        config.close();
    }

    private <T> T getOrDefault(CommentedFileConfig config, String path, T defaultValue)
    {
        try
        {
            return config.getOrElse(path, defaultValue);
        }
        catch (Exception exception)
        {
            return defaultValue;
        }
    }
}
