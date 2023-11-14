package net.neoforged.neoforge.network.registration.registrar;

public interface INetworkPayloadVersioningBuilder {
    
    INetworkPayloadVersioningBuilder withVersion(int version);
    
    INetworkPayloadVersioningBuilder withMinimalVersion(int min);
    
    INetworkPayloadVersioningBuilder withMaximalVersion(int max);
    
    INetworkPayloadVersioningBuilder optional();
    
    default INetworkPayloadVersioningBuilder withAcceptableRange(int min, int max) {
        return withMinimalVersion(min).withMaximalVersion(max);
    }
}
