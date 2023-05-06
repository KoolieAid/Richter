package com.koolie.bot.richter.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Getter;

import java.io.FileNotFoundException;
import java.io.FileReader;

public class BotConfigManager {
    private @Getter static String token;
    private @Getter static String spotifyClientId;
    private @Getter static String spotifyClientSecret;
    private @Getter static String sentryDsn;
    private @Getter static String prefix;
    private @Getter static String musixmatchApiKey;
    private @Getter static String DB_URL;
    private @Getter static String DB_USER;
    private @Getter static String DB_PASS;
    private @Getter static String rapidApiKey;

//    private @Getter static String PAPISID;
//    private @Getter static String PSID;

    private @Getter static String youtubeEmail;
    private @Getter static String youtubePassword;

    private static JsonObject config;
    private BotConfigManager() {}

    public static void loadJSON() throws FileNotFoundException {
        JsonObject jsonObject = JsonParser.parseReader(new FileReader(System.getProperty("user.dir") + "/config.json")).getAsJsonObject();
        config = jsonObject;
        token = jsonObject.get("discord_token").getAsString();
        spotifyClientId = jsonObject.get("spotify_client_id").getAsString();
        spotifyClientSecret = jsonObject.get("spotify_client_secret").getAsString();
        sentryDsn = jsonObject.get("sentry_dsn").getAsString();
        prefix = jsonObject.get("default_prefix").getAsString();
        musixmatchApiKey = jsonObject.get("musixmatch_api_key").getAsString();
        DB_URL = jsonObject.get("database_address").getAsString();
        DB_USER = jsonObject.get("database_user").getAsString();
        DB_PASS = jsonObject.get("database_password").getAsString();
        rapidApiKey = jsonObject.get("rapid_api_key").getAsString();

//        PAPISID = jsonObject.get("youtube_PAPISid").getAsString();
//        PSID = jsonObject.get("youtube_PSid").getAsString();

        youtubeEmail = jsonObject.get("youtube_email").getAsString();
        youtubePassword = jsonObject.get("youtube_password").getAsString();
    }

    public static String get(String value) {
        return config.get(value).getAsString();
    }

    public static class Constants {
        public static String discordToken = "discord_token";
        public static String spotifyClientId = "spotify_client_id";
        public static String spotifyClientSecret = "spotify_client_secret";
        public static String sentryDsn = "sentry_dsn";
        public static String defaultPrefix = "default_prefix";
        public static String musixmatchApi = "musixmatch_api_key";
        public static String dbAddress = "database_address";
        public static String dbPassword = "database_password";
        public static String rapidApiKey = "rapid_api_key";
        public static String youtubeEmail = "youtube_email";
        public static String youtubePassword = "youtube_password";
    }

}
