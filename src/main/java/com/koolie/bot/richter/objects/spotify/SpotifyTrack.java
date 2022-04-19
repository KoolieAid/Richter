package com.koolie.bot.richter.objects.spotify;

import com.koolie.bot.richter.MusicUtil.MusicManager;
import com.koolie.bot.richter.SourceManagers.SpotifySourceManager;
import com.sedmelluq.discord.lavaplayer.player.FunctionalResultHandler;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.*;
import com.sedmelluq.discord.lavaplayer.track.playback.LocalAudioTrackExecutor;
import io.sentry.Sentry;
import lombok.Setter;
import se.michaelthelin.spotify.exceptions.detailed.NotFoundException;

import java.util.concurrent.CompletableFuture;

public class SpotifyTrack extends DelegatedAudioTrack {
    private String identifier = null;

    public SpotifyTrack(String title, String author, long length, String uri) {
        this(new AudioTrackInfo(title, author, length, null, false, uri));
    }

    public SpotifyTrack(AudioTrackInfo trackInfo) {
        super(trackInfo);
    }

    @Override
    public void process(LocalAudioTrackExecutor executor) throws Exception {
        String trackName = trackInfo.title;
        String firstArtistName = trackInfo.author;

        CompletableFuture<AudioTrack> future = new CompletableFuture<>();

        MusicManager.getAudioPlayerManager().loadItem("ytmsearch:" + trackName + " " + firstArtistName, new FunctionalResultHandler(null,
                playlist -> {
                    AudioTrack track = playlist.getTracks().get(0);
                    future.complete(track);
                }, () -> future.completeExceptionally(new FriendlyException("Could not find an equivalent track in the database.", FriendlyException.Severity.COMMON, new NotFoundException())),
                future::completeExceptionally));

        AudioTrack track;
        try {
            track = future.join();
        } catch (Exception e) {
            Sentry.captureException(e, "Error loading track: " + trackName + " by " + firstArtistName);
            throw e;
        }
        identifier = track.getInfo().identifier;
        super.processDelegate((InternalAudioTrack) track, executor);
    }

    @Override
    public AudioTrack makeClone() {
        return new SpotifyTrack(trackInfo);
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }
}
