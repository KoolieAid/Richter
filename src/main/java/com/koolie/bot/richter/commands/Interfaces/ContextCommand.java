package com.koolie.bot.richter.commands.Interfaces;

import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import org.jetbrains.annotations.NotNull;

public interface ContextCommand extends Command {
    @NotNull
    String getEffectiveName();

    void onContext(MessageContextInteractionEvent event);
}