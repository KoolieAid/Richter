package com.koolie.bot.richter.commands.music;

import com.koolie.bot.richter.MusicUtil.GMManager;
import com.koolie.bot.richter.MusicUtil.MusicManagerFactory;
import com.koolie.bot.richter.commands.Command;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class RemoveQueue extends Command {
    public RemoveQueue() {
        setName("Remove Queue");
        setDescription("Removes a specific song in the queue");
        setCommandType(commandType.Music);
    }

    @Override
    public void execute(MessageReceivedEvent event) {
        GMManager manager = MusicManagerFactory.getGuildMusicManager(event.getGuild());

        String[] args = event.getMessage().getContentRaw().split(" ", 2);
        if (args.length < 2) {
            event.getMessage().reply("Please provide a number").queue();
            return;
        }
        int index;
        try {
            index = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            event.getMessage().reply("That's not a number lmao").queue();
            return;
        }
        index--;

        Queue<AudioTrack> queue = manager.eventListener.queue;

        if (index > queue.size()) {
            event.getMessage().reply("That number is more than the amount of songs in the queue.").queue();
            return;
        }
        if (index < 0) {
            event.getMessage().reply("Index should be more than 1.").queue();
            return;
        }

        List<AudioTrack> replacement = new LinkedList<>(queue);
        AudioTrack track = replacement.get(index);
        replacement.remove(index);
        manager.eventListener.queue = (Deque<AudioTrack>) replacement;
        event.getMessage().reply("`" + track.getInfo().title + "` removed").queue();
    }
}
