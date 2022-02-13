package com.koolie.bot.richter.commands;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

public class Upgrade implements TextCommand, SlashCommand {

    private final String roleId = "760002551461707806";

    public Upgrade() {}

    @NotNull
    @Override
    public String getName() {
        return "Upgrade";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Gives the mentioned user the everyone role";
    }

    @NotNull
    @Override
    public Command.CommandType getCommandType() {
        return CommandType.Power;
    }

    @Override
    public String getOperator() {
        return "upgrade";
    }

    @Override
    public String[] getAliases() {
        return null;
    }

    @NotNull
    @Override
    public String getEffectiveCommand() {
        return "upgrade";
    }

    @Override
    public void execute(Message event) {
        if (!event.getGuild().getId().equals("759999287270047745")) {
            event.reply("This command is only for a private server").queue();
            return;
        }
        Guild guild = event.getGuild();

        if (event.getMentionedMembers().size() == 0) {
            event.reply("No user specified").queue();
            return;
        }

        if (!event.getMember().getRoles().contains(guild.getRoleById(roleId))) {
            event.reply("You don't have the role yourself").queue();
            return;
        }

        Iterator iterator = event.getMentionedMembers().iterator();

        Role roleObj = guild.getRoleById(roleId);

        while (iterator.hasNext()) {
            Member member = (Member) iterator.next();
            guild.addRoleToMember(member, roleObj).queue();
        }

        event.reply("User(s) have been upgraded").queue();
    }

    @Override
    public void onSlash(SlashCommandInteractionEvent event) {
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
