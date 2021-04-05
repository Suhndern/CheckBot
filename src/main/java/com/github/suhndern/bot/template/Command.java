package com.github.suhndern.bot.template;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.annotation.Nonnull;

public abstract class Command {

    public abstract void apply(MessageReceivedEvent event);

    @Nonnull
    public abstract String getCommandDescription();

    @Nonnull
    public abstract String getCommandAuthor();

    @Nonnull
    public String getCommandUsage() {
        return "";
    }
}
