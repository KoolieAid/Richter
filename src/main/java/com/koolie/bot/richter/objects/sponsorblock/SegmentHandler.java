package com.koolie.bot.richter.objects.sponsorblock;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.TrackMarker;
import com.sedmelluq.discord.lavaplayer.track.TrackMarkerHandler;
import lombok.Getter;

import java.util.List;

public class SegmentHandler implements TrackMarkerHandler {

    private @Getter
    final List<Segment> segments;
    private final AudioTrack track;
    private int segmentIndex = 0;

    public SegmentHandler(AudioTrack track, List<Segment> segments) {
        this.track = track;
        this.segments = segments;
    }

    @Override
    public void handle(MarkerState state) {
        if (!(state == MarkerState.REACHED || state == MarkerState.LATE)) return;

        Segment segment = segments.get(segmentIndex);

        if (segment.getEnd() > track.getPosition())
            track.setPosition(segment.getEnd());

        segmentIndex++;

        if (segmentIndex >= segments.size()) return;

        track.setMarker(new TrackMarker(segments.get(segmentIndex).getStart(), this));

    }
}
