package com.koolie.bot.richter.commands;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Iterator;

public class Upgrade extends Command {

    private final String roleId = "760002551461707806";

    public Upgrade() {
        this.setName("Upgrade");
        this.setDescription("Gives the mentioned user the everyone role");
        this.setCommandType(commandType.Power);
    }

    @Override
    public void execute(MessageReceivedEvent event) {
        if (!event.getGuild().getId().equals("759999287270047745")) {
            event.getMessage().reply("This command is only for a private server").queue();
            return;
        }
        Message message = event.getMessage();
        Guild guild = message.getGuild();

        if (message.getMentionedMembers().size() == 0) {
            message.reply("No user specified").queue();
            return;
        }

        if (!message.getMember().getRoles().contains(guild.getRoleById(roleId))) {
            message.reply("You don't have the role yourself").queue();
            return;
        }

        Iterator iterator = message.getMentionedMembers().iterator();

        Role roleObj = guild.getRoleById(roleId);

        while (iterator.hasNext()) {
            Member member = (Member) iterator.next();
            guild.addRoleToMember(member, roleObj).queue();
        }

        message.reply("User(s) have been upgraded").queue();
    }

    @Override
    public void slash(SlashCommandEvent event) {
        if (!event.getMember().getRoles().contains(event.getGuild().getRoleById(roleId))) {
            event.reply("You don't have the role yourself").setEphemeral(true).queue();
            return;
        }

        Role roleObj = event.getGuild().getRoleById(roleId);
        Member member = event.getOption("target").getAsMember();

        event.getGuild().addRoleToMember(member, roleObj).queue((s) -> event.reply("User has been upgraded").setEphemeral(true).queue()
                , (e) -> event.reply("There seems to be a problem doing that task").setEphemeral(true).queue());

    }
}
