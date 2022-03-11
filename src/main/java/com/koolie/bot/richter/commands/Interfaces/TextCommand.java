package com.koolie.bot.richter.commands.Interfaces;

import net.dv8tion.jda.api.entities.Message;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface TextCommand extends Command {
    @Nonnull
    String getOperator();

    @Nullable
    default String[] getAliases() {
        return null;
    }

    void execute(@Nonnull Message message);
}
