package net.neoforged.neoforge.test;

import com.google.common.collect.Lists;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.negotiation.NegotiableNetworkComponent;
import net.neoforged.neoforge.network.negotiation.NegotiatedNetworkComponent;
import net.neoforged.neoforge.network.negotiation.NegotiationResult;
import net.neoforged.neoforge.network.negotiation.NetworkComponentNegotiator;
import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.List;
import java.util.OptionalInt;

@TestMethodOrder(MethodOrderer.MethodName.class)
public class NetworkComponentNegotiatorTest {
    
    private static ResourceLocation ONE = new ResourceLocation("neoforge", "one");
    private static ResourceLocation TWO = new ResourceLocation("neoforge", "two");
    

    @Test
    public void optionalClientMissingOnServerAreNotSent() {
        final List<NegotiableNetworkComponent> client = Lists.newArrayList(
                new NegotiableNetworkComponent(ONE, OptionalInt.empty(), OptionalInt.empty(), OptionalInt.empty(), true)
        );
        final List<NegotiableNetworkComponent> server = Lists.newArrayList();
        
        final NegotiationResult result = NetworkComponentNegotiator.negotiate(server, client);
        
        Assert.assertTrue(result.success());
        Assert.assertNull(result.failureReason());
        Assert.assertSame(0, result.components().size());
    }
    
    @Test
    public void optionalServerMissingOnClientAreNotSent() {
        final List<NegotiableNetworkComponent> server = Lists.newArrayList(
                new NegotiableNetworkComponent(ONE, OptionalInt.empty(), OptionalInt.empty(), OptionalInt.empty(), true)
        );
        final List<NegotiableNetworkComponent> client = Lists.newArrayList();
        
        final NegotiationResult result = NetworkComponentNegotiator.negotiate(server, client);
        
        Assert.assertTrue(result.success());
        Assert.assertNull(result.failureReason());
        Assert.assertSame(0, result.components().size());
    }
    
    @Test
    public void noneOptionalClientMissingOnServerFails() {
        final List<NegotiableNetworkComponent> client = Lists.newArrayList(
                new NegotiableNetworkComponent(ONE, OptionalInt.empty(), OptionalInt.empty(), OptionalInt.empty(), false)
        );
        final List<NegotiableNetworkComponent> server = Lists.newArrayList();
        
        final NegotiationResult result = NetworkComponentNegotiator.negotiate(server, client);
        
        Assert.assertFalse(result.success());
        Assert.assertNotNull(result.failureReason());
        Assert.assertSame(0, result.components().size());
    }
    
    @Test
    public void noneOptionalServerMissingOnClientFails() {
        final List<NegotiableNetworkComponent> server = Lists.newArrayList(
                new NegotiableNetworkComponent(ONE, OptionalInt.empty(), OptionalInt.empty(), OptionalInt.empty(), false)
        );
        final List<NegotiableNetworkComponent> client = Lists.newArrayList();
        
        final NegotiationResult result = NetworkComponentNegotiator.negotiate(server, client);
        
        Assert.assertFalse(result.success());
        Assert.assertNotNull(result.failureReason());
        Assert.assertSame(0, result.components().size());
    }
    
    @Test
    public void serverHasLowerPreferredVersionThenClientsMinFails() {
        final List<NegotiableNetworkComponent> server = Lists.newArrayList(
                new NegotiableNetworkComponent(ONE, OptionalInt.of(0), OptionalInt.empty(), OptionalInt.empty(), false)
        );
        final List<NegotiableNetworkComponent> client = Lists.newArrayList(
                new NegotiableNetworkComponent(ONE, OptionalInt.of(1), OptionalInt.of(1), OptionalInt.empty(), false)
        );
        
        final NegotiationResult result = NetworkComponentNegotiator.negotiate(server, client);
        
        Assert.assertFalse(result.success());
        Assert.assertNotNull(result.failureReason());
        Assert.assertSame(0, result.components().size());
    }
    
    @Test
    public void serverHasHigherPreferredVersionThenClientsMaxFails() {
        final List<NegotiableNetworkComponent> server = Lists.newArrayList(
                new NegotiableNetworkComponent(ONE, OptionalInt.of(2), OptionalInt.empty(), OptionalInt.empty(), false)
        );
        final List<NegotiableNetworkComponent> client = Lists.newArrayList(
                new NegotiableNetworkComponent(ONE, OptionalInt.of(1), OptionalInt.empty(), OptionalInt.of(1), false)
        );
        
        final NegotiationResult result = NetworkComponentNegotiator.negotiate(server, client);
        
        Assert.assertFalse(result.success());
        Assert.assertNotNull(result.failureReason());
        Assert.assertSame(0, result.components().size());
    }
    
    @Test
    public void serverHasNoPreferredVersionButClientHasMinFails() {
        final List<NegotiableNetworkComponent> server = Lists.newArrayList(
                new NegotiableNetworkComponent(ONE, OptionalInt.empty(), OptionalInt.empty(), OptionalInt.empty(), false)
        );
        final List<NegotiableNetworkComponent> client = Lists.newArrayList(
                new NegotiableNetworkComponent(ONE, OptionalInt.of(1), OptionalInt.of(1), OptionalInt.empty(), false)
        );
        
        final NegotiationResult result = NetworkComponentNegotiator.negotiate(server, client);
        
        Assert.assertFalse(result.success());
        Assert.assertNotNull(result.failureReason());
        Assert.assertSame(0, result.components().size());
    }
    
    @Test
    public void serverHasNoPreferredVersionButClientHasMaxFails() {
        final List<NegotiableNetworkComponent> server = Lists.newArrayList(
                new NegotiableNetworkComponent(ONE, OptionalInt.empty(), OptionalInt.empty(), OptionalInt.empty(), false)
        );
        final List<NegotiableNetworkComponent> client = Lists.newArrayList(
                new NegotiableNetworkComponent(ONE, OptionalInt.of(1), OptionalInt.empty(), OptionalInt.of(1), false)
        );
        
        final NegotiationResult result = NetworkComponentNegotiator.negotiate(server, client);
        
        Assert.assertFalse(result.success());
        Assert.assertNotNull(result.failureReason());
        Assert.assertSame(0, result.components().size());
    }
    
    @Test
    public void clientHasLowerPreferredVersionThenServersMinFails() {
        final List<NegotiableNetworkComponent> client = Lists.newArrayList(
                new NegotiableNetworkComponent(ONE, OptionalInt.of(0), OptionalInt.empty(), OptionalInt.empty(), false)
        );
        final List<NegotiableNetworkComponent> server = Lists.newArrayList(
                new NegotiableNetworkComponent(ONE, OptionalInt.of(1), OptionalInt.of(1), OptionalInt.empty(), false)
        );
        
        final NegotiationResult result = NetworkComponentNegotiator.negotiate(client, server);
        
        Assert.assertFalse(result.success());
        Assert.assertNotNull(result.failureReason());
        Assert.assertSame(0, result.components().size());
    }
    
    @Test
    public void clientHasHigherPreferredVersionThenServersMaxFails() {
        final List<NegotiableNetworkComponent> client = Lists.newArrayList(
                new NegotiableNetworkComponent(ONE, OptionalInt.of(2), OptionalInt.empty(), OptionalInt.empty(), false)
        );
        final List<NegotiableNetworkComponent> server = Lists.newArrayList(
                new NegotiableNetworkComponent(ONE, OptionalInt.of(1), OptionalInt.empty(), OptionalInt.of(1), false)
        );
        
        final NegotiationResult result = NetworkComponentNegotiator.negotiate(client, server);
        
        Assert.assertFalse(result.success());
        Assert.assertNotNull(result.failureReason());
        Assert.assertSame(0, result.components().size());
    }
    
    @Test
    public void clientHasNoPreferredVersionButServerHasMinFails() {
        final List<NegotiableNetworkComponent> client = Lists.newArrayList(
                new NegotiableNetworkComponent(ONE, OptionalInt.empty(), OptionalInt.empty(), OptionalInt.empty(), false)
        );
        final List<NegotiableNetworkComponent> server = Lists.newArrayList(
                new NegotiableNetworkComponent(ONE, OptionalInt.of(1), OptionalInt.of(1), OptionalInt.empty(), false)
        );
        
        final NegotiationResult result = NetworkComponentNegotiator.negotiate(client, server);
        
        Assert.assertFalse(result.success());
        Assert.assertNotNull(result.failureReason());
        Assert.assertSame(0, result.components().size());
    }
    
    @Test
    public void clientHasNoPreferredVersionButServerHasMaxFails() {
        final List<NegotiableNetworkComponent> client = Lists.newArrayList(
                new NegotiableNetworkComponent(ONE, OptionalInt.empty(), OptionalInt.empty(), OptionalInt.empty(), false)
        );
        final List<NegotiableNetworkComponent> server = Lists.newArrayList(
                new NegotiableNetworkComponent(ONE, OptionalInt.of(1), OptionalInt.empty(), OptionalInt.of(1), false)
        );
        
        final NegotiationResult result = NetworkComponentNegotiator.negotiate(client, server);
        
        Assert.assertFalse(result.success());
        Assert.assertNotNull(result.failureReason());
        Assert.assertSame(0, result.components().size());
    }
    
    @Test
    public void serverVersionIsPreferredIfCompatible() {
        final List<NegotiableNetworkComponent> client = Lists.newArrayList(
                new NegotiableNetworkComponent(ONE, OptionalInt.of(2), OptionalInt.empty(), OptionalInt.empty(), false)
        );
        final List<NegotiableNetworkComponent> server = Lists.newArrayList(
                new NegotiableNetworkComponent(ONE, OptionalInt.of(1), OptionalInt.empty(), OptionalInt.empty(), false)
        );
        
        final NegotiationResult result = NetworkComponentNegotiator.negotiate(client, server);
        
        Assert.assertTrue(result.success());
        Assert.assertNotNull(result.failureReason());
        Assert.assertSame(1, result.components().size());
        
        final NegotiatedNetworkComponent component = result.components().get(0);
        Assert.assertSame(ONE, component.id());
        Assert.assertSame(OptionalInt.of(1), component.version());
    }
    
    @Test
    public void clientVersionIsPreferredIfServerHasNoPreference() {
        final List<NegotiableNetworkComponent> client = Lists.newArrayList(
                new NegotiableNetworkComponent(ONE, OptionalInt.of(2), OptionalInt.empty(), OptionalInt.empty(), false)
        );
        final List<NegotiableNetworkComponent> server = Lists.newArrayList(
                new NegotiableNetworkComponent(ONE, OptionalInt.empty(), OptionalInt.empty(), OptionalInt.empty(), false)
        );
        
        final NegotiationResult result = NetworkComponentNegotiator.negotiate(client, server);
        
        Assert.assertTrue(result.success());
        Assert.assertNotNull(result.failureReason());
        Assert.assertSame(1, result.components().size());
        
        final NegotiatedNetworkComponent component = result.components().get(0);
        Assert.assertSame(ONE, component.id());
        Assert.assertSame(OptionalInt.of(2), component.version());
    }
    
    @Test
    public void noPreferredVersionIsReturnedIfThereIsNone() {
        final List<NegotiableNetworkComponent> client = Lists.newArrayList(
                new NegotiableNetworkComponent(ONE, OptionalInt.empty(), OptionalInt.empty(), OptionalInt.empty(), false)
        );
        final List<NegotiableNetworkComponent> server = Lists.newArrayList(
                new NegotiableNetworkComponent(ONE, OptionalInt.empty(), OptionalInt.empty(), OptionalInt.empty(), false)
        );
        
        final NegotiationResult result = NetworkComponentNegotiator.negotiate(client, server);
        
        Assert.assertTrue(result.success());
        Assert.assertNotNull(result.failureReason());
        Assert.assertSame(1, result.components().size());
        
        final NegotiatedNetworkComponent component = result.components().get(0);
        Assert.assertSame(ONE, component.id());
        Assert.assertTrue(component.version().isEmpty());
    }
}
