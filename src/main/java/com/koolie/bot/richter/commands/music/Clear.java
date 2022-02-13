package com.koolie.bot.richter.commands.music;

import com.koolie.bot.richter.MusicUtil.MusicManager;
import com.koolie.bot.richter.commands.TextCommand;
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
    public void execute(Message message) {
        if (!message.getGuild().getAudioManager().isConnected()) {
            message.reply("Seems like I already disconnected").queue();
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
