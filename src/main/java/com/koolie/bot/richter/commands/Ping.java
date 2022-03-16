package com.koolie.bot.richter.commands;

import com.koolie.bot.richter.commands.Interfaces.TextCommand;
import net.dv8tion.jda.api.entities.Message;
import org.jetbrains.annotations.NotNull;

public class Ping implements TextCommand {
    @NotNull
    @Override
    public String getName() {
        return "ping";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Returns the ping of the bot";
    }

    @NotNull
    @Override
    public CommandType getCommandType() {
        return CommandType.General;
    }

    @NotNull
    @Override
    public String getOperator() {
        return "ping";
    }

    @Override
    public void execute(@NotNull Message message) {
        long gatewayPing = message.getJDA().getGatewayPing();
        message.getJDA().getRestPing().queue(rPing ->{
            long time = System.currentTimeMillis();
            message.reply("Pong!").queue(m -> {
                long ping = System.currentTimeMillis() - time;
                m.editMessage("Pong! `" + ping + "`ms" + "\nGateway Ping: `" + gatewayPing + "`ms\t" + "Rest Ping: `" + rPing + "`ms").queue();
            });
        });
    }
}
