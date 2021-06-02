package io.github.aquerr.clientinspector.server.log;

import io.github.aquerr.clientinspector.ClientInspector;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
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
        this.logsDirPath = Paths.get(ClientInspector.ID + "-logs");
        try
        {
            Files.createDirectories(this.logsDirPath);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void logPlayerWithMods(final ServerPlayerEntity player, final Set<String> detectedModsNames) throws IOException
    {
        LOGGER.info("Logging player '" + player.getName() + "' with mods " + Arrays.toString(detectedModsNames.toArray()));
        logMessage(buildLogMessage(player, detectedModsNames));
    }

    private String buildLogMessage(final ServerPlayerEntity player, final Set<String> detectedModsNames)
    {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[")
                .append(LocalTime.now().withNano(0).toString())
                .append("]")
                .append(" Player ")
                .append("[name=")
                .append(player.getName())
                .append(", uuid=")
                .append(player.getUniqueID())
                .append("]")
                .append(" connected from '")
                .append(player.getPlayerIP())
                .append("'")
                .append(" with detected mods ")
                .append(Arrays.toString(detectedModsNames.toArray()))
                .append("\n");
        return stringBuilder.toString();
    }

    public void logPlayerNoModsListResponsePacket(PlayerEntity player) throws IOException
    {
        final String message = "Did not receive response mod list packet from '" + player.getName() + "'";
        LOGGER.info(message);
        logMessage(message);
    }

    private void logMessage(final String message) throws IOException
    {
        final Path logFilePath = this.logsDirPath.resolve("inspection-" + LocalDate.now() + ".log");
        if (Files.notExists(logFilePath))
            Files.createFile(logFilePath);

        Files.write(logFilePath, message.getBytes(), StandardOpenOption.APPEND);
    }
}
