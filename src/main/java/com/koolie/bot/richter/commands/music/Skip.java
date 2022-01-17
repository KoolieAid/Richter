package com.koolie.bot.richter.commands.music;

import com.koolie.bot.richter.MusicUtil.GMManager;
import com.koolie.bot.richter.MusicUtil.MusicManagerFactory;
import com.koolie.bot.richter.commands.Command;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class Skip extends Command {
    public Skip() {
        setName("Skip");
        setDescription("Skips the playing song");
        setCommandType(commandType.Music);
    }

    @Override
    public void execute(MessageReceivedEvent event) {
        if (!event.getGuild().getAudioManager().isConnected()) {
            event.getMessage().reply("I'm not in a channel bro").queue();
            return;
        }

        if (event.getMessage().getContentRaw().split(" ").length > 1) {
            try {
                Integer.parseInt(event.getMessage().getContentRaw().split(" ")[1]);
            } catch (NumberFormatException e) {
                event.getMessage().reply("Seems like you're trying to skip a certain song. :thinking: How about trying `removequeue` or `rq` perhaps? \uD83E\uDDD0").queue();
                return;
            }
            event.getMessage().reply("Seems like you're trying to skip a certain song. :thinking: How about trying `removequeue` or `rq` perhaps? \uD83E\uDDD0").queue();
            return;
        }

        GMManager gManager = MusicManagerFactory.getGuildMusicManager(event.getGuild());
        AudioTrack skippedTrack = gManager.audioPlayer.getPlayingTrack();
        if (skippedTrack == null) {
            event.getMessage().reply("There's nothing to skip").queue();
            return;
        }

        if (gManager.eventListener.getCurrentMode() == RepeatMode.Single) {
            gManager.audioPlayer.startTrack(null, false);
        } else {
            gManager.eventListener.nextTrack();
        }
        event.getMessage().addReaction("\uD83D\uDC4C").queue();
    }
}
