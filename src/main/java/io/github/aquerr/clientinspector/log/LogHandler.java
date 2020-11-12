package io.github.aquerr.clientinspector.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.api.entity.living.player.Player;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Set;

public class LogHandler
{
    private static final Logger LOGGER = LoggerFactory.getLogger(LogHandler.class);

    private Path logsDirPath;

    public LogHandler(final Path configDir)
    {
        this.logsDirPath = configDir.resolve("logs");
        try
        {
            Files.createDirectories(this.logsDirPath);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void logPlayerWithMods(final Player player, final Set<String> detectedModsNames) throws IOException
    {
        LOGGER.info("Logging player '" + player.getName() + "' with mods " + Arrays.toString(detectedModsNames.toArray()));

        //We create log files per day.
        final Path logFilePath = this.logsDirPath.resolve("inspection-" + LocalDate.now().toString() + ".log");
        if (Files.notExists(logFilePath))
            Files.createFile(logFilePath);

        final String logMessage = "[" + LocalTime.now().toString() + "] " + "Player '" + player.getName() + "' connected with mods " + Arrays.toString(detectedModsNames.toArray()) + "\n";
        Files.write(logFilePath, logMessage.getBytes(), StandardOpenOption.APPEND);
    }
}
