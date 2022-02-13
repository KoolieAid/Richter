package com.koolie.bot.richter.commands.music;

import com.koolie.bot.richter.MusicUtil.MusicManager;
import com.koolie.bot.richter.commands.TextCommand;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Volume implements TextCommand {
    public Volume() {}

    @NotNull
    @Override
    public String getName() {
        return "Volume";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Changes the volume of your player";
    }

    @NotNull
    @Override
    public CommandType getCommandType() {
        return CommandType.Music;
    }

    @NotNull
    @Override
    public String getOperator() {
        return "volume";
    }

    @Nullable
    @Override
    public String[] getAliases() {
        return new String[] {"vol"};
    }

    @Override
    public void execute(Message message) {
        MusicManager gManager = MusicManager.of(message.getGuild());

        String[] args = message.getContentRaw().split(" ");

        if (args.length == 1) {
            message.reply("Current Volume: " + gManager.audioPlayer.getVolume()).queue();
            return;
        }
        int newVol;
        try {
            newVol = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            message.reply("That's not a number lmao").queue();
            return;
        }

        if (newVol > 75) newVol = 75;
        gManager.audioPlayer.setVolume(newVol);

        message.reply("Player internal volume has been set to: **" + newVol + "**").queue();
    }
}
