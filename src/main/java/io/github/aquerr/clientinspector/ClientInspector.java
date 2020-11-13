package io.github.aquerr.clientinspector;

import com.google.inject.Inject;
import io.github.aquerr.clientinspector.config.Config;
import io.github.aquerr.clientinspector.inspector.Inspector;
import io.github.aquerr.clientinspector.listener.PlayerConnectListener;
import io.github.aquerr.clientinspector.log.LogHandler;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandManager;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.EventManager;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.plugin.Plugin;

import java.nio.file.Path;

@Plugin(id = ClientInspector.ID, name = ClientInspector.NAME, version = ClientInspector.VERSION, description = ClientInspector.DESCRIPTION, authors = {"Aquerr/Nerdi"}, url = ClientInspector.URL)
public class ClientInspector
{
    public static final String ID = "clientinspector",
            NAME = "Client Inspector",
            DESCRIPTION = "A plugin that inspects data of the clients who connects to the server and preforms predefined tasks",
            VERSION = "1.0.0",
            URL = "https://github.com/Aquerr/ClientInspector";

    private static ClientInspector plugin;

    public static ClientInspector getInstance()
    {
        return plugin;
    }

    @Inject
    @ConfigDir(sharedRoot = false)
    private Path configDir;

    @Inject
    private Logger logger;

    private Config config;
    private Inspector inspector;
    private LogHandler logHandler;

    @Listener
    public void onServerInit(final GameInitializationEvent event)
    {
        plugin = this;
        this.logger.info("Initializing " + NAME);
        this.config = Config.createConfig(this.configDir);
        this.logHandler = new LogHandler(this.configDir);
        this.inspector = new Inspector(this.config, this.logHandler);

        registerCommands();
        registerListeners();

        this.logger.info("Plugin load completed!");
    }

    public Config getConfig()
    {
        return config;
    }

    public Logger getLogger()
    {
        return logger;
    }

    public Path getConfigDir()
    {
        return configDir;
    }

    public Inspector getInspector()
    {
        return inspector;
    }

    public LogHandler getLogHandler()
    {
        return logHandler;
    }

    private void registerCommands()
    {
        final CommandManager commandManager = Sponge.getCommandManager();
    }

    private void registerListeners()
    {
        final EventManager eventManager = Sponge.getEventManager();
        eventManager.registerListeners(this, new PlayerConnectListener(this));
    }
}
