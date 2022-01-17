package com.koolie.bot.richter.MusicUtil;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;
import com.sedmelluq.discord.lavaplayer.track.playback.MutableAudioFrame;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;

/**
 * Combines and wraps AudioPlayer from LavaPlayer with AudioSendHandler from JDA
 *
 * @author Erik
 */

public class AudioPlayerSendHandler implements AudioSendHandler {

    private final AudioPlayer audioPlayer;
    private AudioFrame lFrame;
    private ByteBuffer buffer;

    public AudioPlayerSendHandler(AudioPlayer player) {
        this.audioPlayer = player;
        this.lFrame = new MutableAudioFrame();
    }

    @Override
    public boolean canProvide() {
        lFrame = audioPlayer.provide();
        return lFrame != null;

    }

    @Nullable
    @Override
    public ByteBuffer provide20MsAudio() {
        return ByteBuffer.wrap(lFrame.getData());
    }

    @Override
    public boolean isOpus() {
        return true;
    }
}
