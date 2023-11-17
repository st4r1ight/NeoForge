/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.extensions;

import com.google.common.base.Preconditions;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import com.google.common.collect.Sets;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.registries.ForgeRegistry;
import net.neoforged.neoforge.registries.IForgeRegistry;
import net.neoforged.neoforge.registries.RegistryManager;
import org.jetbrains.annotations.NotNull;

/**
 * Extension-Interface providing methods for writing registry-id's instead of their registry-names.
 */
public interface IFriendlyByteBufExtension {
    private FriendlyByteBuf self() {
        return (FriendlyByteBuf) this;
    }

    /**
     * Writes the given entries integer id to the buffer. Notice however that this will only write the id of the given entry and will not check whether it actually exists
     * in the given registry. Therefore no safety checks can be performed whilst reading it and if the entry is not in the registry a default value will be written.
     * 
     * @param registry The registry containing the given entry
     * @param entry    The entry who's registryName is to be written
     * @param <T>      The type of the entry.
     */
    default <T> void writeRegistryIdUnsafe(@NotNull IForgeRegistry<T> registry, @NotNull T entry) {
        ForgeRegistry<T> forgeRegistry = (ForgeRegistry<T>) registry;
        int id = forgeRegistry.getID(entry);
        self().writeVarInt(id);
    }

    /**
     * Writes the given entries integer id to the buffer. Notice however that this will only write the id of the given entry and will not check whether it actually exists
     * in the given registry. Therefore no safety checks can be performed whilst reading it and if the entry is not in the registry a default value will be written.
     * 
     * @param registry The registry containing the entry represented by this key
     * @param entryKey The registry-name of an entry in this {@link IForgeRegistry}
     */
    default void writeRegistryIdUnsafe(@NotNull IForgeRegistry<?> registry, @NotNull ResourceLocation entryKey) {
        ForgeRegistry<?> forgeRegistry = (ForgeRegistry<?>) registry;
        int id = forgeRegistry.getID(entryKey);
        self().writeVarInt(id);
    }

    /**
     * Reads an integer value from the buffer, which will be interpreted as an registry-id in the given registry. Notice that if there is no value in the specified registry for the
     * read id, that the registry's default value will be returned.
     * 
     * @param registry The registry containing the entry
     */
    default <T> T readRegistryIdUnsafe(@NotNull IForgeRegistry<T> registry) {
        ForgeRegistry<T> forgeRegistry = (ForgeRegistry<T>) registry;
        int id = self().readVarInt();
        return forgeRegistry.getValue(id);
    }

    /**
     * Writes a given registry-entry's integer id to the specified buffer in combination with writing the containing registry's id. In contrast to
     * {@link #writeRegistryIdUnsafe(IForgeRegistry, Object)} this method checks every single step performed as well as
     * writing the registry-id to the buffer, in order to prevent any unexpected behaviour. Therefore this method is to be preferred whenever possible,
     * over using the unsafe methods.
     *
     * @param registry The registry containing the entry
     * @param entry    The entry to write
     * @param <T>      The type of the registry-entry
     * @throws NullPointerException     if the registry or entry was null
     * @throws IllegalArgumentException if the registry does not contain the specified value
     */
    default <T> void writeRegistryId(@NotNull IForgeRegistry<T> registry, @NotNull T entry) {
        Objects.requireNonNull(registry, "Cannot write a null registry key!");
        Objects.requireNonNull(entry, "Cannot write a null registry entry!");
        ResourceLocation name = registry.getRegistryName();
        Preconditions.checkArgument(registry.containsValue(entry), "Cannot find %s in %s", registry.getKey(entry) != null ? registry.getKey(entry) : entry, name);
        ForgeRegistry<T> reg = (ForgeRegistry<T>) registry;
        self().writeResourceLocation(name); //TODO change to writing a varInt once registries use id's
        self().writeVarInt(reg.getID(entry));
    }

    /**
     * Reads an registry-entry from the specified buffer. Notice however that the type cannot be checked without providing an additional class parameter
     * - see {@link #readRegistryIdSafe(Class)} for an safe version.
     * 
     * @param <T> The type of the registry-entry. Notice that this should match the actual type written to the buffer.
     * @throws NullPointerException if the registry could not be found.
     */
    default <T> T readRegistryId() {
        ResourceLocation location = self().readResourceLocation(); //TODO change to reading a varInt once registries use id's
        ForgeRegistry<T> registry = RegistryManager.ACTIVE.getRegistry(location);
        return registry.getValue(self().readVarInt());
    }

    /**
     * Reads an registry-entry from the specified buffer. This method also verifies, that the value read is of the appropriate type.
     * 
     * @param <T> The type of the registry-entry.
     * @throws IllegalArgumentException if the retrieved entries registryType doesn't match the one passed in.
     * @throws NullPointerException     if the registry could not be found.
     */
    default <T> T readRegistryIdSafe(Class<? super T> registrySuperType) {
        T value = readRegistryId();
        if (!registrySuperType.isAssignableFrom(value.getClass()))
            throw new IllegalArgumentException("Attempted to read an registryValue of the wrong type from the Buffer!");
        return value;
    }

    /**
     * Writes a FluidStack to the packet buffer, easy enough. If EMPTY, writes a FALSE.
     * This behavior provides parity with the ItemStack method in PacketBuffer.
     *
     * @param stack FluidStack to be written to the packet buffer.
     */
    default void writeFluidStack(FluidStack stack) {
        if (stack.isEmpty()) {
            self().writeBoolean(false);
        } else {
            self().writeBoolean(true);
            stack.writeToPacket(self());
        }
    }

    /**
     * Reads a FluidStack from this buffer.
     */
    default FluidStack readFluidStack() {
        return !self().readBoolean() ? FluidStack.EMPTY : FluidStack.readFromPacket(self());
    }
    
    /**
     * Reads the values from the current buffer using the given reader into a set.
     *
     * @param reader The reader to read the values from
     * @return The set containing the values
     * @param <T> The type of the entry
     */
    default <T> Set<T> readSet(Function<FriendlyByteBuf, T> reader) {
        Set<T> ret = Sets.newHashSet();
        readSet(reader, ret);
        return ret;
    }
    
    /**
     * Reads the values from the current buffer using the given reader and adds them to the given set.
     *
     * @param reader The reader to read the values from
     * @param target The set to add the values to
     * @param <T> The type of the entry
     */
    default <T> void readSet(Function<FriendlyByteBuf, T> reader, Set<T> target) {
        addToCollection(reader, target::add);
    }
    
    /**
     * Reads the values from the current buffer using the given reader and adds them to the given collection.
     *
     * @param reader The reader to read the values from
     * @param adder The consumer to add the values to
     * @param <T> The type of the entry
     */
    default <T> void addToCollection(Function<FriendlyByteBuf, T> reader, Consumer<T> adder) {
        int size = self().readVarInt();
        for (int i = 0; i < size; i++) {
            adder.accept(reader.apply(self()));
        }
    }
    
    /**
     * Writes the entries in the given set to the buffer, by first writing the count and then writing each entry.
     *
     * @param set The set to write
     * @param writer The writer to use for writing each entry
     * @param <T> The type of the entry
     */
    default <T> void writeSet(Set<T> set, BiConsumer<FriendlyByteBuf, T> writer) {
        self().writeVarInt(set.size());
        for (T entry : set) {
            writer.accept(self(), entry);
        }
    }
    
    /**
     * Writes the entries in the given set to the buffer, by first writing the count and then writing each entry.
     *
     * @param set The set to write
     * @param writer The writer to use for writing each entry
     * @param <T> The type of the entry
     */
    default <T> void writeObjectSet(Set<T> set, BiConsumer<T, FriendlyByteBuf> writer) {
        self().writeVarInt(set.size());
        for (T entry : set) {
            writer.accept(entry, self());
        }
    }
    
    /**
     * Reads an optional int from the buffer.
     *
     * @return The optional int
     */
    default OptionalInt readOptionalInt() {
        if (!self().readBoolean())
            return OptionalInt.empty();
        
        return OptionalInt.of(self().readVarInt());
    }
    
    /**
     * Writes an optional int to the buffer.
     *
     * @param optionalInt The optional int
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    default void writeOptionalInt(final OptionalInt optionalInt) {
        self().writeBoolean(optionalInt.isPresent());
        optionalInt.ifPresent(self()::writeVarInt);
    }
}
