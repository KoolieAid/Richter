package com.koolie.bot.richter.commands;

import com.koolie.bot.richter.EventHandler;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class Prefix extends Command {
    public Prefix() {
        this.setName("Prefix");
        this.setDescription("Changes the prefix of the bot");
        this.setCommandType(commandType.General);
    }

    @Override
    public void execute(MessageReceivedEvent event) {
        event.getChannel().sendTyping().queue();
        new Thread(() -> {
            Message message = event.getMessage();
            String[] args = event.getMessage().getContentRaw().split(" ");
            if (args.length == 1) {
                message.reply("No prefix provided").queue();
                return;
            }

            EventHandler.prefix = args[1];
            message.reply("Prefix changed to " + EventHandler.prefix).queue();
        }).start();
    }
}
