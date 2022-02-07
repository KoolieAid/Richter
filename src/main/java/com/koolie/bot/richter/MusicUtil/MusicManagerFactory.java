package com.koolie.bot.richter.MusicUtil;

import com.koolie.bot.richter.SourceManagers.SpotifySourceManager;
import com.koolie.bot.richter.objects.Context;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import io.sentry.Sentry;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;

import java.awt.*;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * Handles the Guild Music Managers of guilds
 */
public class MusicManagerFactory {
    private static final AudioPlayerManager audioPlayerManager = new DefaultAudioPlayerManager();
    public static HashMap<Long, GMManager> guildManagerMap = new HashMap<>();

    private MusicManagerFactory() {}

    /**
     * @param guild Guild object to get AudioManager and ID
     * @return The Specific Music Manager for that guild
     */
    public static GMManager getGuildMusicManager(Guild guild) {
        GMManager musicManager = guildManagerMap.get(guild.getIdLong());

        if (musicManager == null) {
            musicManager = new GMManager(audioPlayerManager);
            musicManager.eventListener.setJda(guild.getJDA());
            guildManagerMap.put(guild.getIdLong(), musicManager);
        }

        // Sets the sending handler of the guild to a wrapped send handler from lava player
        guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());

        return musicManager;
    }

    public static void loadSources(){
        audioPlayerManager.registerSourceManager(new SpotifySourceManager());
        AudioSourceManagers.registerRemoteSources(audioPlayerManager);
        AudioSourceManagers.registerLocalSource(audioPlayerManager);

        audioPlayerManager.getConfiguration().setFilterHotSwapEnabled(true);
    }

    public static void loadToGuild(Context message, String trackIdentifier) {
        loadToGuild(message, trackIdentifier, false);
    }

    public static void loadToGuild(Context message, String trackIdentifier, boolean isFront){
        GMManager gManager = getGuildMusicManager(message.getGuild());

        audioPlayerManager.loadItemOrdered(gManager, trackIdentifier, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                gManager.eventListener.setChannel(message.getChannel().getIdLong());
//                message.reply(track.getInfo().title + " queued.").queue();
                EmbedBuilder eb = new EmbedBuilder();
                eb.setDescription("Queued: `" + track.getInfo().title + "`").setFooter(message.getAuthor().getName()).setColor(Color.CYAN);

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
//                    message.reply(track.getInfo().title + " added to queue").queue();

                    eb.setDescription("Queued: `" + track.getInfo().title + "`").setFooter(message.getAuthor().getName()).setColor(Color.CYAN);

                    message.replyEmbeds(eb.build()).queue();

                    if (isFront) {
                        gManager.eventListener.queueFront(track);
                        return;
                    }
                    gManager.eventListener.queue(track);
                    return;
                }

//                message.reply(playlist.getTracks().size() + " songs have been added from: " + playlist.getName()).queue();

                eb.setDescription(playlist.getTracks().size() + " songs have been added from: `"+ playlist.getName() + "`").setFooter(message.getAuthor().getName()).setColor(Color.CYAN);
                message.replyEmbeds(eb.build()).queue();

                for (AudioTrack track : playlist.getTracks()) {
                    gManager.eventListener.queue(track);
                }
            }

            @Override
            public void noMatches() {
                EmbedBuilder eb = new EmbedBuilder();
                eb.setDescription("No matches found for: `" + trackIdentifier + "`").setFooter(message.getAuthor().getName()).setColor(Color.RED);

                message.replyEmbeds(eb.build()).queue();
//                message.reply("No search results for: " + trackIdentifier).queue();
                if (gManager.eventListener.queue.size() == 0) {
                    gManager.eventListener.scheduleLeave();
                }

                Sentry.captureMessage("No search results for identifier: " + trackIdentifier);
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                EmbedBuilder eb = new EmbedBuilder();
                eb.setDescription("Could not play: `" + trackIdentifier + "`" + "\nReason: " + exception.getMessage()).setFooter(message.getAuthor().getName()).setColor(Color.RED);
                message.replyEmbeds(eb.build()).queue();

//                message.reply("I can't seem to load the query because of: " + exception.getMessage()).queue();
                if (gManager.eventListener.queue.size() == 0) {
                    gManager.eventListener.scheduleLeave();
                }

                Sentry.captureMessage("Failed to load track: " + exception.getMessage() + "\nIdentifier: " + trackIdentifier);
            }
        });
    }

    public static void onLeave(Guild guild) {
        GMManager manager = getGuildMusicManager(guild);

        manager.eventListener.setRepeatOff();
        manager.eventListener.queue.clear();
        manager.eventListener.nextTrack();

        manager.eventListener.cancelLeave();
        manager.eventListener.deleteMessage();
        manager.eventListener.setJda(null);

        manager.audioPlayer.destroy();
        guildManagerMap.remove(guild.getIdLong(), manager);
//        guildManagerMap.remove(guild.getIdLong());
    }

    public static void timerLeave(Guild guild) {
        GMManager manager = getGuildMusicManager(guild);
        manager.eventListener.scheduleLeave(5, TimeUnit.MINUTES);
    }

    public static boolean isLeaving(Guild guild) {
        return getGuildMusicManager(guild).eventListener.isLeaving();
    }

    public static void cancelLeave(Guild guild) {
        getGuildMusicManager(guild).eventListener.cancelLeave();
    }

    public static int getActivePlayers(){
        return guildManagerMap.size();
    }

}
