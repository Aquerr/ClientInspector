package io.github.aquerr.clientinspector.log;

import io.github.aquerr.clientinspector.util.ForgePlayerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.api.entity.living.player.Player;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

        Files.write(logFilePath, buildLogMessage(player, detectedModsNames).getBytes(), StandardOpenOption.APPEND);
    }

    private String buildLogMessage(final Player player, final Set<String> detectedModsNames)
    {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[")
                .append(LocalTime.now().withNano(0).toString())
                .append("]")
                .append(" Player ")
                .append("[name=")
                .append(player.getName())
                .append(", uuid=")
                .append(player.getUniqueId().toString())
                .append("]")
                .append(" connected from '")
                .append(ForgePlayerUtil.getIpAddress(player))
                .append("'")
                .append(" with mods ")
                .append(Arrays.toString(detectedModsNames.toArray()))
                .append("\n");
        return stringBuilder.toString();
    }
}
