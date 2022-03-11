package com.koolie.bot.richter.commands.music;

import com.koolie.bot.richter.MusicUtil.MusicManager;
import com.koolie.bot.richter.commands.TextCommand;
import com.koolie.bot.richter.util.MusicUtil;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Message;
import org.jetbrains.annotations.NotNull;

public class Resume implements TextCommand {
    public Resume() {
    }

    @Override
    public String getName() {
        return "Pause";
    }

    @Override
    public String getDescription() {
        return "Pauses the music duh";
    }

    @Override
    public CommandType getCommandType() {
        return CommandType.Music;
    }

    @NotNull
    @Override
    public String getOperator() {
        return "resume";
    }

    @Override
    public void execute(Message message) {
        if (!message.getGuild().getAudioManager().isConnected()) {
            message.reply("I'm not in a channel bro").queue();
            return;
        }

        if (!MusicUtil.isInSameChannel(message.getMember().getVoiceState().getChannel(), message.getMember(), message.getGuild().getSelfMember())) {
            message.reply("You must be in the same voice channel as me to use this command.").queue();
            return;
        }

        MusicManager gManager = MusicManager.of(message.getGuild());
        AudioTrack track = gManager.audioPlayer.getPlayingTrack();
        if (track == null) {
            message.reply("Nothing is playing").queue();
            return;
        }
        gManager.audioPlayer.setPaused(false);

        message.reply(track.getInfo().title + " has been resumed").queue();
    }
}
