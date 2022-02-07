package com.koolie.bot.richter.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.StageChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class Activity extends Command {
    public Activity() {
        setName("activity");
        setDescription("Starts a Discord Game Activity");
        setCommandType(commandType.General);
    }

    @Override
    public void execute(MessageReceivedEvent event) {
        event.getMessage().reply("Activity command is restricted to slash commands. Use `/activity` instead.").queue();
    }

    @Override
    public void slash(SlashCommandInteractionEvent event) {
        if (event.getMember().getVoiceState().getChannel() == null) {
            event.reply("You must be in a voice channel to use this command.").queue();
            return;
        }

        if (event.getMember().getVoiceState().getChannel() instanceof StageChannel){
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
