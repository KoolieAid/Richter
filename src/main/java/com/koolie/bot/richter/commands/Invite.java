package com.koolie.bot.richter.commands;

import com.koolie.bot.richter.commands.Interfaces.Command;
import com.koolie.bot.richter.commands.Interfaces.TextCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

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
    public void execute(@NotNull Message message) {
        EmbedBuilder ebuilder = new EmbedBuilder()
                .setDescription("""
                        How to invite me:
                        1. Click on my profile
                        2. Click on the "Add to Server" button
                        3. ???
                        4. Profit
                        
                        Or copy and paste this link:
                        https://discord.com/api/oauth2/authorize?client_id=881408982802116618&permissions=8&scope=bot%20applications.commands
                        """);


        message.replyEmbeds(ebuilder.build()).queue();
    }
}
