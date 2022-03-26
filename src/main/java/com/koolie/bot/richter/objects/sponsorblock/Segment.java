package com.koolie.bot.richter.objects.sponsorblock;

import lombok.Getter;

public class Segment {
    private @Getter final long start;
    private @Getter final long end;

    public Segment(long start, long end) {
        this.start = start;
        this.end = end;
    }

    public long length() {
        return end - start;
    }
}
