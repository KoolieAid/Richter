package com.koolie.bot.richter.commands.music;

import com.koolie.bot.richter.MusicUtil.GMManager;
import com.koolie.bot.richter.MusicUtil.MusicManagerFactory;
import com.koolie.bot.richter.commands.Command;
import com.koolie.bot.richter.threading.ThreadUtil;
import com.koolie.bot.richter.util.BotConfigManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import io.sentry.Sentry;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jmusixmatch.MusixMatch;
import org.jmusixmatch.MusixMatchException;
import org.jmusixmatch.entity.lyrics.Lyrics;
import org.jmusixmatch.entity.track.Track;
import org.jmusixmatch.entity.track.TrackData;

import java.awt.*;
import java.util.concurrent.CompletableFuture;

public class LyricsCommand extends Command {
    private final MusixMatch client;

    public LyricsCommand() {
        setName("Lyrics");
        setDescription("Gets the lyrics of the current playing song");
        setCommandType(commandType.Music);

        client = new MusixMatch(BotConfigManager.getMusixmatchApiKey());
    }

    @Override
    public void execute(MessageReceivedEvent event) {
        GMManager manager = MusicManagerFactory.getGuildMusicManager(event.getGuild());
        AudioTrack track = manager.audioPlayer.getPlayingTrack();
        event.getChannel().sendTyping().queue();
        String[] args = event.getMessage().getContentRaw().split(" ", 2);

        if (track == null && args.length < 2) {
            event.getMessage().reply("There is no music playing").queue();
            return;
        }

        String query;
        if (args.length > 1) {
            query = args[1];
        } else {
            query = track.getInfo().title.replaceAll("[^a-zA-Z0-9\\s]", "") + " " + track.getInfo().author.replaceAll("[^a-zA-Z0-9\\s]", "");
        }

        //event.getMessage().reply("Info:\nIdentifier: " + track.getIdentifier() + "\nAuthor: " + track.getInfo().author + "\nTitle: " + track.getInfo().title).queue();
        TrackData trackData;
        try {
            trackData = client.getMatchingTrack(track.getInfo().title, track.getInfo().author).getTrack();
        } catch (MusixMatchException e) {
            event.getMessage().replyEmbeds(new EmbedBuilder()
                    .setDescription("I could not find the track in the database.")
                    .build()).queue();
            return;
        }
        //event.getMessage().reply("Used: " + "TWO DOOR CINEMA CLUB | UNDERCOVER MARTYN".replaceAll("[^a-zA-Z0-9\\s]", "")).queue();

        if (trackData.getHasLyrics() == 0) {
            event.getMessage().replyEmbeds(new EmbedBuilder()
                    .setDescription("Looks like the track has no lyrics.")
                    .build()).queue();
            return;
        }

        Lyrics lyrics;
        try {
            lyrics = client.getLyrics(trackData.getTrackId());
        } catch (MusixMatchException e) {
            event.getMessage().replyEmbeds(new EmbedBuilder()
                    .setDescription("I had trouble finding the lyrics.")
                    .build()).queue();
            return;
        }

        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setColor(Color.RED);

        embedBuilder.setTitle(trackData.getTrackName());
        embedBuilder.appendDescription(trackData.getArtistName()).appendDescription("\n\n");
        embedBuilder.appendDescription(lyrics.getLyricsBody());
        embedBuilder.setFooter(lyrics.getLyricsCopyright());
        event.getMessage().replyEmbeds(embedBuilder.build()).queue();
    }
}
