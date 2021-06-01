package io.github.aquerr.clientinspector.server.listener;

import io.github.aquerr.clientinspector.server.inspector.Inspector;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

public class PlayerConnectListener
{
    @SubscribeEvent
    public void onPlayerConnect(final PlayerEvent.PlayerLoggedInEvent event)
    {
        // Request mod-list packet from the client.
        Inspector.getInstance().requestAndVerifyModListFromPlayer((EntityPlayerMP)event.player);
    }
}
