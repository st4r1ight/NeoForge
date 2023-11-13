package net.neoforged.neoforge.network.negotiation;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.util.Range;
import org.apache.commons.compress.utils.Lists;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.*;

/**
 * Negotiates the network components between the server and client.
 */
public class NetworkComponentNegotiator {
    
    /**
     * Negotiates the network components between the server and client.
     * <p>
     * The following rules are followed:
     *     <ul>
     *         <li>Any component that is optional on the client but is not present on the server is removed from the client's list.</li>
     *         <li>Any component that is optional on the server but is not present on the client is removed from the server's list.</li>
     *         <li>If the client has none optional components that are not present on the server, then negotiation fails</li>
     *         <li>If the server has none optional components that are not present on the client, then negotiation fails</li>
     *         <li>For each of the matching channels the following is executed:</li>
     *         <ul>
     *             <li>If one side has no version range provided, but has a preferred version, then it is checked against the other sides range. If the check fails then it fail.</li>
     *             <li>If one side has a range, but the other has no version, then negotiation fails.</li>
     *             <li>If one side has a preferred version, but no range, then the preferred version is interpreted as the sole acceptable range.</li>
     *             <li>Check if the version ranges overlap, if this is not the case, then return.</li>
     *         </ul>
     *         <li>At this point the channels are considered compatible, and a version is to be selected:</li>
     *         <ul>
     *             <li>If the server has a preferred version, and that version is part of the agreed upon range, then that version is selected</li>
     *             <li>If the client has a preferred version, and that version is part of the agreed upon range, then that version is selected</li>
     *             <li>If neither the server, nor the client, has a preferred version, and a agreed upon range exists, the highest version is picked.</li>
     *             <li>If neither the server nor the client has a preferred version, and no agreed upon range exists, then no version is selected.</li>
     *         </ul>
     *     </ul>
     * </p>
     * <p>
     *     If negotiation succeeds then a list of agreed upon channels and their versions is returned.
     * </p>
     * <p>
     *     If negotiation fails then a {@link Component} is returned with the reason for failure.
     * </p>
     *
     * @param server The list of server components that the server wishes to use for communication.
     * @param client The list of client components that the client wishes to use for communication.
     * @return A {@link NegotiationResult} that contains the agreed upon channels and their versions if negotiation succeeded, or a {@link Component} with the reason for failure if negotiation failed.
     */
    public static NegotiationResult negotiate(List<NegotiableNetworkComponent> server, List<NegotiableNetworkComponent> client) {
        //Ensure the inputs are modifiable
        server = new ArrayList<>(server);
        client = new ArrayList<>(client);
        
        List<NegotiableNetworkComponent> finalServer = server;
        final List<NegotiableNetworkComponent> disabledOptionalOnClient = client.stream()
                                                                                  .filter(NegotiableNetworkComponent::optional)
                                                                                  .filter(c -> finalServer.stream().noneMatch(c2 -> c2.id().equals(c.id())))
                                                                                  .toList();
        
        client.removeAll(disabledOptionalOnClient);
        
        List<NegotiableNetworkComponent> finalClient = client;
        final List<NegotiableNetworkComponent> disabledOptionalOnServer = server.stream()
                                                                                  .filter(NegotiableNetworkComponent::optional)
                                                                                  .filter(c -> finalClient.stream().noneMatch(c2 -> c2.id().equals(c.id())))
                                                                                  .toList();
        
        server.removeAll(disabledOptionalOnServer);
        
        Table<ResourceLocation, NegotiableNetworkComponent, NegotiableNetworkComponent> matches = HashBasedTable.create();
        server.forEach(s -> finalClient.forEach(c -> {
            if (s.id().equals(c.id())) {
                matches.put(s.id(), s, c);
            }
        }));
        
        client.removeIf(c -> matches.containsRow(c.id()));
        server.removeIf(c -> matches.containsRow(c.id()));
        
        if (!client.isEmpty()) {
            final Map<ResourceLocation, Component> failureReasons = Maps.newHashMap();
            client.forEach(c -> failureReasons.put(c.id(), Component.translatable("neoforge.network.negotiation.failure.missing.client.server")));
            return new NegotiationResult(List.of(), false, failureReasons);
        }
        
        if (!server.isEmpty()) {
            final Map<ResourceLocation, Component> failureReasons = Maps.newHashMap();
            server.forEach(c -> failureReasons.put(c.id(), Component.translatable("neoforge.network.negotiation.failure.missing.server.client")));
            return new NegotiationResult(List.of(), false, failureReasons);
        }
        
        final List<NegotiatedNetworkComponent> result = Lists.newArrayList();
        final Map<ResourceLocation, Component> failureReasons = Maps.newHashMap();
        for (Table.Cell<ResourceLocation, NegotiableNetworkComponent, NegotiableNetworkComponent> resourceLocationNegotiableNetworkComponentNegotiableNetworkComponentCell : matches.cellSet()) {
            final NegotiableNetworkComponent serverComponent = resourceLocationNegotiableNetworkComponentNegotiableNetworkComponentCell.getColumnKey();
            final NegotiableNetworkComponent clientComponent = resourceLocationNegotiableNetworkComponentNegotiableNetworkComponentCell.getValue();
            
            Optional<ComponentNegotiationResult> serverToClientComparison = validateComponent(serverComponent, clientComponent, "client");
            if (serverToClientComparison.isPresent() && !serverToClientComparison.get().success()) {
                failureReasons.put(serverComponent.id(), serverToClientComparison.get().failureReason());
                continue;
            }
            
            Optional<ComponentNegotiationResult> clientToServerComparison = validateComponent(clientComponent, serverComponent, "server");
            if (clientToServerComparison.isPresent() && !clientToServerComparison.get().success()) {
                failureReasons.put(serverComponent.id(), clientToServerComparison.get().failureReason());
                continue;
            }
            
            Optional<Range<Integer>> serverRange = serverComponent.buildVersionRange();
            Optional<Range<Integer>> clientRange = clientComponent.buildVersionRange();
            OptionalInt version;
            if (serverRange.isEmpty() && clientRange.isEmpty()) {
                //No range available, means also no preferred version available.
                version = OptionalInt.empty();
            } else if (serverRange.isEmpty()) {
                //Should have produced a failure reason above, so this should not happen.
                throw new IllegalStateException("Server range is empty, but client range is not empty. This should not happen.");
            } else if (clientRange.isEmpty()) {
                //Should have produced a failure reason above, so this should not happen.
                throw new IllegalStateException("Client range is empty, but server range is not empty. This should not happen.");
            } else {
                final Range<Integer> overlap = serverRange.get().overlap(clientRange.get());
                if (serverComponent.preferredVersion().isPresent() && overlap.contains(serverComponent.preferredVersion().getAsInt())) {
                    version = serverComponent.preferredVersion();
                } else if (clientComponent.preferredVersion().isPresent() && overlap.contains(clientComponent.preferredVersion().getAsInt())) {
                    version = clientComponent.preferredVersion();
                } else {
                    version = OptionalInt.of(overlap.max());
                }
            }
            
            result.add(new NegotiatedNetworkComponent(serverComponent.id(), version));
        }
        
        if (!failureReasons.isEmpty()) {
            result.clear();
        }
        return new NegotiationResult(result, failureReasons.isEmpty(), failureReasons);
    }
    
    /**
     * Checks if two components are compatible.
     * <p>
     * The following rules are followed:
     *         <ul>
     *             <li>If one side has no version range provided, but has a preferred version, then it is checked against the other sides range.</li>
     *             <li>If one side has a range, but the other has no version, then negotiation fails.</li>
     *             <li>If one side has a preferred version, but no range, then the preferred version is interpreted as the sole acceptable range.</li>
     *             <li>Check if the version ranges overlap, if this is not the case, then return.</li>
     *             <li>If no side has a preferred version, or a range, then the check succeeds.</li>
     *         </ul>
     * </p>
     * <p>
     *     If negotiation succeeds then an empty {@link Optional} is returned.
     * </p>
     * <p>
     *     If negotiation fails then a {@link NegotiationResult} is returned with the reason for failure.
     * </p>
     *
     * @param left           The verification component to compare.
     * @param right          The requesting component to compare.
     * @param requestingSide The side of the requesting component.
     * @return An empty {@link Optional} if negotiation succeeded, or a {@link NegotiationResult} with the reason for failure if negotiation failed.
     */
    @VisibleForTesting
    public static Optional<ComponentNegotiationResult> validateComponent(NegotiableNetworkComponent left, NegotiableNetworkComponent right, String requestingSide) {
        Optional<Range<Integer>> leftRange = left.buildVersionRange();
        Optional<Range<Integer>> rightRange = right.buildVersionRange();
        
        //Perform a left full, right empty check.
        final Optional<Optional<ComponentNegotiationResult>> leftRightCheck = checkRange(left, right, requestingSide, leftRange, rightRange);
        if (leftRightCheck.isPresent()) {
            return leftRightCheck.get();
        }
        
        //Perform a left empty, right full check.
        final Optional<Optional<ComponentNegotiationResult>> rightLeftCheck = checkRange(right, left, requestingSide, rightRange, leftRange);
        if (rightLeftCheck.isPresent()) {
            return rightLeftCheck.get();
        }
        
        //At this point both sides have a range, and both ranges are not empty.
        if (leftRange.isPresent() && rightRange.isPresent()) {
            //Check if the ranges overlap
            //This also performs the check if two channels only have a preferred version, and no range. In that case the ranges have equal min and max values, and they need to line up.
            if (leftRange.get().overlaps(rightRange.get())) {
                //Okay some overlap found so there is a common version that we can support.
                return Optional.empty();
            }
            
            //No overlap found, so negotiation fails.
            return Optional.of(new ComponentNegotiationResult(false, Component.translatable("neoforge.network.negotiation.failure.range.%s.overlap".formatted(requestingSide), left.id(), leftRange.get(), right.id(), rightRange.get())));
        }
        
        //This happens when both the ranges are empty.
        //In other words, no channel has a range, and no channel has a preferred version.
        return Optional.empty();
    }
    
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    @NotNull
    private static Optional<Optional<ComponentNegotiationResult>> checkRange(NegotiableNetworkComponent left, NegotiableNetworkComponent right, String requestingSide, Optional<Range<Integer>> leftRange, Optional<Range<Integer>> rightRange) {
        if (leftRange.isPresent() && rightRange.isEmpty()) {
            if (right.preferredVersion().isPresent()) {
                final Range<Integer> leftActiveRange = leftRange.get();
                if (!leftActiveRange.contains(right.preferredVersion().getAsInt())) {
                    return Optional.of(Optional.of(new ComponentNegotiationResult(false, Component.translatable("neoforge.network.negotiation.failure.preferredVersion.%s.out_of_range".formatted(requestingSide), left.id(), right.preferredVersion().getAsInt(), leftActiveRange))));
                }
                
                return Optional.of(Optional.empty());
            }
            
            return Optional.of(Optional.of(new ComponentNegotiationResult(false, Component.translatable("neoforge.network.negotiation.failure.preferredVersion.%s.required".formatted(requestingSide), left.id(), leftRange.get().toString()))));
        }
        
        return Optional.empty();
    }
    
    public record ComponentNegotiationResult(boolean success, @Nullable Component failureReason) {
    }
}
