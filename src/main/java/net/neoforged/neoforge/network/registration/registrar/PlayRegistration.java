package net.neoforged.neoforge.network.registration.registrar;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPlayPayloadHandler;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import net.neoforged.neoforge.network.reading.IPayloadReader;
import net.neoforged.neoforge.network.reading.PayloadReadingContext;

import java.util.Optional;
import java.util.OptionalInt;

public record PlayRegistration<T extends CustomPacketPayload>(
        Class<T> type,
        IPayloadReader<T> reader,
        IPlayPayloadHandler<T> handler,
        OptionalInt version,
        OptionalInt minVersion,
        OptionalInt maxVersion,
        
        Optional<PacketFlow> flow,
        boolean optional
) implements IPlayPayloadHandler<CustomPacketPayload>, IPayloadReader<CustomPacketPayload> {
    @Override
    public void handle(PlayPayloadContext context, CustomPacketPayload payload) {
        handler.handle(context, type.cast(payload));
    }
    
    @Override
    public CustomPacketPayload readPayload(FriendlyByteBuf buffer, PayloadReadingContext context) {
        return reader.readPayload(buffer, context);
    }
}
