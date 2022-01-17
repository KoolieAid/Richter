package com.koolie.bot.richter.commands;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class Donate extends Command {
    public Donate() {
        this.setName("Donate");
        this.setDescription("Sends links for donation");
        this.setCommandType(commandType.Other);
    }

    @Override
    public void execute(MessageReceivedEvent event) {
        event.getMessage().reply("""
                Please send some money over at: https://www.paypal.com/paypalme/KoolieAid
                GCASH: 09777708608; PayMaya(Better): 09777708608
                I also accept crypto: 0xaeD29e384F26A42d17EF9D4273C86c0De81C63d8
                """).queue();
    }
}
