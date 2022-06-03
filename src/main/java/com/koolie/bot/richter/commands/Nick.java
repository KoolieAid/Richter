package com.koolie.bot.richter.commands;

import com.koolie.bot.richter.commands.Interfaces.TextCommand;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.exceptions.HierarchyException;
import org.jetbrains.annotations.NotNull;

public class Nick implements TextCommand {
    public Nick() {
    }

    @NotNull
    @Override
    public String getOperator() {
        return "nick";
    }

    @Override
    public void execute(@NotNull Message event) {
        String[] args = event.getContentRaw().split(" ", 3);

        if (event.getMentions().getMembers().size() == 0) {
            event.reply("Please include a user").queue();
            return;
        }

        if (event.getMentions().getMembers().get(0).isOwner()) {
            event.reply("The mentioned user is the owner of the server, means I can't change their nickname").queue();
            return;
        }

        Member mentionedMember = event.getMentions().getMembers().get(0);
        String oldNick = event.getMentions().getMembers().get(0).getEffectiveName();

        if (!event.getGuild().getMember(event.getJDA().getSelfUser()).canInteract(mentionedMember)) {
            event.reply("I can't modify their nickname, because of role positions").queue();
            return;
        }

        try {
            if (args.length == 2) {
                mentionedMember.modifyNickname(null).queue((e) -> event.reply("Nickname has been reset").queue(),
                        (f) -> event.reply("Something went wrong while changing their nickname. Error: " + f.getMessage()).queue());
                return;
            }

            mentionedMember.modifyNickname(args[2]).queue((s) -> event.reply("Nickname changed from " + oldNick).queue(),
                    (e) -> event.reply(e.getMessage()).queue());
        } catch (HierarchyException e) {
            event.reply("The person's position is higher than mine in the server").queue();
        }

    }

    @NotNull
    @Override
    public String getName() {
        return "Nick";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Changes the nickname of the mentioned user";
    }

    @NotNull
    @Override
    public CommandType getCommandType() {
        return CommandType.General;
    }
}
