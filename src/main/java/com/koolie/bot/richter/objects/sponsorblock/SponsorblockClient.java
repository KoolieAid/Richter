package com.koolie.bot.richter.objects.sponsorblock;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.koolie.bot.richter.objects.guild.GuildConfig;
import com.koolie.bot.richter.threading.ThreadUtil;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.TrackMarker;
import io.sentry.Sentry;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SponsorblockClient {
    public static final OkHttpClient client = new OkHttpClient();
    private static final Logger logger = LoggerFactory.getLogger(SponsorblockClient.class);

    private static final HashMap<String, List<Segment>> segmentCache = new HashMap<>();

    public static void putSegmentsAsync(AudioTrack track, long guildId) {
        CompletableFuture.runAsync(() -> {
            List<Segment> segments = getSegments(track, guildId);
            if (segments.isEmpty()) return;
            track.setMarker(new TrackMarker(segments.get(0).getStart(), new SegmentHandler(track, segments)));
        }, ThreadUtil.getThreadExecutor());
    }

    public static List<Segment> getSegments(AudioTrack track, long guildId) {
        GuildConfig config = GuildConfig.of(guildId);
        if (!config.isSegmentSkippingEnabled()) return Collections.emptyList();

        if (segmentCache.containsKey(track.getIdentifier())) {
            return segmentCache.get(track.getIdentifier());
        }

        List<Segment> segments = getSegmentsHTTP(track);

        if (segments.isEmpty()) return segments;

        segmentCache.put(track.getIdentifier(), segments);

        return segments;
    }

    private static List<Segment> getSegmentsHTTP(AudioTrack track) {

        Request request = new Request.Builder()
                .get()
                .url("https://sponsor.ajay.app/api/skipSegments?videoID=" + track.getIdentifier() + "&categories=[\"sponsor\", \"selfpromo\", \"interaction\", \"intro\", \"outro\", \"preview\", \"music_offtopic\"]")
                .build();

        try {
            Response response = client.newCall(request).execute();
            if (!response.isSuccessful()) {
                response.close();
                return Collections.emptyList();
            }

            List<Segment> segments = new ArrayList<>();

            JsonArray segmentJsonObjects = JsonParser.parseString(response.body().string()).getAsJsonArray();

            for (JsonElement segmentObjectElement : segmentJsonObjects) {
                JsonObject segmentObject = segmentObjectElement.getAsJsonObject();

                JsonArray positionsInSeconds = segmentObject.get("segment").getAsJsonArray();

                long start = (long) (positionsInSeconds.get(0).getAsFloat() * 1000);
                long end = (long) (positionsInSeconds.get(1).getAsFloat() * 1000);

                segments.add(new Segment(start, end));
            }

            response.body().close();
            return segments;
        } catch (IOException e) {
            logger.error("Error retrieving segments", e);
            Sentry.captureException(e);
            return Collections.emptyList();
        }

    }
}
