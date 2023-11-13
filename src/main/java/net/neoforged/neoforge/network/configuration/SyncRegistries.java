package net.neoforged.neoforge.network.configuration;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.network.ConfigurationTask;
import net.neoforged.neoforge.network.payload.FrozenRegistrySyncStartPayload;

import java.util.function.Consumer;

public record SyncRegistries() implements ICustomConfigurationTask {

    public static final Type TYPE = new Type("neoforge:sync_registries");
    
    @Override
    public void run(Consumer<CustomPacketPayload> sender) {
        sender.accept(new FrozenRegistrySyncStartPayload());
    }
    
    @Override
    public Type type() {
        return TYPE;
    }
}
