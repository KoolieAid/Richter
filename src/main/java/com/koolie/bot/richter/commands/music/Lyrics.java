package com.koolie.bot.richter.commands.music;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.koolie.bot.richter.MusicUtil.MusicManager;
import com.koolie.bot.richter.commands.Interfaces.TextCommand;
import com.koolie.bot.richter.objects.Ignored;
import com.koolie.bot.richter.util.BotConfigManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import io.sentry.Sentry;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.IOException;

public class Lyrics implements TextCommand {
    private static final String apiKey = BotConfigManager.getRapidApiKey();
    private final OkHttpClient client;

    public Lyrics() {
        client = new OkHttpClient();
    }

    @NotNull
    @Override
    public String getName() {
        return "Lyrics";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Gets the lyrics of a song";
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

    @Nullable
    @Override
    public String[] getAliases() {
        return new String[]{"lyric", "ly"};
    }

    @Override
    public void execute(@NotNull Message message) {
        message.getChannel().sendTyping().queue();
        String[] args = message.getContentRaw().split(" ", 2);

        String query;
        if (args.length < 2) {
            if (!MusicManager.isPresent(message.getGuild())) {
                message.reply("There is no music playing").queue();
                return;
            }

            MusicManager manager = MusicManager.of(message.getGuild());
            AudioTrack track = manager.audioPlayer.getPlayingTrack();

            query = track.getInfo().title;
        } else {
            query = args[1];
        }

        Lyric lyric;
        try {
            lyric = getLyrics(getSongId(query));
        } catch (IOException e) {
            Sentry.captureException(e, args[1]);
            message.replyEmbeds(new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Something went wrong while getting the lyrics")
                    .setDescription("Error: `" + e.getMessage() + "`")
                    .build()).queue();
            return;
        } catch (IndexOutOfBoundsException e) {
            message.replyEmbeds(new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("No lyrics found for: " + query)
                    .build()).queue();
            return;
        }

        EmbedBuilder embedBuilder = new EmbedBuilder();

        embedBuilder.setTitle(lyric.getTitle())
                .setDescription(lyric.getArtist())
                .appendDescription("\n\n")
                .appendDescription(lyric.getBody())
                .setFooter("Lyrics provided by Genius with the help of RapidAPI. Client made by me.", null)
                .setColor(Color.RED);

        message.replyEmbeds(embedBuilder.build()).queue();

    }

    private Lyric getLyrics(int songId) throws IOException {
        Request request = new Request.Builder()
                .get()
                .url("https://genius-song-lyrics1.p.rapidapi.com/songs/" + songId + "/lyrics")
                .addHeader("x-rapidapi-host", "genius-song-lyrics1.p.rapidapi.com")
                .addHeader("x-rapidapi-key", apiKey)
                .build();

        Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            response.close();
            throw new IOException("Unexpected code: " + response.code());
        }

        JsonObject jsonObject = JsonParser.parseString(response.body().string()).getAsJsonObject();

        int statusCode = jsonObject.getAsJsonObject("meta").getAsJsonPrimitive("status").getAsInt();

        if (statusCode == 404) {
            response.close();
            throw new IndexOutOfBoundsException();
        } else if (statusCode != 200) {
            response.close();
            throw new IOException(statusCode + ": " + jsonObject.getAsJsonObject("meta").getAsJsonPrimitive("message").getAsString());
        }

        JsonObject lyricsJsonObject = jsonObject.getAsJsonObject("response").getAsJsonObject("lyrics");

        JsonObject trackingData = lyricsJsonObject.getAsJsonObject("trackingData");

        String artist = trackingData.getAsJsonPrimitive("Primary Artist").getAsString();
        String title = trackingData.getAsJsonPrimitive("Title").getAsString();

        int id = lyricsJsonObject.get("song_id").getAsInt();

        String lyricsBody = lyricsJsonObject.getAsJsonObject("lyrics").getAsJsonObject("body").get("plain").getAsString();

        response.close();
        return new Lyric(artist, title, lyricsBody, id);
    }

    private int getSongId(String songName) throws IOException, IndexOutOfBoundsException {
        songName = songName.replaceAll(" ", "%20");
        Request request = new Request.Builder()
                .get()
                .url("https://genius-song-lyrics1.p.rapidapi.com/search?q=" + songName + "&per_page=1")
                .addHeader("x-rapidapi-host", "genius-song-lyrics1.p.rapidapi.com")
                .addHeader("x-rapidapi-key", apiKey)
                .build();

        Response response = client.newCall(request).execute();
        JsonObject jsonObject = JsonParser.parseString(response.body().string()).getAsJsonObject();

        response.close();
        return jsonObject.getAsJsonObject("response")
                .getAsJsonArray("hits")
                .get(0).getAsJsonObject()
                .getAsJsonObject("result")
                .getAsJsonPrimitive("id")
                .getAsInt();

    }

    @Ignored
    public class Lyric {
        private final String artist;
        private final String title;
        private final String body;
        private final int id;

        public Lyric(String artist, String title, String body, int id) {
            this.artist = artist;
            this.title = title;
            this.body = body;
            this.id = id;
        }

        public String getArtist() {
            return artist;
        }

        public String getTitle() {
            return title;
        }

        public int getId() {
            return id;
        }

        public String getBody() {
            return body;
        }

    }
}
