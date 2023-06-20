package io.github.aquerr.clientinspector.server.listener;

import io.github.aquerr.clientinspector.server.inspector.Inspector;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class PlayerConnectListener
{
    @SubscribeEvent
    public void onPlayerConnect(final PlayerEvent.PlayerLoggedInEvent event)
    {
        // Request mod-list packet from the client.
        Inspector.getInstance().requestAndVerifyModListFromPlayer((ServerPlayer) event.getEntity());
    }
}
