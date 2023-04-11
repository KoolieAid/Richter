package com.koolie.bot.richter.commands.music;

import com.koolie.bot.richter.MusicUtil.MusicManager;
import com.koolie.bot.richter.commands.Interfaces.TextCommand;
import com.koolie.bot.richter.util.MusicUtil;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.Message;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Skip implements TextCommand {
    public Skip() {}

    @NotNull
    @Override
    public String getName() {
        return "Skip";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Skips the playing song";
    }

    @NotNull
    @Override
    public CommandType getCommandType() {
        return CommandType.Music;
    }

    @NotNull
    @Override
    public String getOperator() {
        return "skip";
    }

    @Nullable
    @Override
    public String[] getAliases() {
        return new String[]{"next", "n"};
    }

    @Override
    public void execute(@NotNull Message message) {
        if (!message.getGuild().getAudioManager().isConnected()) {
            message.reply("I'm not in a channel bro").queue();
            return;
        }

        if (!MusicUtil.isInSameChannel(message.getMember().getVoiceState().getChannel(), message.getMember(), message.getGuild().getSelfMember())) {
            message.reply("Hey! Have some manners. Other people are using me.\nYou have to join the same voice channel to be able to use this command").queue();
            return;
        }

        if (message.getContentRaw().split(" ").length > 1) {
            try {
                Integer.parseInt(message.getContentRaw().split(" ")[1]);
            } catch (NumberFormatException e) {
                message.reply("Seems like you're trying to skip a certain song. :thinking: How about trying `removequeue` or `rq` perhaps? \uD83E\uDDD0").queue();
                return;
            }
            message.reply("Seems like you're trying to skip a certain song. :thinking: How about trying `removequeue` or `rq` perhaps? \uD83E\uDDD0").queue();
            return;
        }

        MusicManager gManager = MusicManager.of(message.getGuild());
        AudioTrack skippedTrack = gManager.audioPlayer.getPlayingTrack();
        if (skippedTrack == null) {
            message.reply("There's nothing to skip").queue();
            return;
        }

        if (gManager.eventListener.getCurrentMode() == RepeatMode.Single) {
            gManager.audioPlayer.startTrack(null, false);
        } else {
            gManager.eventListener.nextTrack();
        }

        message.addReaction(Emoji.fromUnicode("\uD83D\uDC4C")).queue();
    }
}
