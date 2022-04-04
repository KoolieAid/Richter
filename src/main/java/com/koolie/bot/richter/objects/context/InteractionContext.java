package com.koolie.bot.richter.objects.context;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.requests.RestAction;

public class InteractionContext implements Context {
    private final IReplyCallback interaction;

    public InteractionContext(IReplyCallback interaction) {
        this.interaction = interaction;
    }

    @Override
    public RestAction<?> reply(String message) {
        return interaction.reply(message).setEphemeral(true);
    }

    @Override
    public RestAction<?> replyEmbeds(MessageEmbed embed) {
        return interaction.replyEmbeds(embed).setEphemeral(true);
    }

    @Override
    public MessageChannel getChannel() {
        return interaction.getMessageChannel();
    }

    @Override
    public Guild getGuild() {
        return interaction.getGuild();
    }

    @Override
    public User getAuthor() {
        return getUser();
    }

    @Override
    public User getUser() {
        return interaction.getUser();
    }

    @Override
    public Member getMember() {
        return interaction.getMember();
    }
}
