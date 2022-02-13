package com.koolie.bot.richter.commands;

import javax.annotation.Nonnull;

public interface Command {

    @Nonnull
    String getName();

    @Nonnull
    String getDescription();

    @Nonnull
    default CommandType getCommandType() { return CommandType.Other; }

    enum CommandType {
        General, Music, Power, Other
    }
}
