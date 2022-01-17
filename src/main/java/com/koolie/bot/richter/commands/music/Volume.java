package com.koolie.bot.richter.commands.music;

import com.koolie.bot.richter.MusicUtil.GMManager;
import com.koolie.bot.richter.MusicUtil.MusicManagerFactory;
import com.koolie.bot.richter.commands.Command;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class Volume extends Command {
    public Volume() {
        setName("Volume");
        setDescription("Changes the volume of your player");
        setCommandType(commandType.Music);
    }

    @Override
    public void execute(MessageReceivedEvent event) {
        GMManager gManager = MusicManagerFactory.getGuildMusicManager(event.getGuild());

        String[] args = event.getMessage().getContentRaw().split(" ");

        if (args.length == 1) {
            event.getMessage().reply("Current Volume: " + gManager.audioPlayer.getVolume()).queue();
            return;
        }
        int newVol;
        try {
            newVol = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            event.getMessage().reply("That's not a number lmao").queue();
            return;
        }

        if (newVol > 75) newVol = 75;
        gManager.audioPlayer.setVolume(newVol);

        event.getMessage().reply("Player internal volume has been set to: **" + newVol + "**").queue();
    }
}
