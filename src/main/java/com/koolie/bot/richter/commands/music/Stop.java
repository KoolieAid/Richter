package com.koolie.bot.richter.commands.music;

import com.koolie.bot.richter.MusicUtil.GMManager;
import com.koolie.bot.richter.MusicUtil.MusicManagerFactory;
import com.koolie.bot.richter.commands.Command;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;

public class Stop extends Command {
    public Stop() {
        setName("Stop");
        setDescription("Clears the queue and stop the music");
        setCommandType(commandType.Music);
    }

    @Override
    public void execute(MessageReceivedEvent event) {
        if (!event.getGuild().getAudioManager().isConnected()) {
            event.getMessage().reply("Seems like I already disconnected").queue();
            return;
        }

        GMManager gManager = MusicManagerFactory.getGuildMusicManager(event.getGuild());
        if (gManager.eventListener.queue.size() != 0) {
            gManager.eventListener.queue.clear();
        }
        gManager.eventListener.setRepeatOff();
        gManager.eventListener.nextTrack();

        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Stopped Playback").setColor(Color.RED);
        event.getMessage().replyEmbeds(eb.build()).queue();
    }
}
