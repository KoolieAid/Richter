package com.koolie.bot.richter.MusicUtil;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import java.util.Deque;
import java.util.LinkedList;

public class AutoPlayEventListener extends AudioPlayerEventListener {

    private Deque<AudioTrack> hiddenQueue;

    public AutoPlayEventListener(AudioPlayer player, long guildId) {
        super(player, guildId);
        hiddenQueue = new LinkedList<>();
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        super.onTrackEnd(player, track, endReason);



    }
}
