package com.koolie.bot.richter.commands;

import net.dv8tion.jda.api.interactions.commands.CommandAutoCompleteInteraction;

public interface AutoSlashCommand extends SlashCommand {
    void completeOption(CommandAutoCompleteInteraction interaction);
}
