package com.koolie.bot.richter.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import org.jetbrains.annotations.NotNull;

public class Invite implements TextCommand {
    public Invite() {
    }

    @NotNull
    @Override
    public String getName() {
        return "invite";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Sends a link to invite me. ;)";
    }

    @NotNull
    @Override
    public Command.CommandType getCommandType() {
        return CommandType.General;
    }

    @NotNull
    @Override
    public String getOperator() {
        return "invite";
    }

    @Override
    public void execute(Message message) {
        EmbedBuilder ebuilder = new EmbedBuilder()
                .setDescription("""
                        How to invite me:
                        1. Click on my profile
                        2. Click on the "Add to Server" button
                        3. ???
                        4. Profit
                        """);
        message.replyEmbeds(ebuilder.build()).queue();
    }
}
