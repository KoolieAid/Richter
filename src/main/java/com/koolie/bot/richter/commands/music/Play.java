package com.koolie.bot.richter.commands.music;

import com.koolie.bot.richter.MusicUtil.MusicManager;
import com.koolie.bot.richter.commands.Interfaces.AutoSlashCommand;
import com.koolie.bot.richter.commands.Interfaces.ContextCommand;
import com.koolie.bot.richter.commands.Interfaces.TextCommand;
import com.koolie.bot.richter.objects.context.Context;
import com.koolie.bot.richter.objects.context.InteractionContext;
import com.koolie.bot.richter.objects.context.MessageContext;
import com.koolie.bot.richter.util.MusicUtil;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.interactions.commands.CommandAutoCompleteInteraction;
import org.jetbrains.annotations.NotNull;

import java.net.MalformedURLException;
import java.net.URL;

public class Play implements TextCommand, ContextCommand, AutoSlashCommand {
    public Play() {
    }

    @Override
    public @NotNull String getName() {
        return "Play";
    }

    @Override
    public @NotNull String getDescription() {
        return "Plays music";
    }

    @Override
    public @NotNull CommandType getCommandType() {
        return CommandType.Music;
    }

    @Override
    public @NotNull String getEffectiveName() {
        return "Add to queue";
    }

    @Override
    public @NotNull String getOperator() {
        return "play";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"p"};
    }

    @NotNull
    @Override
    public String getEffectiveCommand() {
        return "play";
    }

    @Override
    public void execute(@NotNull Message message) {
        load(new MessageContext(message), message.getContentRaw());
    }

    @Override
    public void onContext(MessageContextInteractionEvent event) {
        load(new InteractionContext(event), "ignored " + event.getInteraction().getTarget().getContentRaw());
    }

    @Override
    public void onSlash(SlashCommandInteractionEvent event) {
        load(new InteractionContext(event), "ignored " + event.getInteraction().getOption("query").getAsString());
    }

    @Override
    public void completeOption(CommandAutoCompleteInteraction interaction) {
        MusicManager.autoComplete(interaction.getFocusedOption().getValue(), interaction);
    }

    private void load(Context context, String rawText) {
        AudioChannel vChannel = context.getMember().getVoiceState().getChannel();
        if (vChannel == null) {
            context.reply("Bro. You are not in a voice channel").queue();
            return;
        }

        if (!MusicUtil.isInSameChannel(vChannel, context.getMember(), context.getGuild().getSelfMember()) &&
                MusicManager.isPresent(context.getGuild())) {
            context.reply("Hey! Have some manners. Other people are using me.\nYou have to join the same voice channel to be able to use this command").queue();
            return;
        }

        context.getChannel().sendTyping().queue();
        String[] args = rawText.split(" ", 2);
        if (args.length == 1) {
            context.reply("""
                    ———————————No query?———————————
                    ⠀⣞⢽⢪⢣⢣⢣⢫⡺⡵⣝⡮⣗⢷⢽⢽⢽⣮⡷⡽⣜⣜⢮⢺⣜⢷⢽⢝⡽⣝
                    ⠸⡸⠜⠕⠕⠁⢁⢇⢏⢽⢺⣪⡳⡝⣎⣏⢯⢞⡿⣟⣷⣳⢯⡷⣽⢽⢯⣳⣫⠇
                    ⠀⠀⢀⢀⢄⢬⢪⡪⡎⣆⡈⠚⠜⠕⠇⠗⠝⢕⢯⢫⣞⣯⣿⣻⡽⣏⢗⣗⠏⠀
                    ⠀⠪⡪⡪⣪⢪⢺⢸⢢⢓⢆⢤⢀⠀⠀⠀⠀⠈⢊⢞⡾⣿⡯⣏⢮⠷⠁⠀⠀
                    ⠀⠀⠀⠈⠊⠆⡃⠕⢕⢇⢇⢇⢇⢇⢏⢎⢎⢆⢄⠀⢑⣽⣿⢝⠲⠉⠀⠀⠀⠀
                    ⠀⠀⠀⠀⠀⡿⠂⠠⠀⡇⢇⠕⢈⣀⠀⠁⠡⠣⡣⡫⣂⣿⠯⢪⠰⠂⠀⠀⠀⠀
                    ⠀⠀⠀⠀⡦⡙⡂⢀⢤⢣⠣⡈⣾⡃⠠⠄⠀⡄⢱⣌⣶⢏⢊⠂⠀⠀⠀⠀⠀⠀
                    ⠀⠀⠀⠀⢝⡲⣜⡮⡏⢎⢌⢂⠙⠢⠐⢀⢘⢵⣽⣿⡿⠁⠁⠀⠀⠀⠀⠀⠀⠀
                    ⠀⠀⠀⠀⠨⣺⡺⡕⡕⡱⡑⡆⡕⡅⡕⡜⡼⢽⡻⠏⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
                    ⠀⠀⠀⠀⣼⣳⣫⣾⣵⣗⡵⡱⡡⢣⢑⢕⢜⢕⡝⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
                    ⠀⠀⠀⣴⣿⣾⣿⣿⣿⡿⡽⡑⢌⠪⡢⡣⣣⡟⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
                    ⠀⠀⠀⡟⡾⣿⢿⢿⢵⣽⣾⣼⣘⢸⢸⣞⡟⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
                    ⠀⠀⠀⠀⠁⠇⠡⠩⡫⢿⣝⡻⡮⣒⢽⠋⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
                    —————————————————————————————
                    """).queue();
            return;
        }

        String query = args[1];

        try {
            new URL(query);
        } catch (MalformedURLException e) {
            query = "ytsearch:" + query;
        }

        MusicManager.loadToGuild(context, query);

        MusicManager musicManager = MusicManager.of(context.getGuild());
        if (musicManager.audioPlayer.isPaused()) {
            musicManager.audioPlayer.setPaused(false);
        }
        try {
            context.getGuild().getAudioManager().openAudioConnection(vChannel);
        } catch (InsufficientPermissionException e) {
            context.reply("I can't seem to connect to that channel").queue();
            musicManager.eventListener.queue.clear();
            musicManager.eventListener.nextTrack();
        }
        context.getGuild().getAudioManager().setSelfDeafened(true);
    }
}
