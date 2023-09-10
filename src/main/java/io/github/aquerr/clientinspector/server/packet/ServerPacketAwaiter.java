package io.github.aquerr.clientinspector.server.packet;

import io.github.aquerr.clientinspector.server.config.Configuration;
import io.github.aquerr.clientinspector.server.inspector.Inspector;
import net.minecraft.entity.player.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class ServerPacketAwaiter
{
    // Player UUID => seconds
    private static final List<ModListPacketAwaitTask> AWAIT_PACKET_LIST = new ArrayList<>();
    private static final ServerPacketAwaiter INSTANCE = new ServerPacketAwaiter();
    private static final Inspector INSPECTOR = Inspector.getInstance();

    private final ScheduledExecutorService SCHEDULED_EXECUTOR;

    public static ServerPacketAwaiter getInstance()
    {
        return INSTANCE;
    }

    private final int modListAwaitTime;

    private ServerPacketAwaiter()
    {
        SCHEDULED_EXECUTOR = Executors.newSingleThreadScheduledExecutor();
        SCHEDULED_EXECUTOR.scheduleAtFixedRate(this::packetAwaitTask, 1, 1, TimeUnit.SECONDS);
        this.modListAwaitTime = Configuration.getInstance().getModListAwaitTime();
    }

    private void packetAwaitTask()
    {
        try
        {
            doAwaitPackets();
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
    }

    private void doAwaitPackets()
    {
        synchronized (AWAIT_PACKET_LIST)
        {
            Iterator<ModListPacketAwaitTask> iterator = AWAIT_PACKET_LIST.iterator();
            while (iterator.hasNext())
            {
                ModListPacketAwaitTask modListPacketAwaitTask = iterator.next();
                int seconds = modListPacketAwaitTask.getSeconds();
                if (seconds > this.modListAwaitTime)
                {
                    INSPECTOR.handleNoModListPacketReceived(modListPacketAwaitTask.getServerPlayerEntity());
                    iterator.remove();
                }
                else
                {
                    modListPacketAwaitTask.setSeconds(seconds + 1);
                }
            }
        }
    }

    public void awaitForPacketFromPlayer(final ServerPlayerEntity player)
    {
        synchronized (AWAIT_PACKET_LIST)
        {
            AWAIT_PACKET_LIST.add(new ModListPacketAwaitTask(player));
        }
    }

    public void removeAwaitPacket(final ServerPlayerEntity player)
    {
        synchronized (AWAIT_PACKET_LIST)
        {
            AWAIT_PACKET_LIST.stream()
                    .filter(task -> task.getServerPlayerEntity().getUniqueID().equals(player.getUniqueID()))
                    .findFirst()
                    .ifPresent(AWAIT_PACKET_LIST::remove);
        }
    }

    private static final class ModListPacketAwaitTask
    {
        private final ServerPlayerEntity serverPlayerEntity;
        private int seconds = 0;

        public ModListPacketAwaitTask(ServerPlayerEntity serverPlayerEntity)
        {
            this.serverPlayerEntity = serverPlayerEntity;
        }

        public int getSeconds()
        {
            return seconds;
        }

        public void setSeconds(int seconds)
        {
            this.seconds = seconds;
        }

        public ServerPlayerEntity getServerPlayerEntity()
        {
            return serverPlayerEntity;
        }
    }
}
