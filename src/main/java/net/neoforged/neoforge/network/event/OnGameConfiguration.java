package net.neoforged.neoforge.network.event;

import net.minecraft.server.network.ConfigurationTask;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.neoforge.network.configuration.ICustomConfigurationTask;

import java.util.LinkedList;
import java.util.Queue;

public class OnGameConfiguration extends Event implements IModBusEvent {

    private final Queue<ICustomConfigurationTask> configurationTasks = new LinkedList<>();
    
    public void register(ICustomConfigurationTask task) {
        configurationTasks.add(task);
    }
    
    public Queue<ICustomConfigurationTask> getConfigurationTasks() {
        return new LinkedList<>(configurationTasks);
    }
}
