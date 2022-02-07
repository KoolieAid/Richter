package com.koolie.bot.richter.commands;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;

public class LockChannel extends Command {
    private final HashMap<Long, LockChannelAdapter> adapters;

    public LockChannel() {
        setName("Lock Channel");
        setDescription("Locks the people already inside the channel. No one goes out, no one comes in.");
        setCommandType(commandType.Power);

        adapters = new HashMap<>();
    }

    @Override
    public void execute(MessageReceivedEvent event) {
        if (!event.getMember().hasPermission(Permission.MANAGE_SERVER)) {
            event.getMessage().reply("You don't have the permissions to do that").queue();
            return;
        }
        Message message = event.getMessage();
        String[] args = message.getContentRaw().split(" ");
        Member member = event.getMember();

        if (args.length != 1) {
            if (args[1].equals("who")) {
                LockChannelAdapter adapter = adapters.get(event.getGuild().getIdLong());

                if (adapter == null) {
                    message.reply("No channel is locked").queue();
                    return;
                }

                message.reply(event.getJDA().getVoiceChannelById(adapter.getChannelId()).getName() + " is locked.").queue();
                return;
            }
        }

        // Switcherino
        LockChannelAdapter adapter = adapters.get(event.getGuild().getIdLong());
        //If enabled
        if (adapter != null) {
            if (!adapter.getLockedMembers().contains(message.getAuthor().getIdLong())) {
                message.reply("You are not permitted to do this command").queue();
                return;
            }

            message.reply("Channel " + event.getJDA().getVoiceChannelById(adapter.getChannelId()).getName() + " is __unlocked__").queue();
            event.getJDA().removeEventListener(adapter);
            adapters.remove(event.getGuild().getIdLong());

        } else { //If disabled
            if (member.getVoiceState().getChannel() == null) {
                message.reply("You are not in a channel").queue();
                return;
            }

            ArrayList<Long> lockedMembers = new ArrayList<>();
            member.getVoiceState().getChannel().getMembers().forEach((m) -> lockedMembers.add(m.getIdLong()));
            long channelID = member.getVoiceState().getChannel().getIdLong();

            LockChannelAdapter adapterToAttach = new LockChannelAdapter(channelID, lockedMembers);

            event.getJDA().addEventListener(adapterToAttach);
            adapters.put(event.getGuild().getIdLong(), adapterToAttach);

            message.reply("Channel " + event.getJDA().getVoiceChannelById(channelID).getName() + " __locked__").queue();
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


