package com.koolie.bot.richter.commands.music;

import com.koolie.bot.richter.MusicUtil.GMManager;
import com.koolie.bot.richter.MusicUtil.MusicManagerFactory;
import com.koolie.bot.richter.commands.Command;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;
import java.util.LinkedList;

public class Queue extends Command {
    public Queue() {
        setName("Queue");
        setDescription("Lists the queue");
        setCommandType(commandType.Music);
    }

    @Override
    public void execute(MessageReceivedEvent event) {
        GMManager gManager = MusicManagerFactory.getGuildMusicManager(event.getGuild());

        if (gManager.eventListener.queue.size() == 0) {
            event.getMessage().reply("There are no tracks queued").queue();
            return;
        }

        LinkedList<AudioTrack> queue = new LinkedList<>(gManager.eventListener.queue);
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setColor(Color.CYAN);
        embedBuilder.setTitle("Current queue for: " + event.getGuild().getName());

        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < 10; i++) {
            if (i == queue.size()) break;
            stringBuilder.append(i + 1 + ". " + queue.get(i).getInfo().title + "\n");
        }

        embedBuilder.setDescription(stringBuilder.toString());
        embedBuilder.setFooter("Showing first 10" + " out of " + queue.size());

        event.getMessage().replyEmbeds(embedBuilder.build()).queue();
    }
}
