package com.koolie.bot.richter.commands;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class Command {
    @Nonnull
    private String name;
    @Nonnull
    private String description;

    @Nullable
    private commandType cType = commandType.Other;

    @Nonnull
    public String getName() {
        return name;
    }

    public void setName(@Nonnull String name) {
        this.name = name;
    }

    @Nonnull
    public String getDescription() {
        return description;
    }

    public void setDescription(@Nonnull String description) {
        this.description = description;
    }

    @Nullable
    public commandType getCommandType() {
        return cType;
    }

    public void setCommandType(@Nullable commandType cType) {
        this.cType = cType;
    }

    public abstract void execute(MessageReceivedEvent event);

    public void slash(SlashCommandEvent event) {
        event.reply("Seems like the developer is not finished with this one yet.").setEphemeral(true).queue();
    }

    public enum commandType {
        General, Music, Power, Other
    }
}
