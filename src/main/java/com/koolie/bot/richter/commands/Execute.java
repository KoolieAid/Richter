package com.koolie.bot.richter.commands;

import com.koolie.bot.richter.EventHandler;
import com.koolie.bot.richter.util.BotConfigManager;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class Execute implements ContextCommand{
    @NotNull
    @Override
    public String getName() {
        return "Execute";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Execute a command with context message";
    }

    @NotNull
    @Override
    public CommandType getCommandType() {
        return CommandType.General;
    }

    @NotNull
    @Override
    public String getEffectiveName() {
        return "Execute";
    }

    @Override
    public void onContext(MessageContextInteractionEvent event) {
        Map<String, TextCommand> map = EventHandler.getCommands();
        Map<String, String> alias = EventHandler.getAliases();

        if (event.getTarget().getAuthor().isBot()) {
            event.getInteraction().reply("You can't execute commands from bots").setEphemeral(true).queue();
            return;
        }
        if (!event.getTarget().isFromGuild()) {
            event.getInteraction().reply("You can't execute commands from DMs").setEphemeral(true).queue();
            return;
        }

        if (!event.getUser().equals(event.getTarget().getAuthor())){
            event.getInteraction().reply("You can't execute commands from other users").setEphemeral(true).queue();
            return;
        }

        String[] args = event.getTarget().getContentRaw().split(" ", 2);
        if (!args[0].startsWith(BotConfigManager.getPrefix())) {
            event.getInteraction().reply("Message is not a command").setEphemeral(true).queue();
            return;
        }
        String command = args[0].replaceFirst(BotConfigManager.getPrefix(), "");

        if(!map.containsKey(command) && !alias.containsKey(command)) {
            event.getInteraction().reply("Command not found").setEphemeral(true).queue();
            return;
        }

        try {
            if (map.containsKey(command) && !alias.containsKey(command)) {
                map.get(command).execute(event.getTarget());
                event.getInteraction().reply("Command executed").setEphemeral(true).queue();
                return;
            }
            map.get(alias.get(command)).execute(event.getTarget());
            event.getInteraction().reply("Command executed").setEphemeral(true).queue();
        } catch (InsufficientPermissionException e) {
            event.getTarget().reply("Seems like I don't have the necessary permission for that!\n"
                    + "I needed `" + e.getPermission().getName() + "`").queue();
        } catch (Exception e) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(e).append("\n");
            for (int i = 0; i < 5; i++) {
                stringBuilder.append(e.getStackTrace()[i]).append("\n");
            }
            event.getInteraction().reply("```" + stringBuilder + "```").queue();
        }
    }
}
