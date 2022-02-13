package com.koolie.bot.richter.commands.music;

import com.koolie.bot.richter.MusicUtil.MusicManager;
import com.koolie.bot.richter.commands.Command;
import com.koolie.bot.richter.commands.TextCommand;
import com.koolie.bot.richter.objects.Context;
import net.dv8tion.jda.api.entities.AudioChannel;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.MalformedURLException;
import java.net.URL;

public class PlayNext implements TextCommand {
    public PlayNext() {}

    @NotNull
    @Override
    public String getName() {
        return "playnext";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Adds a song to the front of the queue";
    }

    @NotNull
    @Override
    public Command.CommandType getCommandType() {
        return CommandType.Music;
    }

    @NotNull
    @Override
    public String getOperator() {
        return "playnext";
    }

    @Nullable
    @Override
    public String[] getAliases() {
        return new String[]{"pn", "addnext"};
    }

    @Override
    public void execute(Message message) {
        AudioChannel vChannel = message.getMember().getVoiceState().getChannel();
        if (vChannel == null) {
            message.reply("You are not in a voice channel you dimwit").queue();
            return;
        }

        message.getChannel().sendTyping().queue();
        String[] args = message.getContentRaw().split(" ", 2);
        if (args.length == 1) {
            message.reply("Bruh wheres your query???").queue();
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

        MusicManager.loadToGuild(new Context(message), query, true);

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
}
