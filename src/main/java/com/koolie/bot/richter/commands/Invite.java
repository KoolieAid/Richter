package com.koolie.bot.richter.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class Invite extends Command {
    public Invite() {
        setName("invite");
        setDescription("Sends a link to invite me. ;)");
        setCommandType(commandType.General);
    }

    @Override
    public void execute(MessageReceivedEvent event) {
        EmbedBuilder ebuilder = new EmbedBuilder()
                .setDescription("""
                        How to invite me:
                        1. Click on my profile
                        2. Click on the "Add to Server" button
                        3. ???
                        4. Profit
                        """);
        event.getMessage().replyEmbeds(ebuilder.build()).queue();
    }
}
