package com.koolie.bot.richter.commands.music.filters;

import com.github.natanbc.lavadsp.rotation.RotationPcmAudioFilter;
import com.koolie.bot.richter.MusicUtil.GMManager;
import com.koolie.bot.richter.MusicUtil.MusicManagerFactory;
import com.koolie.bot.richter.commands.Command;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Collections;

public class Rotate extends Command {
    public Rotate() {
        setName("8d");
        setDescription("Makes the music rotate on your head");
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
                    RotationPcmAudioFilter rotate = new RotationPcmAudioFilter(output, format.sampleRate);
                    rotate.setRotationSpeed(0.05f);
                    return Collections.singletonList(rotate);
                }));
                event.getMessage().reply("8d enabled").queue();
            }
            case "disable" -> {
                manager.audioPlayer.setFilterFactory(null);
                event.getMessage().reply("8d disabled").queue();
            }
            default -> {
                event.getMessage().reply(args[1] + " is not a valid argument. Available Arguments: `enable`, `disable`").queue();
            }
        }
    }
}
