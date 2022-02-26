package com.koolie.bot.richter.objects.spotify;

import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackState;
import com.sedmelluq.discord.lavaplayer.track.TrackMarker;

public class SpotifyTrack implements AudioTrack {
    private final AudioTrackInfo trackInfo;

    public SpotifyTrack(String title, String author, long length) {
        trackInfo = new AudioTrackInfo(title, author, length, null, false, null);
    }

    @Override
    public AudioTrackInfo getInfo() {
        return trackInfo;
    }

    @Override
    public String getIdentifier() {
        return null;
    }

    @Override
    public AudioTrackState getState() {
        return null;
    }

    @Override
    public void stop() {

    }

    @Override
    public boolean isSeekable() {
        return false;
    }

    @Override
    public long getPosition() {
        return 0;
    }

    @Override
    public void setPosition(long position) {

    }

    @Override
    public void setMarker(TrackMarker marker) {

    }

    @Override
    public long getDuration() {
        return trackInfo.length;
    }

    @Override
    public AudioTrack makeClone() {
        return null;
    }

    @Override
    public AudioSourceManager getSourceManager() {
        return null;
    }

    @Override
    public Object getUserData() {
        return null;
    }

    @Override
    public void setUserData(Object userData) {

    }

    @Override
    public <T> T getUserData(Class<T> klass) {
        return null;
    }
}
