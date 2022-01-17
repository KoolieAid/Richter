package com.koolie.bot.richter.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.FileReader;

public class BotConfigManager {
    private BotConfigManager() {}
    private static String token;
    private static String spotifyClientId;
    private static String spotifyClientSecret;
    private static String sentryDsn;
    private static String prefix;

    public static void loadJSON() throws FileNotFoundException {
        JsonElement element = JsonParser.parseReader(new FileReader(System.getProperty("user.dir") + "/config.json"));
        token = element.getAsJsonObject().get("discord_token").getAsString();
        spotifyClientId = element.getAsJsonObject().get("spotify_client_id").getAsString();
        spotifyClientSecret = element.getAsJsonObject().get("spotify_client_secret").getAsString();
        sentryDsn = element.getAsJsonObject().get("sentry_dsn").getAsString();
        prefix = element.getAsJsonObject().get("default_prefix").getAsString();
    }

    public static String getToken() {
        return token;
    }

    public static String getSpotifyClientId() {
        return spotifyClientId;
    }

    public static String getSpotifyClientSecret() {
        return spotifyClientSecret;
    }

    public static String getSentryDsn() {
        return sentryDsn;
    }

    public static String getPrefix() {
        return prefix;
    }
}
