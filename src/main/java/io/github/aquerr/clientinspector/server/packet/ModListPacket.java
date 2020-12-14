package io.github.aquerr.clientinspector.server.packet;

import io.github.aquerr.clientinspector.server.inspector.Inspector;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedList;
import java.util.List;

public class ModListPacket implements IMessage
{
    private List<String> modEntries;

    public ModListPacket()
    {
        this.modEntries = new LinkedList<>();
    }

    public ModListPacket(final List<String> modEntries)
    {
        this.modEntries = modEntries;
    }

    @Override
    public void toBytes(ByteBuf buffer)
    {
        ByteBufUtils.writeVarInt(buffer, this.modEntries.size(), 2);
        for (String modName: this.modEntries)
        {
            ByteBufUtils.writeUTF8String(buffer, modName);
        }
    }

    @Override
    public void fromBytes(ByteBuf buffer)
    {
        int modCount = ByteBufUtils.readVarInt(buffer, 2);
        for (int i = 0; i < modCount; i++)
        {
            this.modEntries.add(ByteBufUtils.readUTF8String(buffer));
        }
    }

    public static class ModListPacketHandler implements IMessageHandler<ModListPacket, IMessage>
    {
        private static final Logger LOGGER = LogManager.getLogger(ModListPacketHandler.class);

        @Override
        public IMessage onMessage(ModListPacket message, MessageContext ctx)
        {
            LOGGER.info("Received mod-list packet from client.");

            final List<String> modEntries = message.modEntries;
            final EntityPlayerMP entityPlayer = ctx.getServerHandler().player;

            entityPlayer.getServerWorld().addScheduledTask(() -> Inspector.getInstance().inspectWithMods(entityPlayer, modEntries));

            return null;
        }
    }
}
