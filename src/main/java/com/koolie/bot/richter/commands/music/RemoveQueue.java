package com.koolie.bot.richter.commands.music;

import com.koolie.bot.richter.MusicUtil.MusicManager;
import com.koolie.bot.richter.commands.Interfaces.Command;
import com.koolie.bot.richter.commands.Interfaces.TextCommand;
import com.koolie.bot.richter.util.MusicUtil;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Message;
import org.jetbrains.annotations.NotNull;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class RemoveQueue implements TextCommand {
    public RemoveQueue() {
    }

    @NotNull
    @Override
    public String getName() {
        return "Remove Queue";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Removes a specific song in the queue";
    }

    @NotNull
    @Override
    public Command.CommandType getCommandType() {
        return CommandType.Music;
    }

    @Override
    public @NotNull String getOperator() {
        return "removequeue";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"rq", "remove", "discard", "rm"};
    }

    @Override
    public void execute(@NotNull Message message) {
        if (!MusicManager.isPresent(message.getGuild())) {
            message.getChannel().sendMessage("No music is currently playing").queue();
            return;
        }

        if (!MusicUtil.isInSameChannel(message.getMember().getVoiceState().getChannel(), message.getMember(), message.getGuild().getSelfMember())) {
            message.reply("You must be in the same voice channel as me to use this command.").queue();
            return;
        }

        MusicManager manager = MusicManager.of(message.getGuild());

        String[] args = message.getContentRaw().split(" ", 2);
        if (args.length < 2) {
            message.reply("Please provide a number").queue();
            return;
        }
        int index;
        try {
            index = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            message.reply("That's not a number lmao").queue();
            return;
        }
        index--;

        Queue<AudioTrack> queue = manager.eventListener.queue;

        if (index >= queue.size()) {
            message.reply("That number is more than the amount of songs in the queue.").queue();
            return;
        }
        if (index < 0) {
            message.reply("Index should be more than 1.").queue();
            return;
        }

        List<AudioTrack> replacement = new LinkedList<>(queue);
        AudioTrack track = replacement.get(index);
        replacement.remove(index);
        manager.eventListener.queue = (Deque<AudioTrack>) replacement;
        message.reply("`" + track.getInfo().title + "` removed").queue();
    }
}
