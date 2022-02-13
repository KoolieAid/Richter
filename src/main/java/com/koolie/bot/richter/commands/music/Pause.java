package com.koolie.bot.richter.commands.music;

import com.koolie.bot.richter.MusicUtil.MusicManager;
import com.koolie.bot.richter.commands.TextCommand;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Message;
import org.jetbrains.annotations.NotNull;

public class Pause implements TextCommand {
    public Pause() {
    }

    @NotNull
    @Override
    public String getName() {
        return "Pause";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Pauses the music duh";
    }

    @NotNull
    @Override
    public CommandType getCommandType() {
        return CommandType.Music;
    }

    @NotNull
    @Override
    public String getOperator() {
        return "pause";
    }

    @Override
    public void execute(Message message) {
        if (!message.getGuild().getAudioManager().isConnected()) {
            message.reply("I'm not in a channel bro").queue();
            return;
        }

        MusicManager gManager = MusicManager.of(message.getGuild());
        AudioTrack track = gManager.audioPlayer.getPlayingTrack();
        if (track == null) {
            message.reply("Nothing is playing").queue();
            return;
        }
        gManager.audioPlayer.setPaused(true);

        message.reply(track.getInfo().title + " has been paused").queue();
    }
}
