package com.koolie.bot.richter.commands.music;

import com.koolie.bot.richter.commands.Command;
import com.koolie.bot.richter.commands.TextCommand;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

public class Disconnect implements TextCommand {
    public Disconnect() {
    }

    @NotNull
    @Override
    public String getName() {
        return "Disconnect";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Disconnects from the voice channel";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"dc", "leave"};
    }

    @NotNull
    @Override
    public Command.CommandType getCommandType() {
        return CommandType.Music;
    }

    @NotNull
    @Override
    public String getOperator() {
        return "disconnect";
    }

    @Override
    public void execute(Message message) {
    //TODO: prevent other users from using this command when they are not in the channel
        if (!message.getGuild().getAudioManager().isConnected()) {
            message.reply("Seems like I already disconnected").queue();
            return;
        }

        message.getGuild().getAudioManager().closeAudioConnection();
        message.reply("Disconnected from channel").queue();
    }
}
