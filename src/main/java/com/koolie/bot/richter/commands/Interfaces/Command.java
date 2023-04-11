package com.koolie.bot.richter.commands.Interfaces;

import com.koolie.bot.richter.objects.Ignored;
import org.jetbrains.annotations.NotNull;

@Ignored
public interface Command {

    @NotNull
    String getName();

    @NotNull
    String getDescription();

    @NotNull
    default CommandType getCommandType() {
        return CommandType.Other;
    }

    @Ignored
    enum CommandType {
        General, Music, Power, Other
    }
}
