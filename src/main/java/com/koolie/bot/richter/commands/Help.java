package com.koolie.bot.richter.commands;

import com.koolie.bot.richter.EventHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;
import java.util.HashMap;

public class Help extends Command {
    public Help() {
        setName("Help");
        setDescription("Gives you info");
        setCommandType(commandType.General);
    }

    @Override
    public void execute(MessageReceivedEvent event) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setColor(Color.BLUE);

        embedBuilder.setTitle("Help Menu");
        embedBuilder.setDescription("Bot made by Chad Thundercock, originally made for a private server");
        embedBuilder.setFooter("Try to find the easter egg \uD83D\uDE09");

        embedBuilder.addField("General", getHelpString(commandType.General), false);
        embedBuilder.addField("Music", getHelpString(commandType.Music), false);
        embedBuilder.addField("Power", getHelpString(commandType.Power), false);
        embedBuilder.addField("Other", getHelpString(commandType.Other), false);

        embedBuilder.setAuthor(event.getJDA().getSelfUser().getName(), "https://www.youtube.com/watch?v=dQw4w9WgXcQ", event.getJDA().getSelfUser().getAvatarUrl());

        event.getMessage().replyEmbeds(embedBuilder.build()).queue();

    }

    private String getHelpString(commandType type) {
        StringBuilder stringBuilder = new StringBuilder();

        HashMap<String, Command> map = EventHandler.getCommands();

        map.forEach((String key, Command command) -> {
            if (command.getCommandType() == type) {
                stringBuilder.append(key).append(", ");
            }
        });

        return stringBuilder.toString();
    }


}
