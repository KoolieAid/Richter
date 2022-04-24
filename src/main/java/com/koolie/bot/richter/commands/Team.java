package com.koolie.bot.richter.commands;

import com.koolie.bot.richter.commands.Interfaces.TextCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

public class Team implements TextCommand {
    private final static String[] maps = {
            "Ascent", "Split", "Bind", "Haven", "Breeze", "Icebox", "Fracture"
    };
    private final Random random;

    public Team() {
        random = new Random();
    }

    @Override
    public void execute(@NotNull Message message) {
        LinkedList<User> mentionedUsers = new LinkedList<>(message.getMentionedUsers());

        if (mentionedUsers.size() < 2) {
            message.getChannel().sendMessage("You need at least 2 people to make a team!").queue();
            return;
        }

        ArrayList<User> team1 = new ArrayList<>();
        ArrayList<User> team2 = new ArrayList<>();

        int cap = mentionedUsers.size();

        User extra = null;

        if (mentionedUsers.size() % 2 != 0) {
            int index = random.nextInt(mentionedUsers.size());
            extra = mentionedUsers.get(index);
            cap--;
            mentionedUsers.remove(index);
        }

        // Team 1
        for (int i = 0; i < cap / 2; i++) {
            User temp = mentionedUsers.get(random.nextInt(mentionedUsers.size()));
            team1.add(temp);
            mentionedUsers.remove(temp);
        }

        // Team 2
        for (int i = 0; i < cap / 2; i++) {
            User temp = mentionedUsers.get(random.nextInt(mentionedUsers.size()));
            team2.add(temp);
            mentionedUsers.remove(temp);
        }

        // String Builders of teams
        StringBuilder team1Builder = new StringBuilder();
        StringBuilder team2Builder = new StringBuilder();

        team1.forEach((m) -> team1Builder.append(m.getAsMention() + "\n"));
        team2.forEach((m) -> team2Builder.append(m.getAsMention() + "\n"));

        EmbedBuilder eBuilder = new EmbedBuilder();
        eBuilder.setAuthor("Valorant Teams", null, "https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Fcdn.publish0x.com%2Fprod%2Ffs%2Fimages%2F6ac0ff5feb2e723eaa18dace82b96ab9aca5ed93038ad2d739f3d58132cc3bed.png&f=1&nofb=1");
        eBuilder.setTitle("Team Scramble");
        eBuilder.setFooter("Requested by: " + message.getAuthor().getName(), message.getAuthor().getAvatarUrl());

        StringBuilder stringBuilder = eBuilder.getDescriptionBuilder();

        if (extra != null) {
            stringBuilder.append("Spectator: " + extra.getAsMention());
        } else {
            stringBuilder.append("No Spectators");
        }
        stringBuilder.append("\nMap: **" + maps[random.nextInt(maps.length)] + "**");

        eBuilder.addField("Team 1", team1Builder.toString(), true);
        eBuilder.addField("Team 2", team2Builder.toString(), true);

        eBuilder.setColor(Color.RED);
        MessageEmbed embed = eBuilder.build();

        message.replyEmbeds(embed).queue();
    }

    @NotNull
    @Override
    public String getName() {
        return "Team";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Scramble teams for games";
    }

    @NotNull
    @Override
    public CommandType getCommandType() {
        return CommandType.Other;
    }

    @NotNull
    @Override
    public String getOperator() {
        return "team";
    }

    @Nullable
    @Override
    public String[] getAliases() {
        return new String[]{"teams", "maketeams", "maketeam"};
    }
}
