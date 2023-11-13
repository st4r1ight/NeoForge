package net.neoforged.neoforge.network.negotiation;

import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.util.Range;

import java.util.Optional;
import java.util.OptionalInt;

public record NegotiableNetworkComponent(
        ResourceLocation id,
        OptionalInt preferredVersion,
        OptionalInt minVersion,
        OptionalInt maxVersion,
        Optional<PacketFlow> flow,
        boolean optional
) {
    
    /**
     * Builds the version range for this component.
     * <p>
     *     If no version range and no preferred version is specified, then an empty optional is returned.
     *     If no version range is specified, but a preferred version is, then a range with the preferred version as min and max is returned.
     *     If a min version is specified, but no max version is, then a range with the min version as min and:
     *     <ul>
     *         <li>If a preferred version is specified, then the preferred version is used as max</li>
     *         <li>If no preferred version is specified, then the min version is used as max</li>
     *     </ul>
     *     is returned.
     *     If a max version is specified, but no min version is, then a range with the max version as max and:
     *     <ul>
     *         <li>If a preferred version is specified, then the preferred version is used as min</li>
     *         <li>If no preferred version is specified, then the max version is used as min</li>
     *     </ul>
     *     is returned.
     *     If both a min and max version is specified, then a range with the min version as min and the max version as max is returned.
     * </p>
     * @return The potential version range for this component.
     */
    public Optional<Range<Integer>> buildVersionRange() {
        if (minVersion().isEmpty() && maxVersion().isEmpty() && preferredVersion().isEmpty()) return Optional.empty();
        
        if (minVersion().isEmpty() && maxVersion().isEmpty()) {
            return Optional.of(new Range<>(Integer.class, preferredVersion().getAsInt(), preferredVersion().getAsInt()));
        }
        
        if (minVersion().isEmpty()) {
            if (preferredVersion().isPresent()) {
                return Optional.of(new Range<>(Integer.class, preferredVersion().getAsInt(), maxVersion().getAsInt()));
            }
            
            return Optional.of(new Range<>(Integer.class, maxVersion().getAsInt(), maxVersion().getAsInt()));
        }
        
        if (maxVersion().isEmpty()) {
            if (preferredVersion().isPresent()) {
                return Optional.of(new Range<>(Integer.class, minVersion().getAsInt(), preferredVersion().getAsInt()));
            }
            
            return Optional.of(new Range<>(Integer.class, minVersion().getAsInt(), minVersion().getAsInt()));
        }
        
        return Optional.of(new Range<>(Integer.class, minVersion().getAsInt(), maxVersion().getAsInt()));
    }
}
