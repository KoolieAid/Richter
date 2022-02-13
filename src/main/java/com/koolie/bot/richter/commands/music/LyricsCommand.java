package com.koolie.bot.richter.commands.music;

import com.koolie.bot.richter.MusicUtil.MusicManager;
import com.koolie.bot.richter.commands.TextCommand;
import com.koolie.bot.richter.objects.Ignored;
import com.koolie.bot.richter.util.BotConfigManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import org.jetbrains.annotations.NotNull;
import org.jmusixmatch.MusixMatch;
import org.jmusixmatch.MusixMatchException;
import org.jmusixmatch.entity.lyrics.Lyrics;
import org.jmusixmatch.entity.track.Track;
import org.jmusixmatch.entity.track.TrackData;

import java.awt.*;

@Ignored
public class LyricsCommand implements TextCommand {
    private final MusixMatch client;

    public LyricsCommand() {
        client = new MusixMatch(BotConfigManager.getMusixmatchApiKey());
    }

    @NotNull
    @Override
    public String getName() {
        return "Lyrics";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Gets the lyrics of the current playing song";
    }

    @NotNull
    @Override
    public CommandType getCommandType() {
        return CommandType.Music;
    }

    @NotNull
    @Override
    public String getOperator() {
        return "lyrics";
    }

    @Override
    public void execute(Message message) {
        MusicManager manager = MusicManager.of(message.getGuild());
        AudioTrack track = manager.audioPlayer.getPlayingTrack();
        message.getChannel().sendTyping().queue();
        String[] args = message.getContentRaw().split(" ", 2);

        if (track == null && args.length < 2) {
            message.reply("There is no music playing").queue();
            return;
        }

        String query;
        if (args.length > 1) {
            query = args[1];
            sendSearchedLyrics(message, query);
        } else {
            sendCurrentLyrics(message, track);
//            query = track.getInfo().title.replaceAll("[^a-zA-Z0-9\\s]", "") + " " + track.getInfo().author.replaceAll("[^a-zA-Z0-9\\s]", "");
        }


    }

    private void sendCurrentLyrics(Message message, AudioTrack track) {
        //message.getMessage().reply("Info:\nIdentifier: " + track.getIdentifier() + "\nAuthor: " + track.getInfo().author + "\nTitle: " + track.getInfo().title).queue();
        TrackData trackData;
        try {
            trackData = client.getMatchingTrack(track.getInfo().title, track.getInfo().author).getTrack();
        } catch (MusixMatchException e) {
            message.replyEmbeds(new EmbedBuilder()
                    .setDescription("I could not find the track in the database.")
                    .build()).queue();
            return;
        }
        //message.getMessage().reply("Used: " + "TWO DOOR CINEMA CLUB | UNDERCOVER MARTYN".replaceAll("[^a-zA-Z0-9\\s]", "")).queue();

        if (trackData.getHasLyrics() == 0) {
            message.replyEmbeds(new EmbedBuilder()
                    .setDescription("Looks like the track has no lyrics.")
                    .build()).queue();
            return;
        }

        Lyrics lyrics;
        try {
            lyrics = client.getLyrics(trackData.getTrackId());
        } catch (MusixMatchException e) {
            message.replyEmbeds(new EmbedBuilder()
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
        message.replyEmbeds(embedBuilder.build()).queue();
    }

    private void sendSearchedLyrics(Message message, String query) {
        Track track = null;
        try {
            track = client.searchTracks(query, "", "", 0, 1, true).size() != 0 ?
                    client.searchTracks(query, "", "", 0, 1, true).get(0) :
                    null;
        } catch (MusixMatchException e) {
            message.replyEmbeds(new EmbedBuilder()
                    .setColor(Color.RED)
                    .setDescription("I could not find the track in the database.")
                    .build()).queue();
            return;
        }

        if (track == null) {
            message.replyEmbeds(new EmbedBuilder()
                    .setColor(Color.RED)
                    .setDescription("I could not find the track in the database.")
                    .build()).queue();
            return;
        }

        if (track.getTrack().getHasLyrics() == 0) {
            message.replyEmbeds(new EmbedBuilder()
                    .setColor(Color.RED)
                    .setDescription("Looks like the track has no lyrics.")
                    .build()).queue();
            return;
        }

        Lyrics lyrics = null;
        try {
            lyrics = client.getLyrics(track.getTrack().getTrackId());
        } catch (MusixMatchException e) {
            message.replyEmbeds(new EmbedBuilder()
                    .setDescription("I had trouble finding the lyrics.")
                    .build()).queue();
        }

        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setColor(Color.RED)
                .setTitle(track.getTrack().getTrackName())
                .appendDescription(track.getTrack().getArtistName())
                .appendDescription("\n")
                .appendDescription(lyrics.getLyricsBody())
                .setFooter(lyrics.getLyricsCopyright());
        message.replyEmbeds(embedBuilder.build()).queue();
    }
}
