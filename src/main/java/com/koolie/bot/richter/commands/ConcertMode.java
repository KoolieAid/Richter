package com.koolie.bot.richter.commands;

import com.koolie.bot.richter.commands.Interfaces.TextCommand;
import com.koolie.bot.richter.objects.Ignored;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceGuildMuteEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;

@Ignored @Deprecated
public class ConcertMode implements TextCommand {

    private final HashMap<Long, ConcertModeAdapter> adapters;

    public ConcertMode() {
        adapters = new HashMap<>();
    }

    @Override
    @NotNull
    public String getName() {
        return "Concert Mode";
    }

    @Override
    @NotNull
    public String getDescription() {
        return "Makes the author the only one who can sing/talk in a channel";
    }

    @Override
    @NotNull
    public CommandType getCommandType() {
        return CommandType.General;
    }

    @NotNull
    @Override
    public String getOperator() {
        return "concertMode";
    }

    @Nullable
    @Override
    public String[] getAliases() {
        return new String[]{"concertmode", "cm"};
    }

    @Override
    public void execute(@NotNull Message message) {
        if (!message.getMember().hasPermission(Permission.MANAGE_SERVER)) {
            message.reply("You don't have the permissions to do that").queue();
            return;
        }
        String[] args = message.getContentRaw().split(" ");

        if (args.length == 1) {
            ConcertModeAdapter adapter = adapters.get(message.getGuild().getIdLong());
            if (adapter == null) {
                message.reply("Concert Mode is currently disabled. To enable, type `concertMode enable`. Be careful as this command mutes everyone in your voice channel except you.").queue();
                return;
            }
            message.reply("Concert Mode is currently enabled. To disable type `concertMode disable`").queue();
            return;
        }

        switch (args[1]) {
            case "enable" -> enable(message);
            case "disable" -> disable(message);
            case "who" -> info(message);
            default -> message.reply("Unknown argument").queue();
        }
    }

    private void enable(Message message) {
        if (message.getMember().getVoiceState().getChannel() == null) {
            message.reply("You are not connected to a voice channel").queue();
            return;
        }

        ConcertModeAdapter adapter = adapters.get(message.getGuild().getIdLong());
        if (adapter != null) {
            message.reply("Concert Mode is already on at: " + message.getGuild()
                            .getVoiceChannelById(adapter.getvChannelId())
                            .getName()
                            + "\nDisable concert mode first before changing channel")
                    .queue();
            return;
        }

        VoiceChannel voiceChannel = (VoiceChannel) message.getMember().getVoiceState().getChannel();
        Long vChannelId = voiceChannel.getIdLong();
        Long userId = message.getAuthor().getIdLong();

        ConcertModeAdapter adapterToAttach = new ConcertModeAdapter(vChannelId, userId);

        List<Member> list = voiceChannel.getMembers();

        for (Member m : list) {
            if (m != message.getMember()) {
                m.mute(true).queue();
            }
        }

        message.getJDA().addEventListener(adapterToAttach);
        adapters.put(message.getGuild().getIdLong(), adapterToAttach);

        message.reply("Concert Mode enabled; Connected to: " + voiceChannel.getName()).queue();
    }

    private void disable(Message message) {
        ConcertModeAdapter adapter = adapters.get(message.getGuild().getIdLong());
        if (adapter == null) {
            message.reply("Concert Mode is not enabled").queue();
            return;
        }

        if (!adapter.getUserId().equals(message.getAuthor().getIdLong())) {
            message.reply("You are not the MC").queue();
            return;
        }

        VoiceChannel voiceChannel = (VoiceChannel) message.getMember().getVoiceState().getChannel();

        List<Member> list = voiceChannel.getMembers();

        for (Member m : list) {
            if (m != message.getMember()) {
                m.mute(false).queue();
            }
        }

        message.getJDA().removeEventListener(adapter);
        adapters.remove(message.getGuild().getIdLong());

        message.reply("Concert Mode disabled").queue();
    }

    private void info(Message message) {
        ConcertModeAdapter adapter = adapters.get(message.getGuild().getIdLong());

        if (adapter == null || !adapter.isPopulated()) {
            message.reply("No channel is in concert mode").queue();
            return;
        }

        message.reply(message.getGuild().getMemberById(adapter.getUserId()).getAsMention() +
                " is the MC of " +
                message.getGuild().getVoiceChannelById(adapter.getvChannelId()).getName()).queue();
    }

    private class ConcertModeAdapter extends ListenerAdapter {
        private final Long vChannelId;
        private final Long userId;

        public ConcertModeAdapter(Long channel, Long mcId) {
            vChannelId = channel;
            userId = mcId;
        }

        public Long getvChannelId() {
            return vChannelId;
        }

        public Long getUserId() {
            return userId;
        }

        public boolean isPopulated() {
            return (vChannelId != null);
        }

        @Override
        public void onGuildVoiceUpdate(@NotNull GuildVoiceUpdateEvent event) {
            if (event.getChannelLeft() == null){
                onGuildVoiceJoin(event);
                return;
            }

            onGuildVoiceMove(event);
        }

        public void onGuildVoiceJoin(@NotNull GuildVoiceUpdateEvent event) {

            if (!getUserId().equals(event.getMember().getIdLong()) &&
                    getvChannelId().equals(event.getChannelJoined().getIdLong())) {
                event.getMember().mute(true).queue();
            } else {
                event.getMember().mute(false).queue();
            }

        }

        public void onGuildVoiceMove(@NotNull GuildVoiceUpdateEvent event) {

            if (!getUserId().equals(event.getMember().getIdLong()) &&
                    getvChannelId().equals(event.getChannelJoined().getIdLong())) {
                event.getMember().mute(true).queue();
            } else {
                event.getMember().mute(false).queue();
            }

        }

        @Override
        public void onGuildVoiceGuildMute(@NotNull GuildVoiceGuildMuteEvent event) {
            if (!event.isGuildMuted()) {
                if (!getUserId().equals(event.getMember().getIdLong()) &&
                        getvChannelId().equals(event.getVoiceState().getChannel().getIdLong())) {
                    event.getMember().mute(true).queue();
                } else {
                    event.getMember().mute(false).queue();
                }
            }
        }
    }
}