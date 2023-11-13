/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network;

import java.util.Arrays;
import java.util.List;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;
import net.neoforged.neoforge.network.event.EventNetworkChannel;
import net.neoforged.neoforge.network.event.RegisterPacketHandlerEvent;
import net.neoforged.neoforge.network.payload.FrozenRegistryPayload;
import net.neoforged.neoforge.network.simple.SimpleChannel;
import net.neoforged.neoforge.registries.RegistryManager;
import org.jetbrains.annotations.ApiStatus;

@Mod.EventBusSubscriber(modid = NeoForgeVersion.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
@ApiStatus.Internal
public class NetworkInitialization {

    @SubscribeEvent
    public static void register(final RegisterPacketHandlerEvent event) {
        event.registrar()
                .withVersion(buildNetworkVersion())
                .configuration(
                        FrozenRegistryPayload.ID,
                        FrozenRegistryPayload.class,
                        FrozenRegistryPayload::new,
                        
                )
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

        handshakeChannel.simpleLoginMessageBuilder(HandshakeMessages.C2SAcknowledge.class, 99, LoginNetworkDirection.LOGIN_TO_SERVER).decoder(HandshakeMessages.C2SAcknowledge::decode).consumerNetworkThread(HandshakeHandler.indexFirst(HandshakeHandler::handleClientAck)).add();

        handshakeChannel.simpleLoginMessageBuilder(HandshakeMessages.S2CModData.class, 5, LoginNetworkDirection.LOGIN_TO_CLIENT).decoder(HandshakeMessages.S2CModData::decode).markAsLoginPacket().noResponse().consumerNetworkThread(HandshakeHandler.consumerFor(HandshakeHandler::handleModData)).add();

        handshakeChannel.simpleLoginMessageBuilder(HandshakeMessages.S2CModList.class, 1, LoginNetworkDirection.LOGIN_TO_CLIENT).decoder(HandshakeMessages.S2CModList::decode).markAsLoginPacket().consumerNetworkThread(HandshakeHandler.consumerFor(HandshakeHandler::handleServerModListOnClient)).add();

        handshakeChannel.simpleLoginMessageBuilder(HandshakeMessages.C2SModListReply.class, 2, LoginNetworkDirection.LOGIN_TO_SERVER).decoder(HandshakeMessages.C2SModListReply::decode).consumerNetworkThread(HandshakeHandler.indexFirst(HandshakeHandler::handleClientModListOnServer)).add();

        handshakeChannel.simpleLoginMessageBuilder(HandshakeMessages.S2CRegistry.class, 3, LoginNetworkDirection.LOGIN_TO_CLIENT).decoder(HandshakeMessages.S2CRegistry::decode).buildLoginPacketList(RegistryManager::generateRegistryPackets). //TODO: Make this non-static, and store a cache on the client.
                consumerNetworkThread(HandshakeHandler.consumerFor(HandshakeHandler::handleRegistryMessage)).add();

        handshakeChannel.simpleLoginMessageBuilder(HandshakeMessages.S2CConfigData.class, 4, LoginNetworkDirection.LOGIN_TO_CLIENT).decoder(HandshakeMessages.S2CConfigData::decode).buildLoginPacketList(ConfigSync.INSTANCE::syncConfigs).consumerNetworkThread(HandshakeHandler.consumerFor(HandshakeHandler::handleConfigSync)).add();

        handshakeChannel.simpleLoginMessageBuilder(HandshakeMessages.S2CChannelMismatchData.class, 6, LoginNetworkDirection.LOGIN_TO_CLIENT).decoder(HandshakeMessages.S2CChannelMismatchData::decode).consumerNetworkThread(HandshakeHandler.consumerFor(HandshakeHandler::handleModMismatchData)).add();

        return handshakeChannel;
    }

    public static SimpleChannel getPlayChannel() {
        SimpleChannel playChannel = NetworkRegistry.ChannelBuilder.named(NetworkConstants.FML_PLAY_RESOURCE).clientAcceptedVersions(a -> true).serverAcceptedVersions(a -> true).networkProtocolVersion(() -> NetworkConstants.NETVERSION).simpleChannel();

        playChannel.messageBuilder(PlayMessages.SpawnEntity.class, 0).decoder(PlayMessages.SpawnEntity::decode).encoder(PlayMessages.SpawnEntity::encode).consumerMainThread(PlayMessages.SpawnEntity::handle).add();

        playChannel.messageBuilder(PlayMessages.OpenContainer.class, 1).decoder(PlayMessages.OpenContainer::decode).encoder(PlayMessages.OpenContainer::encode).consumerMainThread(PlayMessages.OpenContainer::handle).add();

        return playChannel;
    }*/
}
