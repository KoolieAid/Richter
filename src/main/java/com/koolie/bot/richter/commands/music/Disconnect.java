package com.koolie.bot.richter.commands.music;

import com.koolie.bot.richter.commands.TextCommand;
import com.koolie.bot.richter.util.MusicUtil;
import net.dv8tion.jda.api.entities.Message;
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
    public CommandType getCommandType() {
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

        if (!MusicUtil.isInSameChannel(message.getMember().getVoiceState().getChannel(), message.getMember(), message.getGuild().getSelfMember())) {
            message.reply("Hey! Have some manners. Other people are using me.\nYou have to join the same voice channel to be able to use this command").queue();
            return;
        }

        message.getGuild().getAudioManager().closeAudioConnection();
        message.reply("Disconnected from channel").queue();
    }
}
