package com.koolie.bot.richter.commands;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import static com.koolie.bot.richter.Richter.shardManager;

public class Shutdown extends Command {
    public Shutdown() {
        setName("shutdown");
        setDescription("Shuts down the bot. Only the owner can use this.");
        setCommandType(commandType.Power);
    }

    @Override
    public void execute(MessageReceivedEvent event) {
        event.getJDA().retrieveApplicationInfo().queue(applicationInfo -> {
            if (applicationInfo.getOwner() != event.getAuthor()) {
                event.getMessage().reply("Only the owner can do that! This incident will be reported.").queue();
                return;
            }
            event.getMessage().reply("Shutting down...").queue();
            shardManager.shutdown();
        });
    }
}
