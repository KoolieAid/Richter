package com.koolie.bot.richter.commands;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;

public class LockChannel implements TextCommand {
    private final HashMap<Long, LockChannelAdapter> adapters;

    public LockChannel() {
        adapters = new HashMap<>();
    }

    @NotNull
    @Override
    public String getName() {
        return "Lock Channel";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Locks the people already inside the channel. No one goes out, no one comes in.";
    }

    @NotNull
    @Override
    public CommandType getCommandType() {
        return CommandType.Power;
    }

    @NotNull
    @Override
    public String getOperator() {
        return "lockChannel";
    }

    @Nullable
    @Override
    public String[] getAliases() {
        return new String[]{"lock", "lc"};
    }

    @Override
    public void execute(Message event) {
        if (!event.getMember().hasPermission(Permission.MANAGE_SERVER)) {
            event.reply("You don't have the permissions to do that").queue();
            return;
        }
        String[] args = event.getContentRaw().split(" ");
        Member member = event.getMember();

        if (args.length != 1) {
            if (args[1].equals("who")) {
                LockChannelAdapter adapter = adapters.get(event.getGuild().getIdLong());

                if (adapter == null) {
                    event.reply("No channel is locked").queue();
                    return;
                }

                event.reply(event.getJDA().getVoiceChannelById(adapter.getChannelId()).getName() + " is locked.").queue();
                return;
            }
        }

        // Switcherino
        LockChannelAdapter adapter = adapters.get(event.getGuild().getIdLong());
        //If enabled
        if (adapter != null) {
            if (!adapter.getLockedMembers().contains(event.getAuthor().getIdLong())) {
                event.reply("You are not permitted to do this command").queue();
                return;
            }

            event.reply("Channel " + event.getJDA().getVoiceChannelById(adapter.getChannelId()).getName() + " is __unlocked__").queue();
            event.getJDA().removeEventListener(adapter);
            adapters.remove(event.getGuild().getIdLong());

        } else { //If disabled
            if (member.getVoiceState().getChannel() == null) {
                event.reply("You are not in a channel").queue();
                return;
            }

            ArrayList<Long> lockedMembers = new ArrayList<>();
            member.getVoiceState().getChannel().getMembers().forEach((m) -> lockedMembers.add(m.getIdLong()));
            long channelID = member.getVoiceState().getChannel().getIdLong();

            LockChannelAdapter adapterToAttach = new LockChannelAdapter(channelID, lockedMembers);

            event.getJDA().addEventListener(adapterToAttach);
            adapters.put(event.getGuild().getIdLong(), adapterToAttach);

            event.reply("Channel " + event.getJDA().getVoiceChannelById(channelID).getName() + " __locked__").queue();
        }
    }

    private class LockChannelAdapter extends ListenerAdapter {

        private final Long channelId;
        private final ArrayList<Long> lockedMembers;

        public LockChannelAdapter(Long channelId, ArrayList<Long> listOfMembers) {
            this.channelId = channelId;
            lockedMembers = listOfMembers;
        }

        public Long getChannelId() {
            return channelId;
        }

        public ArrayList<Long> getLockedMembers() {
            return lockedMembers;
        }

        @Override
        public void onGuildVoiceJoin(@NotNull GuildVoiceJoinEvent event) {

            if (!lockedMembers.contains(event.getMember().getIdLong()) &&
                    channelId.equals(event.getChannelJoined().getIdLong())) {
                event.getGuild().moveVoiceMember(event.getMember(), null).queue();
            } else if (lockedMembers.contains(event.getMember().getIdLong()) &&
                    !channelId.equals(event.getChannelJoined().getIdLong())) {
                event.getGuild().moveVoiceMember(event.getMember(), null).queue();
            }

        }

        @Override
        public void onGuildVoiceMove(@NotNull GuildVoiceMoveEvent event) {

            if (!lockedMembers.contains(event.getMember().getIdLong()) &&
                    channelId.equals(event.getChannelJoined().getIdLong())) {
                event.getGuild().moveVoiceMember(event.getMember(), event.getChannelLeft()).queue();
            } else if (lockedMembers.contains(event.getMember().getIdLong()) &&
                    !channelId.equals(event.getChannelJoined().getIdLong())) {
                event.getGuild().moveVoiceMember(event.getMember(), event.getChannelLeft()).queue();
            }

        }
    }
}


