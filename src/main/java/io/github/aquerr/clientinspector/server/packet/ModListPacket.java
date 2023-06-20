package io.github.aquerr.clientinspector.server.packet;

import io.github.aquerr.clientinspector.server.inspector.Inspector;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

public class ModListPacket implements ClientInspectorPacket
{
    private static final Logger LOGGER = LogManager.getLogger(ModListPacket.class);

    private final List<String> modEntries;

    public ModListPacket()
    {
        this.modEntries = new LinkedList<>();
    }

    public ModListPacket(final List<String> modEntries)
    {
        this.modEntries = modEntries;
    }

    public List<String> getModEntries()
    {
        return modEntries;
    }

    public static FriendlyByteBuf toBytes(ModListPacket modListPacket, FriendlyByteBuf buffer)
    {
        buffer.writeVarInt(modListPacket.getModEntries().size());
        for (String modName: modListPacket.getModEntries())
        {
            buffer.writeUtf(modName);
        }
        return buffer;
    }

    public static ModListPacket fromBytes(FriendlyByteBuf buffer)
    {
        List<String> modEntries = new LinkedList<>();

        int modCount = buffer.readVarInt();
        for (int i = 0; i < modCount; i++)
        {
            modEntries.add(buffer.readUtf());
        }

        return new ModListPacket(modEntries);
    }

    public static void handlePacket(ModListPacket modListPacket, Supplier<NetworkEvent.Context> contextSupplier)
    {
        LOGGER.info("Received mod-list packet from ClientInspector located on the client side.");
        final List<String> modEntries = modListPacket.getModEntries();
        final ServerPlayer entityPlayer = contextSupplier.get().getSender();
        ServerPacketAwaiter.LAST_PACKETS_FROM_PLAYERS.put(entityPlayer.getUUID(), ModListPacket.class);

        entityPlayer.getServer().doRunTask(new TickTask(10, () -> Inspector.getInstance().inspectWithMods(entityPlayer, modEntries)));
    }
}
