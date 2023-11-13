package net.neoforged.neoforge.network.event;

import com.google.common.collect.ImmutableMap;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.neoforge.network.registration.registrar.ConfigurationRegistration;
import net.neoforged.neoforge.network.registration.registrar.ModdedPacketRegistrar;
import net.neoforged.neoforge.network.registration.registrar.PlayRegistration;

import java.util.*;

public class RegisterPacketHandlerEvent extends Event implements IModBusEvent {
    
    private final Map<String, ModdedPacketRegistrar> registrarsByNamespace = Collections.synchronizedMap(new HashMap<>());
    
    public ModdedPacketRegistrar registrar() {
        final String namespace = ModLoadingContext.get().getActiveNamespace();
        return registrarsByNamespace.computeIfAbsent(namespace, ModdedPacketRegistrar::new);
    }
    
    public Map<ResourceLocation, ConfigurationRegistration<?>> getConfigurationRegistrations() {
        final ImmutableMap.Builder<ResourceLocation, ConfigurationRegistration<?>> builder = ImmutableMap.builder();
        registrarsByNamespace.values().forEach(registrar -> registrar.getConfigurationRegistrations().forEach(builder::put));
        return builder.build();
    }
    
    public Map<ResourceLocation, PlayRegistration<?>> getPlayRegistrations() {
        final ImmutableMap.Builder<ResourceLocation, PlayRegistration<?>> builder = ImmutableMap.builder();
        registrarsByNamespace.values().forEach(registrar -> registrar.getPlayRegistrations().forEach(builder::put));
        return builder.build();
    }
}
