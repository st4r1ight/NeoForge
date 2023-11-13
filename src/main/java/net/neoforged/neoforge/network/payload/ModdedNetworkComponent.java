package net.neoforged.neoforge.network.payload;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.OptionalInt;

public record ModdedNetworkComponent(ResourceLocation id, OptionalInt version) {
    
    
    public ModdedNetworkComponent(FriendlyByteBuf buf) {
        this(buf.readResourceLocation(), buf.readOptionalInt());
    }
    
    public void write(FriendlyByteBuf buf) {
        buf.writeResourceLocation(id);
        buf.writeOptionalInt(version);
    }
}
