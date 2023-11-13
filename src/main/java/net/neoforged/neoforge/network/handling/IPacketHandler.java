package net.neoforged.neoforge.network.handling;

import net.minecraft.network.protocol.Packet;

/**
 * Describes a handler for a packet.
 * Allows for the handling of full packets from custom payloads
 */
@FunctionalInterface
public interface IPacketHandler {
    
    /**
     * Invoked to handle the given packet.
     *
     * @param packet  The packet.
     */
    void handle(Packet<?> packet);
}
