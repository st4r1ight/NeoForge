package net.neoforged.neoforge.network.handling;

import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.PacketFlow;

import java.util.OptionalInt;

public interface IPayloadContext {
    OptionalInt version();
    
    IReplyHandler handler();
    
    IPacketHandler packetHandler();
    
    ISynchronizedWorkHandler workHandler();
    
    PacketFlow flow();
    
    ConnectionProtocol protocol();
    
    ChannelHandlerContext channelHandlerContext();
}
