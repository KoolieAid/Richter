package com.koolie.bot.richter.commands.music;

import com.koolie.bot.richter.MusicUtil.GMManager;
import com.koolie.bot.richter.MusicUtil.MusicManagerFactory;
import com.koolie.bot.richter.commands.Command;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class Repeat extends Command {
    public Repeat() {
        setName("Repeat");
        setDescription("Repeats the song/queue. Available arguments: `single`, `queue`");
        setCommandType(commandType.Music);
    }

    @Override
    public void execute(MessageReceivedEvent event) {
        String[] args = event.getMessage().getContentRaw().split(" ", 2);
        if (args.length < 2) {
            event.getMessage().reply("Please provide arguments: Available arguments: `single`, `queue`, `off`").queue();
            return;
        }
        GMManager manager = MusicManagerFactory.getGuildMusicManager(event.getGuild());
        switch (args[1]) {
            case "single" -> {
                manager.eventListener.setRepeatSingle();
                event.getMessage().reply("The track will now repeat").queue();
            }
            case "queue" -> {
                manager.eventListener.setRepeatQueue();
                event.getMessage().reply("The queue will now repeat").queue();
            }
            case "off" -> {
                manager.eventListener.setRepeatOff();
                event.getMessage().reply("Repeat has been turned off").queue();
            }
            default -> {
                event.getMessage().reply("bro. what does " + args[1] + " even mean???").queue();
            }
        }
    }
}
