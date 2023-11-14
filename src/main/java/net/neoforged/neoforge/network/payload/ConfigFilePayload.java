package net.neoforged.neoforge.network.payload;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ConfigFilePayload() implements CustomPacketPayload {
    @Override
    public void write(FriendlyByteBuf p_294947_) {
    
    }
    
    @Override
    public ResourceLocation id() {
        return null;
    }
}
