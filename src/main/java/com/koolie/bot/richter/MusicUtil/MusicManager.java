package com.koolie.bot.richter.MusicUtil;

import com.koolie.bot.richter.SourceManagers.SpotifySourceManager;
import com.koolie.bot.richter.objects.Context;
import com.koolie.bot.richter.objects.guild.GuildConfig;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import io.sentry.Sentry;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.CommandAutoCompleteInteraction;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Handles the Guild Music Managers of guilds
 */
public class MusicManager {
    private static final AudioPlayerManager audioPlayerManager = new DefaultAudioPlayerManager();
    public static HashMap<Long, MusicManager> guildManagerMap = new HashMap<>();

    public AudioPlayer audioPlayer;
    public AudioPlayerEventListener eventListener;

    /**
     * Allocates audio player and Event Listener for the guild
     *
     * @param playerManager AudioPlayerManager to create a player and link a new scheduler for it
     */
    public MusicManager(AudioPlayerManager playerManager, long guildId) {
        audioPlayer = playerManager.createPlayer();

        // Links the two, inside them has a variable of the opposite
        eventListener = new AudioPlayerEventListener(audioPlayer, guildId);
        audioPlayer.addListener(eventListener);

        GuildConfig config = GuildConfig.of(guildId);
        audioPlayer.setVolume(config.getPlayerVolume());
    }

    /**
     * @param guild Guild object to get AudioManager and ID
     * @return The Specific Music Manager for that guild
     */
    public static MusicManager of(Guild guild) {
        MusicManager musicManager = guildManagerMap.get(guild.getIdLong());

        if (musicManager == null) {
            musicManager = new MusicManager(audioPlayerManager, guild.getIdLong());
            musicManager.eventListener.setJda(guild.getJDA());
            guildManagerMap.put(guild.getIdLong(), musicManager);
        }

        // Sets the sending handler of the guild to a wrapped send handler from lava player
        guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());

        return musicManager;
    }

    public static boolean isPresent(Guild guild) {
        return guildManagerMap.containsKey(guild.getIdLong());
    }

    public static void loadSources() {
        audioPlayerManager.registerSourceManager(new SpotifySourceManager());
        AudioSourceManagers.registerRemoteSources(audioPlayerManager);
        AudioSourceManagers.registerLocalSource(audioPlayerManager);

        audioPlayerManager.getConfiguration().setFilterHotSwapEnabled(true);
        audioPlayerManager.setTrackStuckThreshold(30000L);
    }

    public static void shutdown() {
        audioPlayerManager.shutdown();
    }

    public static void loadToGuild(Context message, String trackIdentifier) {
        loadToGuild(message, trackIdentifier, false);
    }

    public static void loadToGuild(Context message, String trackIdentifier, boolean isFront) {
        MusicManager gManager = of(message.getGuild());

        audioPlayerManager.loadItemOrdered(gManager, trackIdentifier, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                gManager.eventListener.setChannel(message.getChannel().getIdLong());
                track.setUserData(message.getUser().getAsMention());
                EmbedBuilder eb = new EmbedBuilder();
                eb.setDescription("Queued: `" + track.getInfo().title + "`")
                        .setFooter(message.getAuthor().getName(), message.getAuthor().getEffectiveAvatarUrl())
                        .setColor(Color.CYAN);

                message.replyEmbeds(eb.build()).queue();

                if (isFront) {
                    gManager.eventListener.queueFront(track);
                    return;
                }
                gManager.eventListener.queue(track);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                EmbedBuilder eb = new EmbedBuilder();

                gManager.eventListener.setChannel(message.getChannel().getIdLong());
                if (playlist.isSearchResult()) {
                    AudioTrack track = playlist.getTracks().get(0);
                    track.setUserData(message.getUser().getAsMention());

                    eb.setDescription("Queued: `" + track.getInfo().title + "`")
                            .setFooter(message.getAuthor().getName(), message.getAuthor().getEffectiveAvatarUrl())
                            .setColor(Color.CYAN);

                    message.replyEmbeds(eb.build()).queue();

                    if (isFront) {
                        gManager.eventListener.queueFront(track);
                        return;
                    }
                    gManager.eventListener.queue(track);
                    return;
                }

                eb.setDescription(playlist.getTracks().size() + " songs have been added from: `" + playlist.getName() + "`")
                        .setFooter(message.getAuthor().getName(), message.getAuthor().getEffectiveAvatarUrl())
                        .setColor(Color.CYAN);
                message.replyEmbeds(eb.build()).queue();

                for (AudioTrack track : playlist.getTracks()) {
                    track.setUserData(message.getUser().getAsMention());
                    gManager.eventListener.queue(track);
                }
            }

            @Override
            public void noMatches() {
                EmbedBuilder eb = new EmbedBuilder();
                eb.setDescription("No matches found for: `" + trackIdentifier + "`")
                        .setFooter(message.getAuthor().getName(), message.getAuthor().getEffectiveAvatarUrl())
                        .setColor(Color.RED);

                message.replyEmbeds(eb.build()).queue();
                if (gManager.eventListener.queue.size() == 0) {
                    gManager.eventListener.scheduleLeave();
                }

                Sentry.captureMessage("No search results for identifier: " + trackIdentifier);
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                EmbedBuilder eb = new EmbedBuilder();
                eb.setDescription("Could not play: `" + trackIdentifier + "`" + "\nReason: " + exception.getMessage())
                        .setFooter(message.getAuthor().getName(), message.getAuthor().getEffectiveAvatarUrl())
                        .setColor(Color.RED);
                message.replyEmbeds(eb.build()).queue();

                if (gManager.eventListener.queue.size() == 0) {
                    gManager.eventListener.scheduleLeave();
                }

                Sentry.captureMessage("Failed to load track: " + exception.getMessage() + "\nIdentifier: " + trackIdentifier);
            }
        });
    }

    public static void autoComplete(String identifier, CommandAutoCompleteInteraction interaction) {
        identifier = "ytsearch:" + identifier;
        audioPlayerManager.loadItemOrdered(interaction, identifier, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {

            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                List<String> tracks = new ArrayList<>();

                for (int i = 0; i < 5; i++) {
                    tracks.add(playlist.getTracks().get(i).getInfo().title);
                }

                interaction.replyChoiceStrings(tracks).queue();
            }

            @Override
            public void noMatches() {

            }

            @Override
            public void loadFailed(FriendlyException exception) {

            }
        });
    }

    public static void onLeave(Guild guild) {
        MusicManager manager = of(guild);

        manager.eventListener.setRepeatOff();
        manager.eventListener.queue.clear();
        manager.eventListener.nextTrack();

        manager.eventListener.cancelLeave();
        manager.eventListener.deleteMessage();
        manager.eventListener.setJda(null);

        manager.audioPlayer.destroy();
        guildManagerMap.remove(guild.getIdLong(), manager);
    }

    public static void timerLeave(Guild guild) {
        MusicManager manager = of(guild);
        manager.eventListener.scheduleLeave(5, TimeUnit.MINUTES);
    }

    public static boolean isLeaving(Guild guild) {
        return of(guild).eventListener.isLeaving();
    }

    public static void cancelLeave(Guild guild) {
        of(guild).eventListener.cancelLeave();
    }

    public static int getActivePlayers() {
        return guildManagerMap.size();
    }

    /**
     * Wrapper for audio player
     *
     * @return Returns a wrapped *JDA* AudioSendHandler with a LavaPlayer audio player
     */
    public AudioPlayerSendHandler getSendHandler() {
        return new AudioPlayerSendHandler(audioPlayer);
    }

    public boolean isPlaying() {
        return audioPlayer.getPlayingTrack() != null;
    }

}
