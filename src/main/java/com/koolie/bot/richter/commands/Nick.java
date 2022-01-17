package com.koolie.bot.richter.commands;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.HierarchyException;

public class Nick extends Command {
    public Nick() {
        setName("Nickname");
        setDescription("Changes the nickname of the mentioned user");
        setCommandType(commandType.General);
    }

    @Override
    public void execute(MessageReceivedEvent event) {
        String[] args = event.getMessage().getContentRaw().split(" ", 3);

        if (event.getMessage().getMentionedMembers().size() == 0) {
            event.getMessage().reply("Please include a user").queue();
            return;
        }

        if (event.getMessage().getMentionedMembers().get(0).isOwner()) {
            event.getMessage().reply("The mentioned user is the owner of the server, means I can't change their nickname").queue();
            return;
        }

        Member mentionedMember = event.getMessage().getMentionedMembers().get(0);
        String oldNick = event.getMessage().getMentionedMembers().get(0).getEffectiveName();

        if (!event.getGuild().getMember(event.getJDA().getSelfUser()).canInteract(mentionedMember)) {
            event.getMessage().reply("I can't modify their nickname, because of role positions").queue();
            return;
        }

        try {
            if (args.length == 2) {
                mentionedMember.modifyNickname(null).queue((e) -> event.getMessage().reply("Nickname has been reset").queue(),
                        (f) -> event.getMessage().reply("Something went wrong while changing their nickname. Error: " + f.getMessage()).queue());
                return;
            }

            mentionedMember.modifyNickname(args[2]).queue((s) -> event.getMessage().reply("Nickname changed from " + oldNick).queue(),
                    (e) -> event.getMessage().reply(e.getMessage()).queue());
        } catch (HierarchyException e) {
            event.getMessage().reply("The person's position is higher than mine in the server").queue();
        }

    }

    @Override
    public void slash(SlashCommandEvent event) {

    }
}
