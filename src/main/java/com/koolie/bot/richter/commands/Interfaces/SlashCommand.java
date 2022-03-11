package com.koolie.bot.richter.commands.Interfaces;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import javax.annotation.Nonnull;

public interface SlashCommand extends Command {
    @Nonnull
    String getEffectiveCommand();

    void onSlash(SlashCommandInteractionEvent event);
}
