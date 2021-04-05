package com.github.suhndern.bot.check;

import com.github.suhndern.bot.template.Command;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.requests.restaction.MessageAction;

import javax.annotation.Nonnull;

public class Check extends Command {
    private final String EMOJI_WHITE_CHECK_MARK = "✅";
    private final String EMOJI_WARNING = "⚠";
    private final String EMOJI_NO_ENTRY = "⛔";
    private final String MESSAGE_TITLE = "Rate this conversation!";
    private final String MESSAGE_TEXT = EMOJI_WHITE_CHECK_MARK + " — I think this conversation is fine.\n" +
            EMOJI_WARNING + " — I think this conversation is getting a little tense.\n" +
            EMOJI_NO_ENTRY + " — I think this conversation needs to stop.";


    @Override
    public void apply(MessageReceivedEvent event) {
        final MessageChannel channel = event.getChannel();
        MessageAction action;

        try {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setTitle(MESSAGE_TITLE);
            builder.setColor(0x87CEEB);
            //builder.setAuthor(event.getAuthor().getName(), null, event.getAuthor().getAvatarUrl());
            builder.setDescription(MESSAGE_TEXT);
            action = channel.sendMessage(builder.build());
        } catch (InsufficientPermissionException e) {
            MessageBuilder builder = new MessageBuilder();
            builder.setContent("**" + MESSAGE_TITLE + "**\n" + MESSAGE_TEXT);
            action = channel.sendMessage(builder.build());
        }

        action.queue(message -> {
            message.addReaction(EMOJI_WHITE_CHECK_MARK).queue();
            message.addReaction(EMOJI_WARNING).queue();
            message.addReaction(EMOJI_NO_ENTRY).queue();
        });

        try {
            event.getMessage().delete().queue();
        } catch (InsufficientPermissionException ignored) {

        }
    }

    @Nonnull
    @Override
    public String getCommandDescription() {
        return "Prompts users to rate the conversation and whether or not it should be stopped.";
    }

    @Nonnull
    @Override
    public String getCommandAuthor() {
        return "Suhndern";
    }
}
