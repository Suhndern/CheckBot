package com.github.suhndern.bot.template;

import java.util.Map;
import java.util.Optional;

public final class CommandFactory {
    private static final Map<String, Command> commandMap = BotProperties.getInstance().getCommandMap();

    public static Optional<Command> getCommand(String commandName) {
        return Optional.ofNullable(commandMap.get(commandName));
    }
}