package net.neoforged.neoforge.network.registration;

import net.minecraft.resources.ResourceLocation;

import java.util.OptionalInt;

public record NetworkChannel(
        ResourceLocation id,
        OptionalInt chosenVersion
) {
}
