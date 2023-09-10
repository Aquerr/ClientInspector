package io.github.aquerr.clientinspector.server.packet;

import io.github.aquerr.clientinspector.server.inspector.Inspector;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

public class ModListPacketResponse implements ClientInspectorPacket
{
    private static final Logger LOGGER = LogManager.getLogger(ModListPacketResponse.class);

    private final List<String> modEntries;

    public ModListPacketResponse()
    {
        this.modEntries = new LinkedList<>();
    }

    public ModListPacketResponse(final List<String> modEntries)
    {
        this.modEntries = modEntries;
    }

    public List<String> getModEntries()
    {
        return modEntries;
    }

    public static FriendlyByteBuf toBytes(ModListPacketResponse modListPacketResponse, FriendlyByteBuf buffer)
    {
        buffer.writeVarInt(modListPacketResponse.getModEntries().size());
        for (String modName: modListPacketResponse.getModEntries())
        {
            buffer.writeUtf(modName);
        }
        return buffer;
    }

    public static ModListPacketResponse fromBytes(FriendlyByteBuf buffer)
    {
        List<String> modEntries = new LinkedList<>();

        int modCount = buffer.readVarInt();
        for (int i = 0; i < modCount; i++)
        {
            modEntries.add(buffer.readUtf());
        }

        return new ModListPacketResponse(modEntries);
    }

    public static void handlePacket(ModListPacketResponse modListPacketResponse, Supplier<NetworkEvent.Context> contextSupplier)
    {
        final ServerPlayer entityPlayer = contextSupplier.get().getSender();
        final List<String> modEntries = modListPacketResponse.getModEntries();
        LOGGER.info("Received mod-list packet from player: {}, {}.", entityPlayer.getName().getString(), entityPlayer.getIpAddress());
        ServerPacketAwaiter.getInstance().removeAwaitPacket(entityPlayer);

        entityPlayer.getServer().doRunTask(new TickTask(10, () -> Inspector.getInstance().inspectWithMods(entityPlayer, modEntries)));
    }
}
