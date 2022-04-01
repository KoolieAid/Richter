package com.koolie.bot.richter.objects.spotify;

import com.koolie.bot.richter.MusicUtil.MusicManager;
import com.koolie.bot.richter.SourceManagers.SpotifySourceManager;
import com.sedmelluq.discord.lavaplayer.player.FunctionalResultHandler;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.*;
import com.sedmelluq.discord.lavaplayer.track.playback.LocalAudioTrackExecutor;
import lombok.Setter;
import se.michaelthelin.spotify.exceptions.detailed.NotFoundException;

import java.util.concurrent.CompletableFuture;

public class SpotifyTrack extends DelegatedAudioTrack {
    private final SpotifySourceManager sourceManager;
    @Setter private String identifier = null;

    public SpotifyTrack(String title, String author, long length, String uri, SpotifySourceManager sourceManager) {
        this(new AudioTrackInfo(title, author, length, null, false, uri), sourceManager);
    }

    public SpotifyTrack(AudioTrackInfo trackInfo, SpotifySourceManager sourceManager) {
        super(trackInfo);
        this.sourceManager = sourceManager;
    }

    @Override
    public void process(LocalAudioTrackExecutor executor) throws Exception {
        String trackName = trackInfo.title;
        String firstArtistName = trackInfo.author;

        CompletableFuture<AudioTrack> future = new CompletableFuture<>();

        MusicManager.getAudioPlayerManager().loadItem("ytmsearch:" + trackName + " " + firstArtistName, new FunctionalResultHandler(null,
                playlist -> {
                    AudioTrack track = playlist.getTracks().get(0);
                    setIdentifier(track.getIdentifier());
                    future.complete(track);
                }, () -> future.completeExceptionally(new FriendlyException("Could not find an equivalent track in the database.", FriendlyException.Severity.COMMON, new NotFoundException())),
                future::completeExceptionally));

        super.processDelegate((InternalAudioTrack) future.join(), executor);
    }

    @Override
    public AudioTrack makeClone() {
        return new SpotifyTrack(trackInfo, sourceManager);
    }

    @Override
    public AudioSourceManager getSourceManager() {
        return sourceManager;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }
}
