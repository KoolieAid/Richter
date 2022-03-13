package com.koolie.bot.richter.objects.spotify;

import com.koolie.bot.richter.SourceManagers.SpotifySourceManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.track.*;
import com.sedmelluq.discord.lavaplayer.track.playback.LocalAudioTrackExecutor;

public class SpotifyTrack extends DelegatedAudioTrack {
    private final AudioTrackInfo trackInfo;
    private final SpotifySourceManager sourceManager;

    public SpotifyTrack(String title, String author, long length, SpotifySourceManager sourceManager) {
        super(new AudioTrackInfo(title, author, length, null, false, null));
        trackInfo = super.getInfo();
        this.sourceManager = sourceManager;
    }

    @Override
    public void process(LocalAudioTrackExecutor executor) throws Exception {
        String trackName = trackInfo.title;
        String firstArtistName = trackInfo.author;

        AudioPlaylist ytSearchList = (AudioPlaylist) sourceManager.ytSourceManager.loadItem(null,
                new AudioReference("ytmsearch:" + trackName + " " + firstArtistName, null));

        //Gets the first result, and replaces the spotify track into YouTube Music track
        InternalAudioTrack track = (InternalAudioTrack) ytSearchList.getTracks().get(0);

        super.processDelegate(track, executor);
    }

    @Override
    public long getDuration() {
        return trackInfo.length;
    }

    @Override
    public AudioTrack makeClone() {
        return new SpotifyTrack(trackInfo.title, trackInfo.author, trackInfo.length, sourceManager);
    }

    @Override
    public AudioSourceManager getSourceManager() {
        return sourceManager;
    }

    @Override
    public void setUserData(Object userData) {

    }

}
