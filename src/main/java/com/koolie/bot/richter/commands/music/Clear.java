package com.koolie.bot.richter.commands.music;

import com.koolie.bot.richter.MusicUtil.MusicManager;
import com.koolie.bot.richter.commands.Interfaces.TextCommand;
import com.koolie.bot.richter.util.MusicUtil;
import net.dv8tion.jda.api.entities.Message;
import org.jetbrains.annotations.NotNull;

public class Clear implements TextCommand {
    public Clear() {
    }

    @NotNull
    @Override
    public String getName() {
        return "Clear";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Clears the queue, doesn't stop the currently playing track";
    }

    @NotNull
    @Override
    public CommandType getCommandType() {
        return CommandType.Music;
    }

    @NotNull
    @Override
    public String getOperator() {
        return "clear";
    }

    @Override
    public void execute(@NotNull Message message) {
        if (!message.getGuild().getAudioManager().isConnected()) {
            message.reply("Seems like I already disconnected").queue();
            return;
        }

        if (!MusicUtil.isInSameChannel(message.getMember().getVoiceState().getChannel(), message.getMember(), message.getGuild().getSelfMember())) {
            message.reply("Hey! Have some manners. Other people are using me.\nYou have to join the same voice channel to be able to use this command").queue();
            return;
        }

        MusicManager gManager = MusicManager.of(message.getGuild());
        if (gManager.eventListener.getCurrentMode() == RepeatMode.Queue) {
            gManager.eventListener.setRepeatOff();
        }
        gManager.eventListener.queue.clear();

        message.reply("Cleared the queue").queue();
    }
}
