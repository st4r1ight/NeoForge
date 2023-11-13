package net.neoforged.neoforge.network.registration.registrar;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IConfigurationPayloadHandler;
import net.neoforged.neoforge.network.handling.IPlayPayloadHandler;
import net.neoforged.neoforge.network.reading.IPayloadReader;

import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;

public class ModdedPacketRegistrar implements IPayloadRegistrarWithAcceptableRange {
    
    private final String modId;
    private final Map<ResourceLocation, ConfigurationRegistration<?>> configurationPayloads = Maps.newHashMap();
    private final Map<ResourceLocation, PlayRegistration<?>> playPayloads = Maps.newHashMap();
    
    public ModdedPacketRegistrar(String modId) {
        this.modId = modId;
    }
    
    public Map<ResourceLocation, ConfigurationRegistration<?>> getConfigurationRegistrations() {
        return ImmutableMap.copyOf(configurationPayloads);
    }
    
    public Map<ResourceLocation, PlayRegistration<?>> getPlayRegistrations() {
        return ImmutableMap.copyOf(playPayloads);
    }
    
    @Override
    public String getNamespace() {
        return modId;
    }
    
    @Override
    public <T extends CustomPacketPayload> IPayloadRegistrarWithAcceptableRange play(ResourceLocation id, Class<T> type, IPayloadReader<T> reader, IPlayPayloadHandler<T> handler) {
        play(
                id, new PlayRegistration<>(
                        type, reader, handler, OptionalInt.empty(), OptionalInt.empty(), OptionalInt.empty(), Optional.empty(), false
                )
        );
        return this;
    }
    
    @Override
    public <T extends CustomPacketPayload> IPayloadRegistrarWithAcceptableRange configuration(ResourceLocation id, Class<T> type, IPayloadReader<T> reader, IConfigurationPayloadHandler<T> handler) {
        configuration(
                id, new ConfigurationRegistration<>(
                        type, reader, handler, OptionalInt.empty(), OptionalInt.empty(), OptionalInt.empty(), Optional.empty(), false
                )
        );
        return this;
    }
    
    @Override
    public IPayloadRegistrarWithAcceptableRange withVersion(int version) {
        final Configured configured = new Configured();
        return configured.withVersion(version);
    }
    
    @Override
    public IPayloadRegistrarWithAcceptableRange withMinimalVersion(int min) {
        final Configured configured = new Configured();
        return configured.withMinimalVersion(min);
    }
    
    @Override
    public IPayloadRegistrarWithAcceptableRange withMaximalVersion(int max) {
        final Configured configured = new Configured();
        return configured.withMaximalVersion(max);
    }
    
    @Override
    public IPayloadRegistrarWithAcceptableRange optional() {
        final Configured configured = new Configured();
        return configured.optional();
    }
    
    @Override
    public IPayloadRegistrarWithAcceptableRange flowing(PacketFlow flow) {
        final Configured configured = new Configured();
        return configured.flowing(flow);
    }
    
    private void configuration(final ResourceLocation id, ConfigurationRegistration<?> registration) {
        validatePayload(id, registration.type(), configurationPayloads);
        
        configurationPayloads.put(id, registration);
    }
    
    private void play(final ResourceLocation id, PlayRegistration<?> registration) {
        validatePayload(id, registration.type(), playPayloads);
        
        playPayloads.put(id, registration);
    }
    
    private void validatePayload(ResourceLocation id, Class<?> type, final Map<ResourceLocation, ?> payloads) {
        if (payloads.containsKey(id)) {
            throw new RegistrationFailedException(type, id, modId, RegistrationFailedException.Reason.DUPLICATE_ID);
        }
        
        if (!id.getNamespace().equals(modId)) {
            throw new RegistrationFailedException(type, id, modId, RegistrationFailedException.Reason.INVALID_NAMESPACE);
        }
    }
    
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private class Configured implements IPayloadRegistrarWithAcceptableRange {
        
        private OptionalInt version = OptionalInt.empty();
        private OptionalInt minimalVersion = OptionalInt.empty();
        private OptionalInt maximalVersion = OptionalInt.empty();
        private PacketFlow flow = null;
        private boolean optional = false;
        
        @Override
        public String getNamespace() {
            return modId;
        }
        
        @Override
        public <T extends CustomPacketPayload> IPayloadRegistrarWithAcceptableRange play(ResourceLocation id, Class<T> type, IPayloadReader<T> reader, IPlayPayloadHandler<T> handler) {
            ModdedPacketRegistrar.this.play(
                    id, new PlayRegistration<>(
                            type, reader, handler, version, minimalVersion, maximalVersion, Optional.ofNullable(flow), optional
                    )
            );
            return this;
        }
        
        @Override
        public <T extends CustomPacketPayload> IPayloadRegistrarWithAcceptableRange configuration(ResourceLocation id, Class<T> type, IPayloadReader<T> reader, IConfigurationPayloadHandler<T> handler) {
            ModdedPacketRegistrar.this.configuration(
                    id, new ConfigurationRegistration<>(
                            type, reader, handler, version, minimalVersion, maximalVersion, Optional.ofNullable(flow), optional
                    )
            );
            return this;
        }
        
        @Override
        public IPayloadRegistrarWithAcceptableRange withVersion(int version) {
            this.version = OptionalInt.of(version);
            return this;
        }
        
        @Override
        public IPayloadRegistrarWithAcceptableRange withMinimalVersion(int min) {
            this.minimalVersion = OptionalInt.of(min);
            return this;
        }
        
        @Override
        public IPayloadRegistrarWithAcceptableRange withMaximalVersion(int max) {
            this.maximalVersion = OptionalInt.of(max);
            return this;
        }
        
        @Override
        public IPayloadRegistrarWithAcceptableRange optional() {
            this.optional = true;
            return this;
        }
        
        @Override
        public IPayloadRegistrarWithAcceptableRange flowing(PacketFlow flow) {
            this.flow = flow;
            return this;
        }
    }
    
}
