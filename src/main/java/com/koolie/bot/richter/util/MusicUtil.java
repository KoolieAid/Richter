package com.koolie.bot.richter.util;

import net.dv8tion.jda.api.entities.AudioChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.VoiceChannel;

import java.time.Duration;

public class MusicUtil {
    public static String getReadableMusicTime(long millis) {

        Duration fullDuration = Duration.ofMillis(millis);
        int fullHours = fullDuration.toHoursPart();
        int fullMinutes = fullDuration.toMinutesPart();
        int fullSeconds = fullDuration.toSecondsPart();

        String durationString;
        if (fullHours == 0) {
            durationString = String.format("%02d:%02d", fullMinutes, fullSeconds);
        } else {
            durationString = String.format("%02d:%02d:%02d", fullHours, fullMinutes, fullSeconds);
        }

        return durationString;
    }

    public static boolean isInSameChannel(AudioChannel channel, Member member1, Member member2) {
        if (channel == null) return false;
        return channel.getMembers().contains(member1) && channel.getMembers().contains(member2);
    }

    public static boolean isInSameChannel(VoiceChannel channel, Member member1, Member member2) {
        if (channel == null) return false;
        return channel.getMembers().contains(member1) && channel.getMembers().contains(member2);
    }
}
