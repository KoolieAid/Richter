package com.koolie.bot.richter.commands.music.filters;

import com.github.natanbc.lavadsp.karaoke.KaraokePcmAudioFilter;
import com.koolie.bot.richter.MusicUtil.GMManager;
import com.koolie.bot.richter.MusicUtil.MusicManagerFactory;
import com.koolie.bot.richter.commands.Command;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Collections;

public class Karaoke extends Command {
    public Karaoke() {
        setName("Karaoke");
        setDescription("Filters out the mid frequencies of the song to get rid of the voices");
        setCommandType(commandType.Music);
    }

    @Override
    public void execute(MessageReceivedEvent event) {
        if (!event.getGuild().getAudioManager().isConnected()) {
            event.getMessage().reply("I'm not in a channel bro").queue();
            return;
        }
        GMManager manager = MusicManagerFactory.getGuildMusicManager(event.getGuild());

        String[] args = event.getMessage().getContentRaw().split(" ", 2);
        if (args.length == 1) {
            event.getMessage().reply("Missing arguments").queue();
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
                event.getMessage().reply("Karaoke enabled").queue();
            }
            case "disable" -> {
                manager.audioPlayer.setFilterFactory(null);
                event.getMessage().reply("Karaoke disabled").queue();
            }
            default -> {
                event.getMessage().reply(args[1] + " is not a valid argument. Available Arguments: `enable`, `disable`").queue();
            }
        }
    }
}
