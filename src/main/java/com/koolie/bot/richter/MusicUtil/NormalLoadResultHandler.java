package com.koolie.bot.richter.MusicUtil;

import com.koolie.bot.richter.objects.context.Context;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import io.sentry.Sentry;
import lombok.Setter;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.Color;

public class NormalLoadResultHandler implements AudioLoadResultHandler {
    private @Setter MusicManager manager;
    private @Setter Context context;
    private @Setter boolean isFront;
    private @Setter String trackIdentifier;

    @Override
    public void trackLoaded(AudioTrack track) {
        manager.eventListener.setChannel(context.getChannel().getIdLong());
        track.setUserData(context.getUser().getAsMention());
        EmbedBuilder eb = new EmbedBuilder();
        eb.setDescription("Queued: `" + track.getInfo().title + "`")
                .setFooter(context.getAuthor().getName(), context.getAuthor().getEffectiveAvatarUrl())
                .setColor(Color.CYAN);

        context.replyEmbeds(eb.build()).queue();

        if (isFront) {
            manager.eventListener.queueFront(track);
            return;
        }
        manager.eventListener.queue(track);
    }

    @Override
    public void playlistLoaded(AudioPlaylist playlist) {
        EmbedBuilder eb = new EmbedBuilder();

        manager.eventListener.setChannel(context.getChannel().getIdLong());
        if (playlist.isSearchResult()) {
            AudioTrack track = playlist.getTracks().get(0);
            track.setUserData(context.getUser().getAsMention());

            eb.setDescription("Queued: `" + track.getInfo().title + "`")
                    .setFooter(context.getAuthor().getName(), context.getAuthor().getEffectiveAvatarUrl())
                    .setColor(Color.CYAN);

            context.replyEmbeds(eb.build()).queue();

            if (isFront) {
                manager.eventListener.queueFront(track);
                return;
            }
            manager.eventListener.queue(track);
            return;
        }

        eb.setDescription(playlist.getTracks().size() + " songs have been added from: `" + playlist.getName() + "`")
                .setFooter(context.getAuthor().getName(), context.getAuthor().getEffectiveAvatarUrl())
                .setColor(Color.CYAN);
        context.replyEmbeds(eb.build()).queue();

        for (AudioTrack track : playlist.getTracks()) {
            track.setUserData(context.getUser().getAsMention());
            manager.eventListener.queue(track);
        }
    }

    @Override
    public void noMatches() {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setDescription("No matches found for: `" + trackIdentifier + "`")
                .setFooter(context.getAuthor().getName(), context.getAuthor().getEffectiveAvatarUrl())
                .setColor(Color.RED);

        context.replyEmbeds(eb.build()).queue();
        if (manager.eventListener.queue.size() == 0) {
            manager.eventListener.scheduleLeave();
        }

        Sentry.captureMessage("No search results for identifier: " + trackIdentifier);
    }

    @Override
    public void loadFailed(FriendlyException exception) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setDescription("Could not play: `" + trackIdentifier + "`" + "\nReason: " + exception.getMessage())
                .setFooter(context.getAuthor().getName(), context.getAuthor().getEffectiveAvatarUrl())
                .setColor(Color.RED);
        context.replyEmbeds(eb.build()).queue();

        if (manager.eventListener.queue.size() == 0) {
            manager.eventListener.scheduleLeave();
        }

        Sentry.captureMessage("Failed to load track: " + exception.getMessage() + "\nIdentifier: " + trackIdentifier);
    }
}
