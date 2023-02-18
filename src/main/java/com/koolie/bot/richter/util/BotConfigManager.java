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

    private @Getter static String PAPISID;
    private @Getter static String PSID;

    private @Getter static String openAIKey;
    private BotConfigManager() {}

    public static void loadJSON() throws FileNotFoundException {
        JsonObject jsonObject = JsonParser.parseReader(new FileReader(System.getProperty("user.dir") + "/config.json")).getAsJsonObject();
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

        PAPISID = jsonObject.get("youtube_PAPISid").getAsString();
        PSID = jsonObject.get("youtube_PSid").getAsString();

        openAIKey = jsonObject.get("open_ai_key").getAsString();
    }

}
