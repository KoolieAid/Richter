package com.koolie.bot.richter.commands.music;

import com.koolie.bot.richter.MusicUtil.MusicManager;
import com.koolie.bot.richter.commands.TextCommand;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.time.Duration;
import java.util.LinkedList;

public class Queue implements TextCommand {
    public Queue() {}

    @NotNull
    @Override
    public String getName() {
        return "Queue";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Lists the current queue";
    }

    @NotNull
    @Override
    public CommandType getCommandType() {
        return CommandType.Music;
    }

    @NotNull
    @Override
    public String getOperator() {
        return "queue";
    }

    @Nullable
    @Override
    public String[] getAliases() {
        return new String[]{"q"};
    }

    @Override
    public void execute(Message message) {
        MusicManager gManager = MusicManager.of(message.getGuild());

        if (gManager.eventListener.queue.size() == 0) {
            message.reply("There are no tracks queued").queue();
            return;
        }

        LinkedList<AudioTrack> queue = new LinkedList<>(gManager.eventListener.queue);
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setColor(Color.CYAN);
        embedBuilder.setTitle("Current queue for: " + message.getGuild().getName());

        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < 10; i++) {
            if (i == queue.size()) break;

            Duration fullDuration = Duration.ofMillis(queue.get(i).getDuration());
            int fullHours = fullDuration.toHoursPart();
            int fullMinutes = fullDuration.toMinutesPart();
            int fullSeconds = fullDuration.toSecondsPart();

            String durationString;
            if (fullHours == 0) {
                durationString = String.format("%02d:%02d", fullMinutes, fullSeconds);
            } else {
                durationString = String.format("%02d:%02d:%02d", fullHours, fullMinutes, fullSeconds);
            }

            stringBuilder.append(i + 1 + ". " + queue.get(i).getInfo().title + " **[" + durationString + "]**" + "\n");
        }

        embedBuilder.setDescription(stringBuilder.toString());
        embedBuilder.setFooter("Showing first 10" + " out of " + queue.size());

        message.replyEmbeds(embedBuilder.build()).queue();
    }
}
