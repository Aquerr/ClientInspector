package io.github.aquerr.clientinspector.server.packet;

import io.github.aquerr.clientinspector.server.inspector.Inspector;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

public class ServerPacketAwaiter
{
    public static final Map<UUID, Class<?>> LAST_PACKETS_FROM_PLAYERS = new HashMap<>();
    private static final ServerPacketAwaiter INSTANCE = new ServerPacketAwaiter();
    private static final Inspector INSPECTOR = Inspector.getInstance();

    public static ServerPacketAwaiter getInstance()
    {
        return INSTANCE;
    }

    private ServerPacketAwaiter()
    {

    }

    public void awaitForPacketFromPlayer(final EntityPlayerMP player, final Class<? extends IMessage> packetClass, int secondsToWait)
    {
        ForkJoinPool forkJoinPool = ForkJoinPool.commonPool();
        forkJoinPool.submit(new PacketAwaitTask(player, packetClass, secondsToWait));
    }

    public static class PacketAwaitTask extends Thread
    {
        private final EntityPlayerMP entityPlayerMP;
        private final Class<? extends IMessage> packetClass;
        private final int secondsToWait;

        public PacketAwaitTask(final EntityPlayerMP entityPlayerMP, final Class<? extends IMessage> packetClass, int secondsToWait)
        {
            this.entityPlayerMP = entityPlayerMP;
            this.packetClass = packetClass;
            this.secondsToWait = secondsToWait;
            setDaemon(true);
        }

        @Override
        public void run()
        {
            try
            {
                TimeUnit.SECONDS.sleep(this.secondsToWait);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }

            if (LAST_PACKETS_FROM_PLAYERS.containsKey(entityPlayerMP.getUniqueID()) && LAST_PACKETS_FROM_PLAYERS.get(entityPlayerMP.getUniqueID()).equals(packetClass))
            {
                LAST_PACKETS_FROM_PLAYERS.remove(entityPlayerMP.getUniqueID());
                return;
            }
            else
            {
                INSPECTOR.noModListPacketReceived(entityPlayerMP);
            }
        }
    }
}
