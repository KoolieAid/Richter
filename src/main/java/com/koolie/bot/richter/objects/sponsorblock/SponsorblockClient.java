package com.koolie.bot.richter.objects.sponsorblock;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.koolie.bot.richter.objects.guild.GuildConfig;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
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

public class SponsorblockClient {
    public static final OkHttpClient client = new OkHttpClient();
    private static final Logger logger = LoggerFactory.getLogger(SponsorblockClient.class);

    private static final HashMap<String, List<Segment>> segmentCache = new HashMap<>();

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

            // Unused code because I found a better way to fix the issue
//            // Merge segments that are colliding with each other
//            for (int i = 0; i < segments.size(); i++) {
//                for (int j = i + 1; j < segments.size(); j++) {
//                    if (Math.max(segments.get(i).getStart(), segments.get(j).getStart()) <
//                            Math.min(segments.get(i).getEnd(), segments.get(j).getEnd())) {
//
//                        // Merge the two segments
//                        segments.get(i).setStart(Math.min(segments.get(i).getStart(), segments.get(j).getStart()));
//                        segments.get(i).setEnd(Math.max(segments.get(i).getEnd(), segments.get(j).getEnd()));
//
//                        // Remove the second segment
//                        segments.remove(j);
//                    }
//                }
//            }

            response.body().close();
            return segments;
        } catch (IOException e) {
            logger.error("Error retrieving segments", e);
            Sentry.captureException(e);
            return Collections.emptyList();
        }

    }
}
