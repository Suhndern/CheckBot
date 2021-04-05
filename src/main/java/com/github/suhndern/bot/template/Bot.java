package com.github.suhndern.bot.template;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import javax.annotation.Nonnull;

import javax.security.auth.login.LoginException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

public final class Bot implements Runnable {
    private boolean built;

    @Override
    public void run() {
        if (built) return;

        try {
            // Set security policy and install security manager
            System.setProperty("java.security.policy", "bot.policy");
            System.setSecurityManager(new SecurityManager());
        } catch (SecurityException e) {
            System.err.println(e.toString());
        }

        try {
            // Get the properties specified
            final BotProperties p = BotProperties.getInstance();
            final String botToken = p.getBotToken();
            final Collection<GatewayIntent> intents = p.getGatewayIntentList();
            final int shardId = p.getBotShardIndex();
            final int shardTotal = p.getBotShardTotal();

            // Disable unnecessary cache flags
            final ArrayList<CacheFlag> disabledCacheFlags = new ArrayList<>();
            for (CacheFlag cacheFlag : CacheFlag.values()) {
                if (!intents.contains(cacheFlag.getRequiredIntent())) {
                    disabledCacheFlags.add(cacheFlag);
                }
            }

            // Build and initialize the bot
            JDABuilder.create(botToken, intents)
                    .disableCache(disabledCacheFlags)
                    .addEventListeners(new MessageListener())
                    .useSharding(shardId, shardTotal)
                    .build()
                    .awaitReady();

            built = true;
        } catch (LoginException | InterruptedException | IllegalStateException | IllegalArgumentException e) {
            System.err.println(e.toString());
        }
    }

    private static final class MessageListener extends ListenerAdapter {
        @Override
        public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
            Message msg = event.getMessage();

            // Ignore messages from ourself or messages without a command prefix
            if (msg.getAuthor() == msg.getJDA().getSelfUser() ||
                    !msg.getContentRaw().startsWith(BotProperties.getInstance().getBotPrefix())) return;

            // Get the command
            Optional<Command> factory = CommandFactory
                    .getCommand(msg.getContentRaw().toLowerCase().substring(1));

            // Call the command, if present
            factory.ifPresent(value -> value.apply(event));
        }
    }
}
