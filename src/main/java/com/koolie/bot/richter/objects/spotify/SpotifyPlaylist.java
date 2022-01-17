package com.koolie.bot.richter.objects.spotify;

import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import java.util.LinkedList;
import java.util.List;

public class SpotifyPlaylist implements AudioPlaylist {

    public List<AudioTrack> tracks;
    public String name;

    public SpotifyPlaylist() {
        tracks = new LinkedList<>();
    }

    public void addTrack(SpotifyTrack track) {
        tracks.add(track);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<AudioTrack> getTracks() {
        return tracks;
    }

    @Override
    public AudioTrack getSelectedTrack() {
        return null;
    }

    @Override
    public boolean isSearchResult() {
        return false;
    }
}
