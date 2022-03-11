package com.koolie.bot.richter.commands.Interfaces;

import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;

import javax.annotation.Nonnull;

public interface ContextCommand extends Command {
    @Nonnull
    String getEffectiveName();

    void onContext(MessageContextInteractionEvent event);
}