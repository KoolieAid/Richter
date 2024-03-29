package com.koolie.bot.richter.objects.context;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.requests.RestAction;

public interface Context {
    RestAction<?> reply(String message);

    RestAction<?> replyEmbeds(MessageEmbed embed);

    MessageChannel getChannel();

    Guild getGuild();

    User getAuthor();

    User getUser();

    Member getMember();
}
