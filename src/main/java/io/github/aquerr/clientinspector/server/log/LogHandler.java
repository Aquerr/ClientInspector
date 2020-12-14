package io.github.aquerr.clientinspector.server.log;

import io.github.aquerr.clientinspector.ClientInspector;
import net.minecraft.entity.player.EntityPlayerMP;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Set;

public class LogHandler
{
    private static final Logger LOGGER = LogManager.getLogger(LogHandler.class);
    private static final LogHandler INSTANCE = new LogHandler();

    public static LogHandler getInstance()
    {
        return INSTANCE;
    }

    private final Path logsDirPath;

    private LogHandler()
    {
        this.logsDirPath = Paths.get("config", ClientInspector.ID, "logs");
        try
        {
            Files.createDirectories(this.logsDirPath);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void logPlayerWithMods(final EntityPlayerMP player, final Set<String> detectedModsNames) throws IOException
    {
        LOGGER.info("Logging player '" + player.getName() + "' with mods " + Arrays.toString(detectedModsNames.toArray()));

        //We create log files per day.
        final Path logFilePath = this.logsDirPath.resolve("inspection-" + LocalDate.now().toString() + ".log");
        if (Files.notExists(logFilePath))
            Files.createFile(logFilePath);

        Files.write(logFilePath, buildLogMessage(player, detectedModsNames).getBytes(), StandardOpenOption.APPEND);
    }

    private String buildLogMessage(final EntityPlayerMP player, final Set<String> detectedModsNames)
    {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[")
                .append(LocalTime.now().withNano(0).toString())
                .append("]")
                .append(" Player ")
                .append("[name=")
                .append(player.getName())
                .append(", uuid=")
                .append(player.getUniqueID().toString())
                .append("]")
                .append(" connected from '")
                .append(player.getPlayerIP())
                .append("'")
                .append(" with mods ")
                .append(Arrays.toString(detectedModsNames.toArray()))
                .append("\n");
        return stringBuilder.toString();
    }
}
