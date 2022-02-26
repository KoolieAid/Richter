package com.koolie.bot.richter.objects;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.requests.RestAction;

public class Context {
    Message message;
    IReplyCallback interaction;

    public Context(Message message) {
        this.message = message;
    }

    public Context(IReplyCallback interaction) {
        this.interaction = interaction;
    }

    public RestAction reply(String message) {
        if (this.message != null) {
            return this.message.reply(message);
        }
        return this.interaction.reply(message).setEphemeral(true);
    }

    public RestAction replyEmbeds(MessageEmbed embed) {
        if (this.message != null) {
            return this.message.replyEmbeds(embed);
        }
        return this.interaction.replyEmbeds(embed).setEphemeral(true);
    }

    public MessageChannel getChannel() {
        if (this.message != null) {
            return this.message.getChannel();
        }
        return this.interaction.getMessageChannel();
    }

    public Guild getGuild() {
        if (this.message != null) {
            return this.message.getGuild();
        }
        return this.interaction.getGuild();
    }

    public User getAuthor() {
        if (this.message != null) {
            return this.message.getAuthor();
        }
        return this.interaction.getUser();
    }

    public User getUser() {
        return getAuthor();
    }
}
