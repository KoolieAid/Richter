package com.koolie.bot.richter.commands.music;

import com.koolie.bot.richter.MusicUtil.GMManager;
import com.koolie.bot.richter.MusicUtil.MusicManagerFactory;
import com.koolie.bot.richter.commands.Command;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class Clear extends Command {
    public Clear() {
        setName("Clear");
        setDescription("Clears the queue, doesn't stop the currently playing track");
        setCommandType(commandType.Music);
    }

    @Override
    public void execute(MessageReceivedEvent event) {
        if (!event.getGuild().getAudioManager().isConnected()) {
            event.getMessage().reply("Seems like I already disconnected").queue();
            return;
        }

        GMManager gManager = MusicManagerFactory.getGuildMusicManager(event.getGuild());
        if (gManager.eventListener.getCurrentMode() == RepeatMode.Queue) {
            gManager.eventListener.setRepeatOff();
        }
        gManager.eventListener.queue.clear();

        event.getMessage().reply("Cleared the queue").queue();
    }
}
