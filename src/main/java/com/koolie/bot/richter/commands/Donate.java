package com.koolie.bot.richter.commands;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Donate implements TextCommand {
    public Donate() {}

    @NotNull
    @Override
    public String getName() {
        return "Donate";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Sends links for donation";
    }

    @NotNull
    @Override
    public Command.CommandType getCommandType() {
        return CommandType.Other;
    }

    @NotNull
    @Override
    public String getOperator() {
        return "donate";
    }

    @Override
    public void execute(Message message) {
        message.reply("""
                Please send some money over at: https://www.paypal.com/paypalme/KoolieAid
                GCASH: 09777708608; PayMaya(Better): 09777708608
                I also accept crypto: 0xaeD29e384F26A42d17EF9D4273C86c0De81C63d8
                """).queue();
    }
}
