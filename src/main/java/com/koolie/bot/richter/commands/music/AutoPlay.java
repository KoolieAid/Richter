package com.koolie.bot.richter.commands.music;

import com.koolie.bot.richter.commands.Interfaces.TextCommand;
import com.koolie.bot.richter.objects.Ignored;
import net.dv8tion.jda.api.entities.Message;
import org.jetbrains.annotations.NotNull;

@Ignored
public class AutoPlay implements TextCommand {
    @NotNull
    @Override
    public String getName() {
        return "AutoPlay";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Autoplay videos when queue ends";
    }

    @NotNull
    @Override
    public CommandType getCommandType() {
        return CommandType.Music;
    }

    @NotNull
    @Override
    public String getOperator() {
        return "autoplay";
    }

    @Override
    public void execute(@NotNull Message message) {

    }
}
