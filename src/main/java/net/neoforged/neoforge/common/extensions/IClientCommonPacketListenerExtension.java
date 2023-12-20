/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.extensions;

import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientCommonPacketListener;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.thread.ReentrantBlockableEventLoop;

/**
 * This interface is used to extend the {@link ClientCommonPacketListener} interface.
 * <p>
 * Its primary purpose is to expose sending logic, for transmitting packets to the server.
 * </p>
 */
public interface IClientCommonPacketListenerExtension {

    /**
     * Sends a packet to the server.
     * 
     * @param packet The packet to send.
     */
    void send(Packet<?> packet);

    /**
     * Sends a custom payload to the server.
     *
     * @param payload The payload to send.
     */
    default void send(CustomPacketPayload payload) {
        send(new ServerboundCustomPayloadPacket(payload));
    }

    /**
     * Exposes the raw underlying connection.
     *
     * @return The raw underlying connection.
     */
    Connection getConnection();

    /**
     * Exposes the raw underlying connection event loop that can be used to schedule tasks on the main thread.
     *
     * @return The raw underlying connection event loop.
     */
    ReentrantBlockableEventLoop<?> getMainThreadEventLoop();

    /**
     * {@return true if the connection is to a vanilla client}
     */
    boolean isVanillaConnection();

    /**
     * {@return true if the custom payload type with the given id is usable by this connection}
     *
     * @param payloadId The payload id to check
     */
    boolean isConnected(final ResourceLocation payloadId);

    /**
     * {@return true if the custom payload is usable by this connection}
     * 
     * @param payload The payload to check
     */
    default boolean isConnected(final CustomPacketPayload payload) {
        return isConnected(payload.id());
    }
}
