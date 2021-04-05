package com.github.suhndern.bot.template;

import lombok.Getter;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

@Getter
public final class BotProperties {
    private BotToken botToken;
    private String botPrefix;
    private int botShardIndex, botShardTotal;
    private List<GatewayIntent> gatewayIntentList;
    private Map<String, Command> commandMap;

    private static BotProperties botProperties = null;

    private BotProperties() {
        try {
            Properties p = new Properties();
            p.load(ClassLoader.getSystemClassLoader().getResourceAsStream("bot.properties"));

            // Get bot token and assert it is not null
            botToken = new BotToken(p.getProperty("bot.token"));
            if (botToken.getBotToken() == null) throw new AssertionError("No bot token found in properties.");

            // Get optional properties
            botPrefix = (String) p.getOrDefault("bot.prefix", "!");
            botShardIndex = Integer.parseInt(String.valueOf(p.getOrDefault("bot.shard.index", 0)));
            botShardTotal = Integer.parseInt(String.valueOf(p.getOrDefault("bot.shard.total", 1)));
            if (botShardIndex >= botShardTotal) throw new AssertionError("Bot shard index not less than shard total.");

            // Parse for gateway intents
            gatewayIntentList = new ArrayList<>(GatewayIntent.values().length);
            for (Object keyObject : p.keySet()) {
                final String keyString = (String) keyObject;
                final String botIntentKeyPrefix = "bot.intents.";

                // Convert property file syntax to Java enum syntax
                if (keyString.startsWith(botIntentKeyPrefix) && Boolean.parseBoolean(p.getProperty(keyString))) {
                    gatewayIntentList.add(GatewayIntent.valueOf(keyString.substring(botIntentKeyPrefix.length()).replace('-', '_').toUpperCase()));
                }
            }

            // Parse for canonical names of commands and load the class instances into a map
            String[] commandClassNameList = p.getProperty("bot.commands").split(",");
            commandMap = new HashMap<>(new CommandLoader(commandClassNameList).getCommandMap());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static BotProperties getInstance() {
        if (botProperties == null) {
            botProperties = new BotProperties();
        }

        return botProperties;
    }

    public String getBotToken() {
        StackTraceElement traceElement = Thread.currentThread().getStackTrace()[2];

        // Explicitly deny all but the run method in the Bot class access to login token
        String className = traceElement.getClassName();
        String methodName = traceElement.getMethodName();
        if (!className.equals(Bot.class.getName()) && methodName.equals("run")) {
            Logger.getGlobal().warning(String.format("Method \"%s\" in class \"%s\" just requested access to the bot token!! Please review the code for this class immediately!",
                    methodName, className));
            return null;
        }

        // Return the bot token to the run method
        return botToken.getBotToken();
    }

    private static final class BotToken extends SecurityManager {
        @Getter
        private final String botToken;

        @Override
        public void checkPackageAccess(String pkg) {
            super.checkPackageAccess(pkg);

            // Disable reflection -- sensitive information is stored here!
            if (pkg.equals("java.lang.reflect")) {
                throw new SecurityException("Reflection is disabled for this class!");
            }
        }

        public BotToken(String botToken) {
            this.botToken = botToken;
        }
    }


}
