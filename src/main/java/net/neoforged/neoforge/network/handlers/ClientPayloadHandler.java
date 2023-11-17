package net.neoforged.neoforge.network.handlers;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.ConfigSync;
import net.neoforged.neoforge.network.handling.ConfigurationPayloadContext;
import net.neoforged.neoforge.network.payload.ConfigFilePayload;
import net.neoforged.neoforge.network.payload.FrozenRegistryPayload;
import net.neoforged.neoforge.network.payload.FrozenRegistrySyncCompletePayload;
import net.neoforged.neoforge.network.payload.FrozenRegistrySyncStartPayload;
import net.neoforged.neoforge.registries.ForgeRegistry;
import net.neoforged.neoforge.registries.GameData;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ClientPayloadHandler {

    private static final ClientPayloadHandler INSTANCE = new ClientPayloadHandler();
    
    public static ClientPayloadHandler getInstance() {
        return INSTANCE;
    }
    
    private final Set<ResourceLocation> toSynchronize = Sets.newHashSet();
    private final Map<ResourceLocation, ForgeRegistry.Snapshot> synchronizedRegistries = Maps.newHashMap();
    
    private ClientPayloadHandler() {}
    
    
    public void handle(ConfigurationPayloadContext context, FrozenRegistryPayload payload) {
        synchronizedRegistries.put(payload.registryName(), payload.snapshot());
        toSynchronize.remove(payload.registryName());
    }
    
    public void handle(ConfigurationPayloadContext context, FrozenRegistrySyncStartPayload payload) {
        this.toSynchronize.addAll(payload.toAccess());
        this.synchronizedRegistries.clear();
    }
    
    public void handle(ConfigurationPayloadContext context, FrozenRegistrySyncCompletePayload payload) {
        if (!this.toSynchronize.isEmpty()) {
            context.packetHandler().disconnect(Component.translatable("neoforge.registries.sync.failed", this.toSynchronize.stream().map(Object::toString).collect(Collectors.joining(", "))));
            return;
        }
        
        //This method normally returns missing entries, but we just accept what the server send us and ignore the rest.
        GameData.injectSnapshot(synchronizedRegistries, false, false);
        
        this.toSynchronize.clear();
        this.synchronizedRegistries.clear();
        
        context.handler().send(new FrozenRegistrySyncCompletePayload());
    }
    
    public void handle(ConfigurationPayloadContext context, ConfigFilePayload payload) {
        ConfigSync.INSTANCE.receiveSyncedConfig(payload.contents(), payload.fileName());
    }
}
