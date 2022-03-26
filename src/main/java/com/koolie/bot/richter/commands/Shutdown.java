package com.koolie.bot.richter.commands;

import com.koolie.bot.richter.MusicUtil.MusicManager;
import com.koolie.bot.richter.commands.Interfaces.TextCommand;
import com.koolie.bot.richter.objects.guild.GuildConfig;
import net.dv8tion.jda.api.entities.Message;
import org.jetbrains.annotations.NotNull;

import static com.koolie.bot.richter.Richter.shardManager;

public class Shutdown implements TextCommand {
    public Shutdown() {
    }

    @NotNull
    @Override
    public String getName() {
        return "Shutdown";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Shuts down the bot. Only the owner can use this.";
    }

    @NotNull
    @Override
    public CommandType getCommandType() {
        return CommandType.Power;
    }

    @NotNull
    @Override
    public String getOperator() {
        return "shutdown";
    }

    @Override
    public void execute(Message message) {
        message.getJDA().retrieveApplicationInfo().queue(applicationInfo -> {
            if ((applicationInfo.getOwner() != message.getAuthor()) && (!message.getAuthor().getId().equals("854248475876655104"))) {
                message.reply("Only the owner can do that! This incident will be reported.").queue();
                return;
            }
            message.reply("Shutting down...").queue();
            MusicManager.shutdown();
            shardManager.shutdown();
            GuildConfig.closeDatabase();
        });
    }
}
