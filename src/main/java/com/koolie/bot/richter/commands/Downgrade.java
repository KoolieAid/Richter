package com.koolie.bot.richter.commands;

import com.koolie.bot.richter.commands.Interfaces.SlashCommand;
import com.koolie.bot.richter.commands.Interfaces.TextCommand;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

public class Downgrade implements TextCommand, SlashCommand {

    private final String roleId = "760002551461707806";

    public Downgrade() {
    }

    @NotNull
    @Override
    public String getName() {
        return "Downgrade";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Removes the everyone role from specified user";
    }

    @NotNull
    @Override
    public CommandType getCommandType() {
        return CommandType.Power;
    }

    @NotNull
    @Override
    public String getOperator() {
        return "downgrade";
    }

    @NotNull
    @Override
    public String getEffectiveCommand() {
        return "downgrade";
    }

    @Override
    public void execute(@NotNull Message event) {
        if (!event.getGuild().getId().equals("759999287270047745")) {
            event.reply("This command is only for a private server").queue();
            return;
        }
        Guild guild = event.getGuild();

        if (event.getMentions().getMembers().size() == 0) {
            event.reply("No user specified").queue();
            return;
        }

        if (!event.getMember().getRoles().contains(guild.getRoleById(roleId))) {
            event.reply("You don't have the role yourself").queue();
            return;
        }

        Role roleObj = guild.getRoleById(roleId);
        if (roleObj == null) {
            LoggerFactory.getLogger(this.getClass()).error("Role ID is incorrect");
            return;
        }

        event.getMentions().getMembers().forEach(member -> guild.removeRoleFromMember(member, roleObj).queue());

        event.reply("User(s) have been downgraded").queue();
    }

    @Override
    public void onSlash(SlashCommandInteractionEvent event) {
        if (!event.getMember().getRoles().contains(event.getGuild().getRoleById(roleId))) {
            event.reply("You don't have the role yourself.").setEphemeral(true).queue();
            return;
        }

        event.getGuild().removeRoleFromMember(event.getOption("target").getAsMember(), event.getGuild().getRoleById(roleId))
                .queue((s) -> event.reply("User has been downgraded").setEphemeral(true).queue()
                        , (e) -> event.reply("There seems to be a problem doing that task").setEphemeral(true).queue());
    }
}
