package io.github.aquerr.clientinspector.server.packet;

import io.github.aquerr.clientinspector.server.inspector.Inspector;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

    public static PacketBuffer toBytes(ModListPacket modListPacket, PacketBuffer buffer)
    {
        buffer.writeVarInt(modListPacket.getModEntries().size());
        for (String modName: modListPacket.getModEntries())
        {
            buffer.writeString(modName);
        }
        return buffer;
    }

    public static ModListPacket fromBytes(PacketBuffer buffer)
    {
        List<String> modEntries = new LinkedList<>();

        int modCount = buffer.readVarInt();
        for (int i = 0; i < modCount; i++)
        {
            modEntries.add(buffer.readString());
        }

        return new ModListPacket(modEntries);
    }

    public static void handlePacket(ModListPacket modListPacket, Supplier<NetworkEvent.Context> contextSupplier)
    {
        LOGGER.info("Received mod-list packet from ClientInspector located on the client side.");
        final List<String> modEntries = modListPacket.getModEntries();
        final ServerPlayerEntity entityPlayer = contextSupplier.get().getSender();
        ServerPacketAwaiter.LAST_PACKETS_FROM_PLAYERS.put(entityPlayer.getUniqueID(), ModListPacket.class);

        entityPlayer.getServer().deferTask(() -> Inspector.getInstance().inspectWithMods(entityPlayer, modEntries));
    }
}
