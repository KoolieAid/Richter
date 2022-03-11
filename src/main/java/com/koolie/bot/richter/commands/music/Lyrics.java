package com.koolie.bot.richter.commands.music;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.koolie.bot.richter.MusicUtil.MusicManager;
import com.koolie.bot.richter.commands.TextCommand;
import com.koolie.bot.richter.objects.Ignored;
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

//@Ignored
public class Lyrics implements TextCommand {
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
        return new String[] {"lyric", "ly"};
    }

    @Override
    public void execute(@NotNull Message message) {
        if (!MusicManager.isPresent(message.getGuild())) {
            message.reply("There is no music playing").queue();
            return;
        }

        MusicManager manager = MusicManager.of(message.getGuild());
        AudioTrack track = manager.audioPlayer.getPlayingTrack();
        message.getChannel().sendTyping().queue();
        String[] args = message.getContentRaw().split(" ", 2);

//        if (track == null && args.length < 2) {
//            message.reply("There is no music playing").queue();
//            return;
//        }

        String query;
        if (args.length > 1) {
            query = args[1];
        } else {
            query = track.getInfo().title;
        }

        Lyric lyric = null;
        try {
            lyric = getLyrics(getSongId(query));
        } catch (IOException e) {
            Sentry.captureException(e, args[1]);
            message.replyEmbeds(new EmbedBuilder()
                    .setColor(Color.RED)
                    .setTitle("Something went wrong while getting the lyrics")
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
                .addHeader("x-rapidapi-key", "094a1d91b7mshe28788dd3c1f8dep15b53ejsn0c2b63918b62")
                .build();

        Response response = null;
        JsonObject jsonObject = null;

        response = client.newCall(request).execute();
        jsonObject = JsonParser.parseString(response.body().string()).getAsJsonObject();

        String body = jsonObject.get("response")
                .getAsJsonObject()
                .getAsJsonObject("lyrics")
                .getAsJsonObject("lyrics")
                .getAsJsonObject("body")
                .getAsJsonPrimitive("plain")
                .getAsString();

        int id = jsonObject.get("response")
                .getAsJsonObject()
                .getAsJsonObject("lyrics")
                .getAsJsonPrimitive("song_id")
                .getAsInt();

        String artist = jsonObject.get("response")
                .getAsJsonObject()
                .getAsJsonObject("lyrics")
                .getAsJsonObject("trackingData")
                .getAsJsonPrimitive("Primary Artist")
                .getAsString();

        String title = jsonObject.get("response")
                .getAsJsonObject()
                .getAsJsonObject("lyrics")
                .getAsJsonObject("trackingData")
                .getAsJsonPrimitive("Title")
                .getAsString();

        return new Lyric(artist, title, body, id);
    }

    private int getSongId(String songName) throws IOException, IndexOutOfBoundsException{
        Request request = new Request.Builder()
                .get()
                .url("https://genius-song-lyrics1.p.rapidapi.com/search?q=" + songName + "&per_page=1")
                .addHeader("x-rapidapi-host", "genius-song-lyrics1.p.rapidapi.com")
                .addHeader("x-rapidapi-key", "094a1d91b7mshe28788dd3c1f8dep15b53ejsn0c2b63918b62")
                .build();

        Response response = null;
        JsonObject jsonObject = null;

        response = client.newCall(request).execute();
        jsonObject = JsonParser.parseString(response.body().string()).getAsJsonObject();

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
