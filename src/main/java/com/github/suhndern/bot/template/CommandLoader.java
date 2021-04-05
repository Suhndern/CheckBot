package com.github.suhndern.bot.template;

import lombok.Getter;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public final class CommandLoader {
    @Getter
    private final Map<String, Command> commandMap;

    public CommandLoader(String[] commandClassNames) {
        // Initialize map variables
        commandMap = new HashMap<>(commandClassNames.length);
        Map<String, CommandNameCounter> nameCounterMap = new HashMap<>(commandClassNames.length);

        // Run through list of command class names
        for (String s : commandClassNames) {
            try {
                // Load and create new instance of specified Command subclass
                Command commandInstance = (Command) Class.forName(s).getConstructor().newInstance();

                // Disambiguate duplicate command class name
                String commandName = commandInstance.getClass().getSimpleName().toLowerCase();
                CommandNameCounter nameCounter = nameCounterMap.getOrDefault(commandName, new CommandNameCounter());
                int commandNameUseCount = nameCounter.getAndIncrementCommandNameUseCount();
                if (commandNameUseCount != 0) commandName += String.format("~%d", commandNameUseCount);
                nameCounterMap.put(commandName, nameCounter);

                // Add command class to command map
                commandMap.put(commandName, commandInstance);
            } catch (ClassNotFoundException | ClassCastException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                System.err.println(e.toString());
            }
        }
    }

    private static final class CommandNameCounter {
        private final AtomicInteger commandNameUseCount = new AtomicInteger(0);

        public int getCommandNameUseCount() {
            return commandNameUseCount.get();
        }

        public int getAndIncrementCommandNameUseCount() {
            return commandNameUseCount.getAndIncrement();
        }
    }
}
