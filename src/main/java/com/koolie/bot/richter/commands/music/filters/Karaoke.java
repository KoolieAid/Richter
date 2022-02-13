package com.koolie.bot.richter.commands.music.filters;

import com.github.natanbc.lavadsp.karaoke.KaraokePcmAudioFilter;
import com.koolie.bot.richter.MusicUtil.MusicManager;
import com.koolie.bot.richter.commands.TextCommand;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

public class Karaoke implements TextCommand {
    public Karaoke() {}

    @NotNull
    @Override
    public String getName() {
        return "Karaoke";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Filters out the mid frequencies of the song to get rid of the voices";
    }

    @NotNull
    @Override
    public CommandType getCommandType() {
        return CommandType.Music;
    }

    @NotNull
    @Override
    public String getOperator() {
        return "karaoke";
    }

    @Override
    public void execute(Message message) {
        if (!message.getGuild().getAudioManager().isConnected()) {
            message.reply("I'm not in a channel bro").queue();
            return;
        }
        MusicManager manager = MusicManager.of(message.getGuild());

        String[] args = message.getContentRaw().split(" ", 2);
        if (args.length == 1) {
            message.reply("Missing arguments").queue();
            return;
        }

        switch (args[1]) {
            case "enable" -> {
                manager.audioPlayer.setFilterFactory(((track, format, output) -> {
                    KaraokePcmAudioFilter karaokeFilter = new KaraokePcmAudioFilter(output, format.channelCount, format.sampleRate);
                    //set filter settings
                    karaokeFilter.setLevel(1f)
                            .setMonoLevel(1f)
                            .setFilterBand(220f)
                            .setFilterWidth(100f);
                    return Collections.singletonList(karaokeFilter);
                }));
                message.reply("Karaoke enabled").queue();
            }
            case "disable" -> {
                manager.audioPlayer.setFilterFactory(null);
                message.reply("Karaoke disabled").queue();
            }
            default -> {
                message.reply(args[1] + " is not a valid argument. Available Arguments: `enable`, `disable`").queue();
            }
        }
    }
}
