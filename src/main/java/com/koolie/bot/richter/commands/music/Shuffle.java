package com.koolie.bot.richter.commands.music;

import com.koolie.bot.richter.MusicUtil.GMManager;
import com.koolie.bot.richter.MusicUtil.MusicManagerFactory;
import com.koolie.bot.richter.commands.Command;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class Shuffle extends Command {
    public Shuffle() {
        setName("Shuffle");
        setDescription("Shuffles the queue");
        setCommandType(commandType.Music);
    }

    @Override
    public void execute(MessageReceivedEvent event) {
        if (!event.getGuild().getAudioManager().isConnected()) {
            event.getMessage().reply("I'm not in a channel bro").queue();
            return;
        }

        GMManager gManager = MusicManagerFactory.getGuildMusicManager(event.getGuild());
        Random random = new Random();

        LinkedList<AudioTrack> newQueue = new LinkedList<>();

        int currentSize = gManager.eventListener.queue.size();
        for (int i = 0; i < currentSize; i++) {
            AudioTrack temp = ((LinkedList<AudioTrack>) gManager.eventListener.queue).get(random.nextInt(gManager.eventListener.queue.size()));
            newQueue.add(temp);
            gManager.eventListener.queue.remove(temp);
        }
        gManager.eventListener.queue.clear();
        gManager.eventListener.queue = newQueue;

        event.getMessage().reply("Shuffled the queue").queue();

    }
}
