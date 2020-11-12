package io.github.aquerr.clientinspector.listener;

import io.github.aquerr.clientinspector.ClientInspector;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.network.ClientConnectionEvent;

public class PlayerConnectListener
{
    private final ClientInspector plugin;

    public PlayerConnectListener(ClientInspector plugin)
    {
        this.plugin = plugin;
    }

    @Listener(order = Order.LAST)
    public void onPlayerConnect(final ClientConnectionEvent.Join event)
    {
        final Player player = event.getTargetEntity();
        this.plugin.getInspector().inspect(player);
    }
}
