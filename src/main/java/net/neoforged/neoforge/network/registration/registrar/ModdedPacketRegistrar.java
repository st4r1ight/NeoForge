package net.neoforged.neoforge.network.registration.registrar;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IConfigurationPayloadHandler;
import net.neoforged.neoforge.network.handling.IPlayPayloadHandler;
import net.neoforged.neoforge.network.reading.IPayloadReader;

import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Consumer;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class ModdedPacketRegistrar implements IPayloadRegistrar, INetworkPayloadVersioningBuilder {
    
    private final String modId;
    private final Map<ResourceLocation, ConfigurationRegistration<?>> configurationPayloads;
    private final Map<ResourceLocation, PlayRegistration<?>> playPayloads;
    private OptionalInt version = OptionalInt.empty();
    private OptionalInt minimalVersion = OptionalInt.empty();
    private OptionalInt maximalVersion = OptionalInt.empty();
    private boolean optional = false;
    
    
    public ModdedPacketRegistrar(String modId) {
        this.modId = modId;
        playPayloads = Maps.newHashMap();
        configurationPayloads = Maps.newHashMap();
    }
    
    private ModdedPacketRegistrar(ModdedPacketRegistrar source) {
        this.modId = source.modId;
        this.playPayloads = source.playPayloads;
        this.configurationPayloads = source.configurationPayloads;
        this.version = source.version;
        this.minimalVersion = source.minimalVersion;
        this.maximalVersion = source.maximalVersion;
        this.optional = source.optional;
    }
    
    public Map<ResourceLocation, ConfigurationRegistration<?>> getConfigurationRegistrations() {
        return ImmutableMap.copyOf(configurationPayloads);
    }
    
    public Map<ResourceLocation, PlayRegistration<?>> getPlayRegistrations() {
        return ImmutableMap.copyOf(playPayloads);
    }
    
    
    @Override
    public <T extends CustomPacketPayload> IPayloadRegistrar play(ResourceLocation id, IPayloadReader<T> reader, IPlayPayloadHandler<T> handler) {
        play(
                id, new PlayRegistration<>(
                        reader, handler, version, minimalVersion, maximalVersion, Optional.empty(), optional
                )
        );
        return this;
    }
    
    @Override
    public <T extends CustomPacketPayload> IPayloadRegistrar configuration(ResourceLocation id, IPayloadReader<T> reader, IConfigurationPayloadHandler<T> handler) {
        configuration(
                id, new ConfigurationRegistration<>(
                        reader, handler, version, minimalVersion, maximalVersion, Optional.empty(), optional
                )
        );
        return this;
    }
    
    @Override
    public IPayloadRegistrar versioned(Consumer<INetworkPayloadVersioningBuilder> configurer) {
        final ModdedPacketRegistrar copy = new ModdedPacketRegistrar(this);
        configurer.accept(copy);
        return copy;
    }
    
    @Override
    public <T extends CustomPacketPayload> IPayloadRegistrar play(ResourceLocation id, IPayloadReader<T> reader, Consumer<PlayPayloadHandler.Builder<T>> handler) {
        final PlayPayloadHandler.Builder<T> builder = new PlayPayloadHandler.Builder<>();
        handler.accept(builder);
        final PlayPayloadHandler<T> innerHandler = builder.create();
        play(
                id, new PlayRegistration<>(
                        reader, innerHandler, version, minimalVersion, maximalVersion, innerHandler.flow(), optional
                )
        );
        return this;
    }
    
    @Override
    public <T extends CustomPacketPayload> IPayloadRegistrar configuration(ResourceLocation id, IPayloadReader<T> reader, Consumer<ConfigurationPayloadHandler.Builder<T>> handler) {
        final ConfigurationPayloadHandler.Builder<T> builder = new ConfigurationPayloadHandler.Builder<>();
        handler.accept(builder);
        final ConfigurationPayloadHandler<T> innerHandler = builder.create();
        configuration(
                id, new ConfigurationRegistration<>(
                        reader, innerHandler, version, minimalVersion, maximalVersion, innerHandler.flow(), optional
                )
        );
        return this;
    }
    
    private void configuration(final ResourceLocation id, ConfigurationRegistration<?> registration) {
        validatePayload(id, configurationPayloads);
        
        configurationPayloads.put(id, registration);
    }
    
    private void play(final ResourceLocation id, PlayRegistration<?> registration) {
        validatePayload(id, playPayloads);
        
        playPayloads.put(id, registration);
    }
    
    private void validatePayload(ResourceLocation id, final Map<ResourceLocation, ?> payloads) {
        if (payloads.containsKey(id)) {
            throw new RegistrationFailedException(id, modId, RegistrationFailedException.Reason.DUPLICATE_ID);
        }
        
        if (!id.getNamespace().equals(modId)) {
            throw new RegistrationFailedException(id, modId, RegistrationFailedException.Reason.INVALID_NAMESPACE);
        }
    }
    
    @Override
    public INetworkPayloadVersioningBuilder withVersion(int version) {
        this.version = OptionalInt.of(version);
        return this;
    }
    
    @Override
    public INetworkPayloadVersioningBuilder withMinimalVersion(int min) {
        this.minimalVersion = OptionalInt.of(min);
        return this;
    }
    
    @Override
    public INetworkPayloadVersioningBuilder withMaximalVersion(int max) {
        this.maximalVersion = OptionalInt.of(max);
        return this;
    }
    
    @Override
    public INetworkPayloadVersioningBuilder optional() {
        this.optional = true;
        return this;
    }
}
