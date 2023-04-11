package com.koolie.bot.richter.commands.Interfaces;

import net.dv8tion.jda.api.entities.Message;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface TextCommand extends Command {
    @NotNull
    String getOperator();

    @Nullable
    default String[] getAliases() {
        return null;
    }

    void execute(@NotNull Message message);
}
