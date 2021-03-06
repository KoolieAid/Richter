package com.koolie.bot.richter.commands.music.filters;

import com.github.natanbc.lavadsp.rotation.RotationPcmAudioFilter;
import com.koolie.bot.richter.MusicUtil.MusicManager;
import com.koolie.bot.richter.commands.Interfaces.TextCommand;
import com.koolie.bot.richter.util.MusicUtil;
import net.dv8tion.jda.api.entities.Message;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;

public class Rotate implements TextCommand {
    public Rotate() {}

    @NotNull
    @Override
    public String getName() {
        return "8d";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Makes the music rotate on your head. Usage: `8d enable` or `8d disable`";
    }

    @NotNull
    @Override
    public CommandType getCommandType() {
        return CommandType.Music;
    }

    @NotNull
    @Override
    public String getOperator() {
        return "8d";
    }

    @Nullable
    @Override
    public String[] getAliases() {
        return new String[]{"rotate"};
    }

    @Override
    public void execute(@NotNull Message message) {
        if (!MusicManager.isPresent(message.getGuild())) {
            message.reply("I'm not in a channel bro").queue();
            return;
        }

        if (!MusicUtil.isInSameChannel(message.getMember().getVoiceState().getChannel(), message.getMember(), message.getGuild().getSelfMember())) {
            message.reply("Hey! Have some manners. Other people are using me.\nYou have to join the same voice channel to be able to use this command").queue();
            return;
        }

        MusicManager manager = MusicManager.of(message.getGuild());

        String[] args = message.getContentRaw().split(" ", 2);
        if (args.length == 1) {
            message.reply("Missing arguments. Available arguments: enable/disable").queue();
            return;
        }


        switch (args[1]) {
            case "enable" -> {
                manager.audioPlayer.setFilterFactory(((track, format, output) -> {
                    RotationPcmAudioFilter rotate = new RotationPcmAudioFilter(output, format.sampleRate);
                    rotate.setRotationSpeed(0.05f);
                    return Collections.singletonList(rotate);
                }));
                message.reply("8d enabled").queue();
            }
            case "disable" -> {
                manager.audioPlayer.setFilterFactory(null);
                message.reply("8d disabled").queue();
            }
            default -> {
                message.reply(args[1] + " is not a valid argument. Available Arguments: `enable`, `disable`").queue();
            }
        }
    }
}
