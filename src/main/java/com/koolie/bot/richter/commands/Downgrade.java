package com.koolie.bot.richter.commands;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Iterator;

public class Downgrade extends Command {

    private final String roleId = "760002551461707806";

    public Downgrade() {
        this.setName("Downgrade");
        this.setDescription("Removes the everyone role from specified user");
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
            guild.removeRoleFromMember(member, roleObj).queue();
        }

        message.reply("User(s) have been downgraded").queue();
    }

    @Override
    public void slash(SlashCommandInteractionEvent event) {
        if (!event.getMember().getRoles().contains(event.getGuild().getRoleById(roleId))) {
            event.reply("You don't have the role yourself.").setEphemeral(true).queue();
            return;
        }

        event.getGuild().removeRoleFromMember(event.getOption("target").getAsMember(), event.getGuild().getRoleById(roleId))
                .queue((s) -> event.reply("User has been downgraded").setEphemeral(true).queue()
                        , (e) -> event.reply("There seems to be a problem doing that task").setEphemeral(true).queue());
    }
}
