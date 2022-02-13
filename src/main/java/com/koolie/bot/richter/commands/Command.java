package com.koolie.bot.richter.commands;

import com.koolie.bot.richter.objects.Ignored;

import javax.annotation.Nonnull;

@Ignored
public interface Command {

    @Nonnull
    String getName();

    @Nonnull
    String getDescription();

    @Nonnull
    default CommandType getCommandType() { return CommandType.Other; }

    @Ignored
    enum CommandType {
        General, Music, Power, Other
    }
}
