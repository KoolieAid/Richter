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
    private BotConfigManager() {}

    public static void loadJSON() throws FileNotFoundException {
        JsonObject jsonObject = JsonParser.parseReader(new FileReader(System.getProperty("user.dir") + "/config.json")).getAsJsonObject();
        token = jsonObject.get("discord_token").getAsString();
        spotifyClientId = jsonObject.get("spotify_client_id").getAsString();
        spotifyClientSecret = jsonObject.get("spotify_client_secret").getAsString();
        sentryDsn = jsonObject.get("sentry_dsn").getAsString();
        prefix = jsonObject.get("default_prefix").getAsString();
        musixmatchApiKey = jsonObject.get("musixmatch_api_key").getAsString();
    }

}
