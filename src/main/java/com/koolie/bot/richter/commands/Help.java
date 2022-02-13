package com.koolie.bot.richter.commands;

import com.koolie.bot.richter.EventHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.lang.reflect.Field;
import java.util.HashMap;

public class Help implements TextCommand {
    public Help() {}

    @Override
    public String getName() {
        return "Help";
    }

    @Override
    public String getDescription() {
        return "Gives you info";
    }

    @Override
    public CommandType getCommandType() {
        return CommandType.General;
    }

    @NotNull
    @Override
    public String getOperator() {
        return "help";
    }

    @Override
    public void execute(Message message) {
        if (message.getContentRaw().split(" ").length > 1) {
            message.replyEmbeds(getSpecific(message.getContentRaw().split(" ")[1])).queue();
            return;
        }

        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setColor(Color.BLUE);

        embedBuilder.setTitle("Help Menu");
        embedBuilder.setDescription("Bot made by Chad Thundercock, originally made for a private server");
        embedBuilder.setFooter("Try to find the easter egg \uD83D\uDE09");

        embedBuilder.addField("General", getHelpString(CommandType.General), false);
        embedBuilder.addField("Music", getHelpString(CommandType.Music), false);
        embedBuilder.addField("Power", getHelpString(CommandType.Power), false);
        embedBuilder.addField("Other", getHelpString(CommandType.Other), false);

        embedBuilder.setAuthor(message.getJDA().getSelfUser().getName(), "https://www.youtube.com/watch?v=dQw4w9WgXcQ", message.getJDA().getSelfUser().getAvatarUrl());

        message.replyEmbeds(embedBuilder.build()).queue();

    }

    private String getHelpString(CommandType type) {
        StringBuilder stringBuilder = new StringBuilder();

        HashMap<String, TextCommand> map = EventHandler.getCommands();

        map.forEach((String key, TextCommand command) -> {
            if (command.getCommandType() == type) {
                stringBuilder.append(key).append(", ");
            }
        });

        return stringBuilder.toString();
    }

    private MessageEmbed getSpecific(String cmd) {
        HashMap<String, TextCommand> map = EventHandler.getCommands();
        if (!map.containsKey(cmd)) {
            return new EmbedBuilder().setColor(Color.RED).setTitle("Command not found").build();
        }
        TextCommand command = map.get(cmd);

        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Color.BLUE)
                .setTitle(command.getName())
                .setDescription(command.getDescription())
                .addField("Operator", command.getOperator(), false);

        if (command.getAliases() != null) {
            MessageEmbed.Field field;

            StringBuilder aliases = new StringBuilder();
            for (String alias : command.getAliases()) {
                aliases.append(alias).append(", ");
            }

            field = new MessageEmbed.Field("Aliases", aliases.toString(), false);
            builder.addField(field);
        }

        return builder.build();
    }
}
