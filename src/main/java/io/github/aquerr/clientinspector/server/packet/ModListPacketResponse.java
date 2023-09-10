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

    public static PacketBuffer toBytes(ModListPacketResponse modListPacketResponse, PacketBuffer buffer)
    {
        buffer.writeVarInt(modListPacketResponse.getModEntries().size());
        for (String modName: modListPacketResponse.getModEntries())
        {
            buffer.writeString(modName);
        }
        return buffer;
    }

    public static ModListPacketResponse fromBytes(PacketBuffer buffer)
    {
        List<String> modEntries = new LinkedList<>();

        int modCount = buffer.readVarInt();
        for (int i = 0; i < modCount; i++)
        {
            modEntries.add(buffer.readString());
        }

        return new ModListPacketResponse(modEntries);
    }

    public static void handlePacket(ModListPacketResponse modListPacketResponse, Supplier<NetworkEvent.Context> contextSupplier)
    {
        final ServerPlayerEntity entityPlayer = contextSupplier.get().getSender();
        final List<String> modEntries = modListPacketResponse.getModEntries();
        LOGGER.info("Received mod-list packet from player: {}, {}.", entityPlayer.getName().getString(), entityPlayer.getPlayerIP());
        ServerPacketAwaiter.getInstance().removeAwaitPacket(entityPlayer);
        entityPlayer.getServer().deferTask(() -> Inspector.getInstance().inspectWithMods(entityPlayer, modEntries));
    }
}
