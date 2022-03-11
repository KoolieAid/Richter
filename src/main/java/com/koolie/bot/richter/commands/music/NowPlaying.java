package com.koolie.bot.richter.commands.music;

import com.koolie.bot.richter.MusicUtil.MusicManager;
import com.koolie.bot.richter.commands.Interfaces.TextCommand;
import com.koolie.bot.richter.util.MusicUtil;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

public class NowPlaying implements TextCommand {
    private final String line = "▬";
    private final String now = ":radio_button:";
    private final int totalSize = 20;

    public NowPlaying() {}

    @NotNull
    @Override
    public String getName() {
        return "Now Playing";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Shows the currently playing song";
    }

    @NotNull
    @Override
    public CommandType getCommandType() {
        return CommandType.Music;
    }

    @NotNull
    @Override
    public String getOperator() {
        return "nowplaying";
    }

    @Nullable
    @Override
    public String[] getAliases() {
        return new String[]{"np"};
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
            message.reply("There's nothing playing").queue();
            return;
        }

        String durationString = MusicUtil.getReadableMusicTime(track.getDuration());
        String positionString = MusicUtil.getReadableMusicTime(track.getPosition());

        /*
        Builds progress bar
        made by: cane from Spidey and JDA discord
         */
        int activeBlocks = (int) ((float) track.getPosition() / track.getDuration() * totalSize);
        StringBuilder progressBar = new StringBuilder();
        for (var i = 0; i < totalSize; i++)
            progressBar.append(i == activeBlocks ? now : line);
        progressBar.append(line);

        EmbedBuilder eb = new EmbedBuilder();
        eb.setThumbnail("http://img.youtube.com/vi/" + track.getIdentifier() + "/maxresdefault.jpg");
        eb.setTitle(track.getInfo().title, track.getInfo().uri).setColor(Color.RED);
        eb.setDescription(progressBar + "\t**[" + positionString + "/" + durationString + "]**");
        message.replyEmbeds(eb.build()).queue();
    }
}
