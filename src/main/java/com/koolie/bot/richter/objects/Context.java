package com.koolie.bot.richter.objects;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.interactions.commands.context.MessageContextInteraction;
import net.dv8tion.jda.api.requests.RestAction;

public class Context {
    Message message;
    MessageContextInteraction contextMessage;

    public Context (Message message) {
        this.message = message;
        contextMessage = null;
    }

    public Context (MessageContextInteraction contextMessage) {
        this.contextMessage = contextMessage;
        message = null;
    }

    public void reply(String message) {
        if (this.message != null) {
            this.message.reply(message).queue();
            return;
        }
        this.contextMessage.reply(message);
    }

    public RestAction replyEmbeds(MessageEmbed embed) {
        if (this.message != null) {
            return this.message.replyEmbeds(embed);
        }
        return this.contextMessage.replyEmbeds(embed);
    }

    public MessageChannel getChannel() {
        if (this.message != null) {
            return this.message.getChannel();
        }
        return this.contextMessage.getChannel();
    }

    public Guild getGuild() {
        if (this.message != null) {
            return this.message.getGuild();
        }
        return this.contextMessage.getGuild();
    }

    public User getAuthor() {
        if (this.message != null) {
            return this.message.getAuthor();
        }
        return this.contextMessage.getUser();
    }
}