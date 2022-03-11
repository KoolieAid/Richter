package com.koolie.bot.richter.commands.music;

import com.koolie.bot.richter.MusicUtil.MusicManager;
import com.koolie.bot.richter.commands.Interfaces.TextCommand;
import com.koolie.bot.richter.util.MusicUtil;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Message;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.Random;

public class Shuffle implements TextCommand {
    public Shuffle() {
    }

    @NotNull
    @Override
    public String getName() {
        return "Shuffle";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Shuffles the queue";
    }

    @NotNull
    @Override
    public CommandType getCommandType() {
        return CommandType.Music;
    }

    @NotNull
    @Override
    public String getOperator() {
        return "shuffle";
    }

    @Override
    public void execute(Message message) {
        if (!message.getGuild().getAudioManager().isConnected()) {
            message.reply("I'm not in a channel bro").queue();
            return;
        }

        if (!MusicUtil.isInSameChannel(message.getMember().getVoiceState().getChannel(), message.getMember(), message.getGuild().getSelfMember())) {
            message.reply("You must be in the same voice channel as me to use this command.").queue();
            return;
        }

        MusicManager gManager = MusicManager.of(message.getGuild());
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

        message.reply("Shuffled the queue").queue();

    }
}
