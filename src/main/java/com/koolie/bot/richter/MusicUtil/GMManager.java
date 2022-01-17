package com.koolie.bot.richter.MusicUtil;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;

// Unique to each guild

/**
 * Stores both the AudioPlayer and the Scheduler for each guild
 */
public class GMManager {
    public AudioPlayer audioPlayer;
    public AudioPlayerEventListener eventListener;

    /**
     * Allocates audio player and Event Listener for the guild
     *
     * @param playerManager AudioPlayerManager to create a player and link a new scheduler for it
     */
    public GMManager(AudioPlayerManager playerManager) {
        audioPlayer = playerManager.createPlayer();

        // Links the two, inside them has a variable of the opposite
        eventListener = new AudioPlayerEventListener(audioPlayer);
        audioPlayer.addListener(eventListener);

        audioPlayer.setVolume(20);
    }

    /**
     * Wrapper for audio player
     *
     * @return Returns a wrapped *JDA* AudioSendHandler with a LavaPlayer audio player
     */
    public AudioPlayerSendHandler getSendHandler() {
        return new AudioPlayerSendHandler(audioPlayer);
    }

    public boolean isPlaying() {
    	return audioPlayer.getPlayingTrack() != null;
    }

}
