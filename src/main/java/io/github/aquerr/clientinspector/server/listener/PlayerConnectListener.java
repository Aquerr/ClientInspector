package io.github.aquerr.clientinspector.server.listener;

import io.github.aquerr.clientinspector.server.packet.ClientInspectorPacketRegistry;
import io.github.aquerr.clientinspector.server.packet.RequestModListPacket;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PlayerConnectListener
{
    private static final Logger LOGGER = LogManager.getLogger(PlayerConnectListener.class);

    @SubscribeEvent
    public void onPlayerConnect(final PlayerEvent.PlayerLoggedInEvent event)
    {
        // Request request mod-list packet to the client.
        LOGGER.info("Sending mod-list request to client...");
        ClientInspectorPacketRegistry.INSTANCE.sendTo(new RequestModListPacket(), (EntityPlayerMP) event.player);
    }
}
