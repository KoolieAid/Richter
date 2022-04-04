package com.koolie.bot.richter.commands.music;

import com.koolie.bot.richter.MusicUtil.MusicManager;
import com.koolie.bot.richter.commands.Interfaces.TextCommand;
import com.koolie.bot.richter.objects.guild.GuildConfig;
import com.koolie.bot.richter.util.MusicUtil;
import net.dv8tion.jda.api.entities.Message;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Volume implements TextCommand {
    public Volume() {
    }

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
        return new String[]{"vol"};
    }

    @Override
    public void execute(@NotNull Message message) {
        if (!MusicManager.isPresent(message.getGuild())) {
            message.getChannel().sendMessage("No music is currently playing").queue();
            return;
        }

        if (!MusicUtil.isInSameChannel(message.getMember().getVoiceState().getChannel(), message.getMember(), message.getGuild().getSelfMember())) {
            message.reply("You must be in the same voice channel as me to use this command.").queue();
            return;
        }

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

        if (newVol > 100) newVol = 100;
        if (newVol < 0) newVol = 0;
        gManager.audioPlayer.setVolume(newVol);

        GuildConfig config = GuildConfig.of(message.getGuild().getIdLong());
        config.setPlayerVolume(newVol);

        message.reply("Player internal volume has been set to: **" + newVol + "**").queue();
    }
}
