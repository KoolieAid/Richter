package com.koolie.bot.richter.commands;

import com.koolie.bot.richter.commands.Interfaces.SlashCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.StageChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

public class Activity implements SlashCommand {
    public Activity() {
    }

    @NotNull
    @Override
    public String getName() {
        return "Activity";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Starts a Discord Game Activity";
    }

    @NotNull
    @Override
    public CommandType getCommandType() {
        return CommandType.General;
    }

    @NotNull
    @Override
    public String getEffectiveCommand() {
        return "activity";
    }

    @Override
    public void onSlash(SlashCommandInteractionEvent event) {
        if (event.getMember().getVoiceState().getChannel() == null) {
            event.reply("You must be in a voice channel to use this command.").queue();
            return;
        }

        if (event.getMember().getVoiceState().getChannel() instanceof StageChannel) {
            event.reply("Stage Channels are not permitted to use activity. This is a Discord limitation.").queue();
            return;
        }

        VoiceChannel voiceChannel = (VoiceChannel) event.getMember().getVoiceState().getChannel();
        event.getOption("game").getAsString();
        voiceChannel.createInvite().setTargetApplication(event.getOption("game").getAsString()).queue(invite -> {
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setDescription("Here's the invite: " + invite.getUrl());
            event.replyEmbeds(embedBuilder.build()).queue();
        }, exception -> {
            event.reply("I don't have permission to create an invite in this channel.").queue();
        });
    }
}
