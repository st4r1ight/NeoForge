/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network;

import net.minecraft.network.protocol.PacketFlow;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;
import net.neoforged.neoforge.network.event.RegisterPacketHandlerEvent;
import net.neoforged.neoforge.network.handlers.ClientForgeRegistryHandler;
import net.neoforged.neoforge.network.handlers.ServerForgeRegistryHandler;
import net.neoforged.neoforge.network.payload.FrozenRegistryPayload;
import net.neoforged.neoforge.network.payload.FrozenRegistrySyncCompletePayload;
import net.neoforged.neoforge.network.payload.FrozenRegistrySyncStartPayload;
import net.neoforged.neoforge.network.registration.registrar.IPayloadRegistrar;
import org.jetbrains.annotations.ApiStatus;

@Mod.EventBusSubscriber(modid = NeoForgeVersion.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
@ApiStatus.Internal
public class NetworkInitialization {
    
    @SubscribeEvent
    public static void register(final RegisterPacketHandlerEvent event) {
        final IPayloadRegistrar registrar = event.registrar(NeoForgeVersion.MOD_ID)
                                                    .versioned(buildNetworkVersion());
        registrar
                .configuration(
                        FrozenRegistrySyncStartPayload.ID,
                        FrozenRegistrySyncStartPayload::new,
                        handlers -> handlers.client(ClientForgeRegistryHandler.getInstance()::handle)
                )
                .configuration(
                        FrozenRegistryPayload.ID,
                        FrozenRegistryPayload::new,
                        handlers -> handlers.client(ClientForgeRegistryHandler.getInstance()::handle)
                )
                .configuration(
                        FrozenRegistrySyncCompletePayload.ID,
                        FrozenRegistrySyncCompletePayload::new,
                        handlers -> handlers.client(ClientForgeRegistryHandler.getInstance()::handle)
                                            .server(ServerForgeRegistryHandler.getInstance()::handle)
                );
    }
    
    /**
     * Build the network version from the current forge version.
     *
     * @return The network version.
     */
    private static int buildNetworkVersion() {
        String activeVersion = NeoForgeVersion.getVersion();
        if (activeVersion.contains("-")) {
            activeVersion = activeVersion.substring(0, activeVersion.indexOf("-"));
        }
        String[] versionParts = activeVersion.split("\\.");
        int[] versionInts = new int[versionParts.length];
        for (int i = 0; i < versionParts.length; i++) {
            versionInts[i] = Integer.parseInt(versionParts[i]);
        }
        
        final int major = versionInts[0];
        final int minor = versionInts[1];
        final int patch = versionInts[2];
        
        return major << 16 | minor << 8 | patch;
    }
    
/*    public static SimpleChannel getHandshakeChannel() {
        SimpleChannel handshakeChannel = NetworkRegistry.ChannelBuilder.named(NetworkConstants.FML_HANDSHAKE_RESOURCE).clientAcceptedVersions(a -> true).serverAcceptedVersions(a -> true).networkProtocolVersion(() -> NetworkConstants.NETVERSION).simpleChannel();

        handshakeChannel.simpleLoginMessageBuilder(HandshakeMessages.S2CConfigData.class, 4, LoginNetworkDirection.LOGIN_TO_CLIENT).decoder(HandshakeMessages.S2CConfigData::decode).buildLoginPacketList(ConfigSync.INSTANCE::syncConfigs).consumerNetworkThread(HandshakeHandler.consumerFor(HandshakeHandler::handleConfigSync)).add();

        return handshakeChannel;
    }

    public static SimpleChannel getPlayChannel() {
        SimpleChannel playChannel = NetworkRegistry.ChannelBuilder.named(NetworkConstants.FML_PLAY_RESOURCE).clientAcceptedVersions(a -> true).serverAcceptedVersions(a -> true).networkProtocolVersion(() -> NetworkConstants.NETVERSION).simpleChannel();

        playChannel.messageBuilder(PlayMessages.SpawnEntity.class, 0).decoder(PlayMessages.SpawnEntity::decode).encoder(PlayMessages.SpawnEntity::encode).consumerMainThread(PlayMessages.SpawnEntity::handle).add();

        playChannel.messageBuilder(PlayMessages.OpenContainer.class, 1).decoder(PlayMessages.OpenContainer::decode).encoder(PlayMessages.OpenContainer::encode).consumerMainThread(PlayMessages.OpenContainer::handle).add();

        return playChannel;
    }*/
}
