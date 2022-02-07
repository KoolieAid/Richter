package com.koolie.bot.richter.MusicUtil;

import com.koolie.bot.richter.SourceManagers.SpotifySourceManager;
import com.koolie.bot.richter.commands.music.RepeatMode;
import com.koolie.bot.richter.objects.spotify.SpotifyTrack;
import com.koolie.bot.richter.threading.ThreadUtil;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioReference;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class AudioPlayerEventListener extends AudioEventAdapter {
    Long expiringMessageId;

    private final AudioPlayer audioPlayer;
    public Deque<AudioTrack> queue;

    private Long channelId;
    private ScheduledFuture<?> leaveSchedule;
    private RepeatMode mode;
    private JDA jda;

    public void setJda(JDA jda) {
        this.jda = jda;
    }

    public AudioPlayerEventListener(AudioPlayer player) {
        this.audioPlayer = player;
        queue = new LinkedList<>();
    }

    public void setRepeatSingle() {
        mode = RepeatMode.Single;
    }

    public void setRepeatQueue() {
        mode = RepeatMode.Queue;
    }

    public void setRepeatOff() {
        mode = RepeatMode.Off;
    }

    public RepeatMode getCurrentMode() {
        return mode;
    }

    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Now Playing");
        embedBuilder.setDescription(track.getInfo().title);
        embedBuilder.setColor(Color.BLUE);

        sendMessageToChannel(embedBuilder.build(), true);

        //if leave is scheduled, cancel it
        cancelLeave();
    }

    public void queue(AudioTrack track) {
        if (queue.size() == 0) {
            track = checkIfSpotifyTrack(track);

            if (!audioPlayer.startTrack(track, true)) {
                track = checkIfSpotifyTrack(track);
                queue.offer(track);
            }
        } else {
            queue.offer(track);
        }
    }

    public void queueFront(AudioTrack track) {
        if (queue.size() == 0) {
            track = checkIfSpotifyTrack(track);

            if (!audioPlayer.startTrack(track, true)) {
                track = checkIfSpotifyTrack(track);
                queue.offerFirst(track);
            }
        } else {
            queue.offerFirst(track);
        }
    }

    public void nextTrack() {
        AudioTrack track = queue.poll();
        track = checkIfSpotifyTrack(track);

        if (track == null) {
            scheduleLeave();
        }

        audioPlayer.startTrack(track, false);
    }

    @Override
    public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
        LoggerFactory.getLogger(this.getClass()).error("Exception Occurred while playing track", exception);
    }

    @Override
    public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setDescription("Unfortunately the track got stuck midway: " + track.getInfo().title + "\nRestarting the track").setColor(Color.RED);
        sendMessageToChannel(embedBuilder.build(), false);

        if (mode == RepeatMode.Single) {
            audioPlayer.startTrack(null, false);
            return;
        }
        audioPlayer.startTrack(track.makeClone(), false);
    }

    @Override
    public void onPlayerPause(AudioPlayer player) {}

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        deleteMessage();
        if (mode == RepeatMode.Single) {
            audioPlayer.startTrack(track.makeClone(), false);
            return;
        }

        if (mode == RepeatMode.Queue) {
            queue(track.makeClone());
        }

        if (endReason.mayStartNext && mode != RepeatMode.Single) {
            nextTrack();
        }
    }

    /**
     * Checks if the track in parameter is a {@link com.koolie.bot.richter.objects.spotify.SpotifyTrack}
     * If the parameter is, this converts it into a YouTube track
     *
     * @param track The track to be replaced
     * @return The replaced track if parameter is a Spotify track instance, otherwise, nothing will change
     * @author Erik Go
     */
    private AudioTrack checkIfSpotifyTrack(AudioTrack track) {
        if (!(track instanceof SpotifyTrack)) return track;

        String trackName = track.getInfo().title;
        String firstArtistName = track.getInfo().author;

        AudioPlaylist ytSearchList = (AudioPlaylist) SpotifySourceManager.ytSourceManager.loadItem(null,
                new AudioReference("ytmsearch:" + trackName + " " + firstArtistName, null));

        //Gets the first result, and replaces the spotify track into YouTube Music track
        track = ytSearchList.getTracks().get(0);

        return track;
    }

    public void setChannel(Long id) {
        channelId = id;
    }

    public Long getChannelId() {
        return channelId;
    }

    public void sendMessageToChannel(String body) {
        jda.getTextChannelById(channelId).sendMessage(body).queue((message) -> {
            expiringMessageId = message.getIdLong();
        });
    }

    public void sendMessageToChannel(MessageEmbed embed) {
        jda.getTextChannelById(channelId).sendMessageEmbeds(embed).queue((message) -> {
            expiringMessageId = message.getIdLong();
        });
    }

    public void sendMessageToChannel(MessageEmbed embed, boolean delete) {
        if (delete) deleteMessage();
        jda.getTextChannelById(channelId).sendMessageEmbeds(embed).queue((message) -> {
            expiringMessageId = message.getIdLong();
        });
    }

    public void deleteMessage() {
        if (expiringMessageId == null) return;
        jda.getTextChannelById(channelId).deleteMessageById(expiringMessageId).queue();
        expiringMessageId = null;
    }

    public void cancelLeave() {
        if (leaveSchedule == null) return;
        leaveSchedule.cancel(true);
        leaveSchedule = null;
    }

    public void scheduleLeave() {
        scheduleLeave(2, TimeUnit.MINUTES);
    }

    public void scheduleLeave(int delay, TimeUnit unit) {
        if (leaveSchedule != null) return;
        leaveSchedule = ThreadUtil.getScheduler().schedule(new LeaveChannel(channelId), delay, unit);
        LoggerFactory.getLogger("Schedule Leave").debug("Leave scheduled");
    }

    public boolean isLeaving(){
        return leaveSchedule != null;
    }

    private class LeaveChannel implements Runnable {

        private final Long channelId;

        private LeaveChannel(Long channelId) {
            this.channelId = channelId;
        }

        @Override
        public void run() {
            Queue<AudioTrack> q = MusicManagerFactory.getGuildMusicManager(jda.getTextChannelById(channelId).getGuild()).eventListener.queue;
            q.clear();
            jda.getTextChannelById(channelId).getGuild().getAudioManager().closeAudioConnection();
        }
    }
}

