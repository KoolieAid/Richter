package com.koolie.bot.richter.commands.music;

import com.koolie.bot.richter.MusicUtil.GMManager;
import com.koolie.bot.richter.MusicUtil.MusicManagerFactory;
import com.koolie.bot.richter.commands.Command;
import net.dv8tion.jda.api.entities.AudioChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;

import java.net.MalformedURLException;
import java.net.URL;

public class Play extends Command {
    public Play() {
        setName("Play");
        setDescription("Plays music");
        setCommandType(commandType.Music);
    }

    @Override
    public void execute(MessageReceivedEvent event) {
        AudioChannel vChannel = event.getMember().getVoiceState().getChannel();
        if (vChannel == null) {
            event.getMessage().reply("Bro. You are not in a voice channel").queue();
            return;
        }

        event.getChannel().sendTyping().queue();
        String[] args = event.getMessage().getContentRaw().split(" ", 2);
        if (args.length == 1) {
            event.getMessage().reply("Bruh wheres your query???").queue();
            return;
        }

        String query;
        query = args[1];

        try {
            URL url = new URL(query);
            if (!url.getHost().equalsIgnoreCase("open.spotify.com")) throw new MalformedURLException();

        } catch (MalformedURLException e) {
            if (!query.matches("^((?:https?:)?\\/\\/)?((?:www|m)\\.)?((?:youtube\\.com|youtu.be))(\\/(?:[\\w\\-]+\\?v=|embed\\/|v\\/)?)([\\w\\-]+)(\\S+)?$")
                    && !query.startsWith("direct:")) {
                query = "ytsearch:" + query;
            }
        }

        if (query.startsWith("direct:")) {
            query = query.substring("direct:".length());
        }

        MusicManagerFactory.loadToGuild(event.getMessage(), query);

        GMManager gmManager = MusicManagerFactory.getGuildMusicManager(event.getGuild());
        if (gmManager.audioPlayer.isPaused()) {
            gmManager.audioPlayer.setPaused(false);
        }
        try {
            event.getGuild().getAudioManager().openAudioConnection(vChannel);
        } catch (InsufficientPermissionException e) {
            event.getMessage().reply("I can't seem to connect to that channel").queue();
            gmManager.eventListener.queue.clear();
            gmManager.eventListener.nextTrack();
        }
        event.getGuild().getAudioManager().setSelfDeafened(true);
    }

}
