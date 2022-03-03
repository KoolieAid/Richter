package com.koolie.bot.richter.commands.music;

import com.koolie.bot.richter.MusicUtil.MusicManager;
import com.koolie.bot.richter.commands.AutoSlashCommand;
import com.koolie.bot.richter.commands.ContextCommand;
import com.koolie.bot.richter.commands.TextCommand;
import com.koolie.bot.richter.objects.Context;
import net.dv8tion.jda.api.entities.AudioChannel;
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
    public String getOperator() {
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
    public void execute(Message message) {
        AudioChannel vChannel = message.getMember().getVoiceState().getChannel();
        if (vChannel == null) {
            message.reply("Bro. You are not in a voice channel").queue();
            return;
        }

        message.getChannel().sendTyping().queue();
        String[] args = message.getContentRaw().split(" ", 2);
        if (args.length == 1) {
            message.reply("""
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

        String query;
        query = args[1];

        try {
            URL url = new URL(query);
            if (!url.getHost().equalsIgnoreCase("open.spotify.com")) throw new MalformedURLException();

        } catch (MalformedURLException e) {
            if (!query.matches("^((?:https?:)?\\/\\/)?((?:www|m)\\.)?((?:youtube\\.com|youtu.be))(\\/(?:[\\w\\-]+\\?v=|embed\\/|v\\/)?)([\\w\\-]+)(\\S+)?$")
                    && !query.startsWith("direct:")) {
                query = "ytsearch:" + query;
            }
        }

        if (query.startsWith("direct:")) {
            query = query.substring("direct:".length());
        }

        MusicManager.loadToGuild(new Context(message), query);

        MusicManager musicManager = MusicManager.of(message.getGuild());
        if (musicManager.audioPlayer.isPaused()) {
            musicManager.audioPlayer.setPaused(false);
        }
        try {
            message.getGuild().getAudioManager().openAudioConnection(vChannel);
        } catch (InsufficientPermissionException e) {
            message.reply("I can't seem to connect to that channel").queue();
            musicManager.eventListener.queue.clear();
            musicManager.eventListener.nextTrack();
        }
        message.getGuild().getAudioManager().setSelfDeafened(true);
    }

    @Override
    public void onContext(MessageContextInteractionEvent event) {
        AudioChannel vChannel = event.getMember().getVoiceState().getChannel();
        if (vChannel == null) {
            event.getInteraction().reply("Bro. You are not in a voice channel").setEphemeral(true).queue();
            return;
        }

        event.getChannel().sendTyping().queue();

        String query = event.getInteraction().getTarget().getContentRaw();

        try {
            URL url = new URL(query);
            if (!url.getHost().equalsIgnoreCase("open.spotify.com")) throw new MalformedURLException();

        } catch (MalformedURLException e) {
            if (!query.matches("^((?:https?:)?\\/\\/)?((?:www|m)\\.)?((?:youtube\\.com|youtu.be))(\\/(?:[\\w\\-]+\\?v=|embed\\/|v\\/)?)([\\w\\-]+)(\\S+)?$")
                    && !query.startsWith("direct:")) {
                query = "ytsearch:" + query;
            }
        }

        if (query.startsWith("direct:")) {
            query = query.substring("direct:".length());
        }


        MusicManager.loadToGuild(new Context(event.getInteraction()), query);

        MusicManager musicManager = MusicManager.of(event.getGuild());
        if (musicManager.audioPlayer.isPaused()) {
            musicManager.audioPlayer.setPaused(false);
        }
        try {
            event.getGuild().getAudioManager().openAudioConnection(vChannel);
        } catch (InsufficientPermissionException e) {
            event.getInteraction().getTarget().reply("I can't seem to connect to that channel").queue();
            musicManager.eventListener.queue.clear();
            musicManager.eventListener.nextTrack();
        }
        event.getGuild().getAudioManager().setSelfDeafened(true);
    }

    @Override
    public void onSlash(SlashCommandInteractionEvent event) {
        AudioChannel vChannel = event.getMember().getVoiceState().getChannel();
        if (vChannel == null) {
            event.reply("Bro. You are not in a voice channel").setEphemeral(true).queue();
            return;
        }

        event.getChannel().sendTyping().queue();
        String query = event.getInteraction().getOption("query").getAsString();

        try {
            URL url = new URL(query);
            if (!url.getHost().equalsIgnoreCase("open.spotify.com")) throw new MalformedURLException();

        } catch (MalformedURLException e) {
            if (!query.matches("^((?:https?:)?\\/\\/)?((?:www|m)\\.)?((?:youtube\\.com|youtu.be))(\\/(?:[\\w\\-]+\\?v=|embed\\/|v\\/)?)([\\w\\-]+)(\\S+)?$")
                    && !query.startsWith("direct:")) {
                query = "ytsearch:" + query;
            }
        }

        if (query.startsWith("direct:")) {
            query = query.substring("direct:".length());
        }

        MusicManager.loadToGuild(new Context(event), query);

        MusicManager musicManager = MusicManager.of(event.getGuild());
        if (musicManager.audioPlayer.isPaused()) {
            musicManager.audioPlayer.setPaused(false);
        }
        try {
            event.getGuild().getAudioManager().openAudioConnection(vChannel);
        } catch (InsufficientPermissionException e) {
            event.reply("I can't seem to connect to that channel").setEphemeral(true).queue();
            musicManager.eventListener.queue.clear();
            musicManager.eventListener.nextTrack();
        }
        event.getGuild().getAudioManager().setSelfDeafened(true);
    }

    @Override
    public void completeOption(CommandAutoCompleteInteraction interaction) {
        MusicManager.autoComplete(interaction.getFocusedOption().getValue(), interaction);
    }
}
