package com.koolie.bot.richter.commands;

import com.koolie.bot.richter.commands.Interfaces.Command;
import com.koolie.bot.richter.commands.Interfaces.TextCommand;
import net.dv8tion.jda.api.entities.Message;
import org.jetbrains.annotations.NotNull;

public class Donate implements TextCommand {
    public Donate() {
    }

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
    public void execute(@NotNull Message message) {
        message.reply("""
                Please send some money over at: https://www.paypal.com/paypalme/KoolieAid
                GCASH: 09777708608; PayMaya(Better): 09777708608
                I also accept crypto: 0xaeD29e384F26A42d17EF9D4273C86c0De81C63d8
                """).queue();
    }
}
