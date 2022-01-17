package com.koolie.bot.richter.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

public class Team extends Command {
    private final static String[] maps = {
            "Ascent", "Split", "Bind", "Haven", "Breeze", "Icebox", "Fracture"
    };
    private final Random random;

    public Team() {
        setName("Team");
        setDescription("Scramble teams for games");
        setCommandType(commandType.Other);
        random = new Random();
    }

    @Override
    public void execute(MessageReceivedEvent event) {
        LinkedList<Member> mentionedMembers = new LinkedList<>(event.getMessage().getMentionedMembers());

        if (mentionedMembers.size() < 2) {
            event.getChannel().sendMessage("You need at least 2 people to make a team!").queue();
            return;
        }

        ArrayList<Member> team1 = new ArrayList<>();
        ArrayList<Member> team2 = new ArrayList<>();

        int cap = event.getMessage().getMentionedMembers().size();

        Member extra = null;

        if (mentionedMembers.size() % 2 != 0) {
            int index = random.nextInt(mentionedMembers.size());
            extra = mentionedMembers.get(index);
            cap--;
            mentionedMembers.remove(index);
        }

        // Team 1
        for (int i = 0; i < cap / 2; i++) {
            Member temp = mentionedMembers.get(random.nextInt(mentionedMembers.size()));
            team1.add(temp);
            mentionedMembers.remove(temp);
        }

        // Team 2
        for (int i = 0; i < cap / 2; i++) {
            Member temp = mentionedMembers.get(random.nextInt(mentionedMembers.size()));
            team2.add(temp);
            mentionedMembers.remove(temp);
        }

        // String Builders of teams
        StringBuilder team1Builder = new StringBuilder();
        StringBuilder team2Builder = new StringBuilder();

        team1.forEach((m) -> team1Builder.append(m.getAsMention() + "\n"));
        team2.forEach((m) -> team2Builder.append(m.getAsMention() + "\n"));

        EmbedBuilder eBuilder = new EmbedBuilder();
        eBuilder.setAuthor("Valorant Teams", null, "https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Fcdn.publish0x.com%2Fprod%2Ffs%2Fimages%2F6ac0ff5feb2e723eaa18dace82b96ab9aca5ed93038ad2d739f3d58132cc3bed.png&f=1&nofb=1");
        eBuilder.setTitle("Team Scramble");
        eBuilder.setFooter("Requested by: " + event.getAuthor().getName(), event.getAuthor().getAvatarUrl());

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

        event.getMessage().replyEmbeds(embed).queue();

    }
}
