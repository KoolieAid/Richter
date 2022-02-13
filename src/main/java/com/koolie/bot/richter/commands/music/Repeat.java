package com.koolie.bot.richter.commands.music;

import com.koolie.bot.richter.MusicUtil.MusicManager;
import com.koolie.bot.richter.commands.TextCommand;
import net.dv8tion.jda.api.entities.Message;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Repeat implements TextCommand {
    public Repeat() {}

    @NotNull
    @Override
    public String getName() {
        return "Repeat";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Repeats the song/queue. Available arguments: `single`, `queue`, `off`";
    }

    @NotNull
    @Override
    public CommandType getCommandType() {
        return CommandType.Music;
    }

    @NotNull
    @Override
    public String getOperator() {
        return "repeat";
    }

    @Nullable
    @Override
    public String[] getAliases() {
        return new String[]{"loop"};
    }

    @Override
    public void execute(Message message) {
        String[] args = message.getContentRaw().split(" ", 2);
        if (args.length < 2) {
            message.reply("Please provide arguments: Available arguments: `single`, `queue`, `off`").queue();
            return;
        }
        MusicManager manager = MusicManager.of(message.getGuild());
        switch (args[1]) {
            case "single" -> {
                manager.eventListener.setRepeatSingle();
                message.reply("The track will now repeat").queue();
            }
            case "queue" -> {
                manager.eventListener.setRepeatQueue();
                message.reply("The queue will now repeat").queue();
            }
            case "off" -> {
                manager.eventListener.setRepeatOff();
                message.reply("Repeat has been turned off").queue();
            }
            default -> {
                message.reply("bro. what does " + args[1] + " even mean???").queue();
            }
        }
    }
}
