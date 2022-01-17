package com.koolie.bot.richter.commands.music;

import com.jagrosh.jlyrics.Lyrics;
import com.jagrosh.jlyrics.LyricsClient;
import com.koolie.bot.richter.MusicUtil.GMManager;
import com.koolie.bot.richter.MusicUtil.MusicManagerFactory;
import com.koolie.bot.richter.commands.Command;
import com.koolie.bot.richter.threading.ThreadUtil;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import io.sentry.Sentry;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;
import java.util.concurrent.CompletableFuture;

public class LyricsCommand extends Command {
    private final LyricsClient lyricsClient;

    public LyricsCommand() {
        setName("Lyrics");
        setDescription("Gets the lyrics of the current playing song");
        setCommandType(commandType.Music);

        lyricsClient = new LyricsClient("Genius", ThreadUtil.getThreadExecutor());
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
        CompletableFuture<Lyrics> lyricsFuture = lyricsClient.getLyrics(query);
        //event.getMessage().reply("Used: " + "TWO DOOR CINEMA CLUB | UNDERCOVER MARTYN".replaceAll("[^a-zA-Z0-9\\s]", "")).queue();

        Lyrics lyrics;
        try {
            lyrics = lyricsFuture.join();
        } catch (Exception e) {
            Sentry.captureException(e, "Lyrics join");
            event.getMessage().reply("Could not find lyrics for the song").queue();
            return;
        }

        if (lyrics == null) {
            event.getMessage().reply("Could not find lyrics for `" + query + "`").queue();
            return;
        }

        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setColor(Color.RED);

        assert lyrics != null;
        embedBuilder.setTitle(lyrics.getTitle());
        embedBuilder.appendDescription(lyrics.getAuthor()).appendDescription("\n\n");
        embedBuilder.appendDescription(lyrics.getContent());
        embedBuilder.setFooter("Lyrics provided by: " + lyrics.getSource());
        event.getMessage().replyEmbeds(embedBuilder.build()).queue();
    }
}
