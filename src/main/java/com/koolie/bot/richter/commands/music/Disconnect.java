package com.koolie.bot.richter.commands.music;

import com.koolie.bot.richter.commands.Command;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class Disconnect extends Command {
    public Disconnect() {
        setName("Disconnect");
        setDescription("Disconnects from the voice channel");
        setCommandType(commandType.Music);
    }

    @Override
    public void execute(MessageReceivedEvent event) {

        if (!event.getGuild().getAudioManager().isConnected()) {
            event.getMessage().reply("Seems like I already disconnected").queue();
            return;
        }

        event.getGuild().getAudioManager().closeAudioConnection();
        event.getMessage().reply("Disconnected from channel").queue();
    }
}
