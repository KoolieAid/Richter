package com.koolie.bot.richter.commands.Interfaces;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

public interface SlashCommand extends Command {
    @NotNull
    String getEffectiveCommand();

    void onSlash(SlashCommandInteractionEvent event);
}
