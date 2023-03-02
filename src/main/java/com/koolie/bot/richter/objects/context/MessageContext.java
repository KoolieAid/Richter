package com.koolie.bot.richter.objects.context;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.requests.RestAction;

public class MessageContext implements Context {
    private final Message message;

    public MessageContext(Message message) {
        this.message = message;
    }

    @Override
    public RestAction<?> reply(String message) {
        return this.message.reply(message);
    }

    @Override
    public RestAction<?> replyEmbeds(MessageEmbed embed) {
        return this.message.replyEmbeds(embed);
    }

    @Override
    public MessageChannel getChannel() {
        return message.getChannel();
    }

    @Override
    public Guild getGuild() {
        return message.getGuild();
    }

    @Override
    public User getAuthor() {
        return message.getAuthor();
    }

    @Override
    public User getUser() {
        return getAuthor();
    }

    @Override
    public Member getMember() {
        return message.getMember();
    }
}
