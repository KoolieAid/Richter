package com.koolie.bot.richter.SourceManagers;

import com.koolie.bot.richter.objects.spotify.SpotifyPlaylist;
import com.koolie.bot.richter.objects.spotify.SpotifyTrack;
import com.koolie.bot.richter.util.BotConfigManager;
import com.neovisionaries.i18n.CountryCode;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.track.*;
import io.sentry.ITransaction;
import io.sentry.Sentry;
import org.apache.hc.core5.http.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.ClientCredentials;
import se.michaelthelin.spotify.model_objects.specification.*;
import se.michaelthelin.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import se.michaelthelin.spotify.requests.data.albums.GetAlbumRequest;
import se.michaelthelin.spotify.requests.data.albums.GetAlbumsTracksRequest;
import se.michaelthelin.spotify.requests.data.artists.GetArtistsTopTracksRequest;
import se.michaelthelin.spotify.requests.data.playlists.GetPlaylistRequest;
import se.michaelthelin.spotify.requests.data.playlists.GetPlaylistsItemsRequest;
import se.michaelthelin.spotify.requests.data.tracks.GetTrackRequest;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Spotify Source Manager for handling spotify links
 *
 * @author Erik Go
 */
public class SpotifySourceManager implements AudioSourceManager {
    //Needs to be static, so I can access it in track scheduler.
    //track scheduler is unique to every server, so I need it static to not waste memory
    //Not used in this class, used only in track schedulers
    public static final YoutubeAudioSourceManager ytSourceManager = new YoutubeAudioSourceManager();
    private static final Logger log = LoggerFactory.getLogger(SpotifySourceManager.class);
    private final SpotifyApi api;
    private final Pattern playlistPattern = Pattern.compile("^https?:\\/\\/(?:open|play)\\.spotify\\.com\\/playlist\\/([\\w\\d]+)(\\?si=(.+))?$", Pattern.CASE_INSENSITIVE);
    private final Pattern trackPattern = Pattern.compile("^https?:\\/\\/(?:open|play)\\.spotify\\.com\\/track\\/([\\w\\d]+)(\\?si=(.+))?$", Pattern.CASE_INSENSITIVE);
    private final Pattern albumPattern = Pattern.compile("^https?:\\/\\/(?:open|play)\\.spotify\\.com\\/album\\/([\\w\\d]+)(\\?si=(.+))?$", Pattern.CASE_INSENSITIVE);
    private final Pattern artistPattern = Pattern.compile("^https?:\\/\\/(?:open|play)\\.spotify\\.com\\/artist\\/([\\w\\d]+)(\\?si=(.+))?$", Pattern.CASE_INSENSITIVE);
    private ClientCredentials credentials;
    private long timeSinceLastRefresh;

    /**
     * Spotify Source Manager constructor
     * uses client id, and client secret obtained from Spotify Dev Dashboard
     */
    public SpotifySourceManager() {
        String clientID = BotConfigManager.getSpotifyClientId();
        String clientSecret = BotConfigManager.getSpotifyClientSecret();
        api = new SpotifyApi.Builder()
                .setClientId(clientID)
                .setClientSecret(clientSecret)
                .build();

        timeSinceLastRefresh = System.currentTimeMillis();
    }

    public static AudioTrack convertToYoutube(SpotifyTrack track) {
        AudioPlaylist searchList = (AudioPlaylist) ytSourceManager.loadItem(null, new AudioReference("ytmsearch:" + track.getInfo().title + " " + track.getInfo().author, null));

        return searchList.getTracks().get(0);
    }

    @Override
    public AudioItem loadItem(AudioPlayerManager manager, AudioReference reference) {
        if (reference.identifier.startsWith("ytsearch:") || reference.identifier.startsWith("scsearch:")) return null;

        try {
            URL url = new URL(reference.identifier);
            if (!url.getHost().equals("open.spotify.com")) return null;
        } catch (MalformedURLException e) {
            return null;
        }

        Matcher matcher;
        matcher = playlistPattern.matcher(reference.identifier);
        if (matcher.find()) return getSpotifyPlaylist(matcher.group(1));
        matcher = albumPattern.matcher(reference.identifier);
        if (matcher.find()) return getSpotifyAlbum(matcher.group(1));
        matcher = trackPattern.matcher(reference.identifier);
        if (matcher.find()) return getSpotifyTrack(matcher.group(1));
        matcher = artistPattern.matcher(reference.identifier);
        if (matcher.find()) return getArtist(matcher.group(1));

        return null;
    }

    private SpotifyPlaylist getSpotifyPlaylist(String playlistId) {
        //Very important to refresh the access token
        refreshAccess();
        SpotifyPlaylist playlist = new SpotifyPlaylist();

        //Playlist Object - to get name
        GetPlaylistRequest playlistRequest = api
                .getPlaylist(playlistId)
                .build();

        //Playlist Items - to get items (weird that the wrapper separated this)
        GetPlaylistsItemsRequest playlistItemsRequest = api
                .getPlaylistsItems(playlistId)
                .build();

        //Requests in the Spotify API
        Paging<PlaylistTrack> page;

        CompletableFuture<Playlist> playlistObject = playlistRequest.executeAsync();

        //New algorithm that does every request async

        //First page, required to be blocking, so I can get total items
        try {
            page = playlistItemsRequest.execute();
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            return null;
        }
        //Immediately put the first page into the playlist
        for (PlaylistTrack track : page.getItems()) {
            if (track.getTrack() == null) continue;
            String trackName = track.getTrack().getName();
            String firstArtistName = ((Track) track.getTrack()).getArtists()[0].getName();

            playlist.addTrack(new SpotifyTrack(trackName, firstArtistName));
        }

        int total = page.getTotal();
        int limit = page.getLimit();
        List<CompletableFuture<Paging<PlaylistTrack>>> completableFutures = new LinkedList<>();

        ITransaction transaction = Sentry.startTransaction("For loop of spotify async", "playlistItemsRequest.executeAsync()");
        int pages = (total % limit + total) / limit; // modulo and / is very intensive, so i want to limit it in just 1 line
        for (int i = 1; i <= pages; i++) {
            playlistItemsRequest = api.getPlaylistsItems(playlistId)
                    .offset(i * limit)
                    .build();
            completableFutures.add(playlistItemsRequest.executeAsync());
        }
        transaction.finish();
        transaction = Sentry.startTransaction("Spotify Future joining", "future.join()");
        for (CompletableFuture<Paging<PlaylistTrack>> future : completableFutures) {
            page = future.join();
            for (PlaylistTrack track : page.getItems()) {
                if (track.getTrack() == null) continue;
                String trackName = track.getTrack().getName();
                String firstArtistName = ((Track) track.getTrack()).getArtists()[0].getName();

                playlist.addTrack(new SpotifyTrack(trackName, firstArtistName));
            }
        }

        transaction.finish();
        playlist.name = playlistObject.join().getName();
        log.debug("Spotify Playlist Returned");
        return playlist;
    }

    /**
     * Refreshes the access token of the api.
     * Since spotify expires the access token in 1 hour
     */
    private void refreshAccess() {
        //Checks if it expired or not, better this way instead of using a thread
        //3600000ms == 1 hour
//        if (System.currentTimeMillis() - timeSinceLastRefresh > 3600000) return;

        ClientCredentialsRequest clientCredentialsRequest = api.clientCredentials().build();

        try {
            credentials = clientCredentialsRequest.execute();
        } catch (Exception e) {
            log.error("Credential Request Rejected", e);
            Sentry.captureException(e, "Credential Request Rejected");
        }

        api.setAccessToken(credentials.getAccessToken());
        timeSinceLastRefresh = System.currentTimeMillis();

    }

    private SpotifyTrack getSpotifyTrack(String trackId) {
        refreshAccess();
        GetTrackRequest trackRequest = api.getTrack(trackId).build();
        Track track;
        try {
            track = trackRequest.execute();
        } catch (IOException | ParseException | SpotifyWebApiException e) {
            return null;
        }

        return new SpotifyTrack(track.getName(), track.getArtists()[0].getName());
    }

    private SpotifyPlaylist getSpotifyAlbum(String albumId) {
        refreshAccess();
        GetAlbumRequest albumRequest = api.getAlbum(albumId).build();
        CompletableFuture<Album> albumObject = albumRequest.executeAsync();

        SpotifyPlaylist playlist = new SpotifyPlaylist();

        GetAlbumsTracksRequest albumTracksRequest = api.getAlbumsTracks(albumId).build();
        Paging<TrackSimplified> tracks;
        try {
            tracks = albumTracksRequest.execute();
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            return null;
        }

        for (TrackSimplified track : tracks.getItems()) {
            if (track == null) continue;
            playlist.addTrack(new SpotifyTrack(track.getName(), track.getArtists()[0].getName()));
        }

        int total = tracks.getTotal();
        int limit = tracks.getLimit();
        List<CompletableFuture<Paging<TrackSimplified>>> completableFutures = new LinkedList<>();
        int pages = (total % limit + total) / limit;

        for (int i = 1; i <= pages; i++) {
            albumTracksRequest = api.getAlbumsTracks(albumId)
                    .offset(i * limit)
                    .build();
            completableFutures.add(albumTracksRequest.executeAsync());
        }

        for (CompletableFuture<Paging<TrackSimplified>> future : completableFutures) {
            tracks = future.join();
            for (TrackSimplified track : tracks.getItems()) {
                if (track == null) continue;
                playlist.addTrack(new SpotifyTrack(track.getName(), track.getArtists()[0].getName()));
            }
        }

        playlist.name = albumObject.join().getName();
        return playlist;
    }

    private SpotifyPlaylist getArtist(String artistId) {
        refreshAccess();
        CompletableFuture<Artist> artistFuture = api.getArtist(artistId).build().executeAsync();
        GetArtistsTopTracksRequest artistTopTracksRequest = api.getArtistsTopTracks(artistId, CountryCode.PH).build();

        SpotifyPlaylist playlist = new SpotifyPlaylist();

        Track[] tracks;
        try {
            tracks = artistTopTracksRequest.execute();
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            return null;
        }

        for (Track track : tracks) {
            playlist.addTrack(new SpotifyTrack(track.getName(), track.getArtists()[0].getName()));
        }

        playlist.name = artistFuture.join().getName();
        return playlist;

    }

    @Override
    public String getSourceName() {
        return "SpotifySourceManager";
    }

    @Override
    public boolean isTrackEncodable(AudioTrack track) {
        return false;
    }

    @Override
    public void encodeTrack(AudioTrack track, DataOutput output) {

    }

    @Override
    public AudioTrack decodeTrack(AudioTrackInfo trackInfo, DataInput input) {
        return null;
    }

    @Override
    public void shutdown() {

    }

}
