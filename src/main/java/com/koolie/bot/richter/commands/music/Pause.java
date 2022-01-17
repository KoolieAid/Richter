package com.koolie.bot.richter.commands.music;

import com.koolie.bot.richter.MusicUtil.GMManager;
import com.koolie.bot.richter.MusicUtil.MusicManagerFactory;
import com.koolie.bot.richter.commands.Command;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class Pause extends Command {
    public Pause() {
        setName("Pause");
        setDescription("Pauses the music duh");
        setCommandType(commandType.Music);
    }

    @Override
    public void execute(MessageReceivedEvent event) {
        if (!event.getGuild().getAudioManager().isConnected()) {
            event.getMessage().reply("I'm not in a channel bro").queue();
            return;
        }

        GMManager gManager = MusicManagerFactory.getGuildMusicManager(event.getGuild());
        AudioTrack track = gManager.audioPlayer.getPlayingTrack();
        if (track == null) {
            event.getMessage().reply("Nothing is playing").queue();
            return;
        }
        gManager.audioPlayer.setPaused(true);

        event.getMessage().reply(track.getInfo().title + " has been paused").queue();
    }
}
