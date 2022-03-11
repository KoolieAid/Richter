package com.koolie.bot.richter.commands;

import com.koolie.bot.richter.EventHandler;
import com.koolie.bot.richter.commands.Interfaces.TextCommand;
import com.koolie.bot.richter.objects.Ignored;
import net.dv8tion.jda.api.entities.Message;
import org.jetbrains.annotations.NotNull;

@Ignored
public class Prefix implements TextCommand {
    public Prefix() {
    }

    @NotNull
    @Override
    public String getName() {
        return "Prefix";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Changes the prefix of the bot";
    }

    @NotNull
    @Override
    public CommandType getCommandType() {
        return CommandType.General;
    }

    @NotNull
    @Override
    public String getOperator() {
        return "prefix";
    }

    //who the fuck used threads
    @Override
    public void execute(Message event) {
        event.getChannel().sendTyping().queue();
        new Thread(() -> {
            String[] args = event.getContentRaw().split(" ");
            if (args.length == 1) {
                event.reply("No prefix provided").queue();
                return;
            }

            EventHandler.prefix = args[1];
            event.reply("Prefix changed to " + EventHandler.prefix).queue();
        }).start();
    }
}
