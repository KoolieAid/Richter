package com.koolie.bot.richter.commands.music;

import com.koolie.bot.richter.MusicUtil.MusicManager;
import com.koolie.bot.richter.commands.TextCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class Stop implements TextCommand {
    public Stop() {}

    @NotNull
    @Override
    public String getName() {
        return "Stop";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Clears the queue and stop the music";
    }

    @NotNull
    @Override
    public CommandType getCommandType() {
        return CommandType.Music;
    }

    @NotNull
    @Override
    public String getOperator() {
        return "stop";
    }

    @Override
    public void execute(Message message) {
        if (!message.getGuild().getAudioManager().isConnected()) {
            message.reply("Seems like I already disconnected").queue();
            return;
        }

        MusicManager gManager = MusicManager.of(message.getGuild());
        if (gManager.eventListener.queue.size() != 0) {
            gManager.eventListener.queue.clear();
        }
        gManager.eventListener.setRepeatOff();
        gManager.eventListener.nextTrack();

        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Stopped Playback").setColor(Color.RED);
        message.replyEmbeds(eb.build()).queue();
    }
}
