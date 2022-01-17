package com.koolie.bot.richter.commands;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Random;

public class Reason extends Command {
    private final static String[] intro = {
            "Sorry I can't come",
            "Please forgive my absence",
            "This is going to sound crazy but,",
            "Get this:",
            "I can't go because",
            "I know you're going to hate me but,",
            "I was minding my own business, and BOOM!",
            "I feel terrible but,",
            "I regretfully cannot attend,",
            "This is going to sound like an excuse but,"
    };
    private final static String[] scapeGoat = {
            "my nephew",
            "the ghost of Hitler",
            "the Pope",
            "my ex",
            "a high school marching band",
            "Dan Rather",
            "a sad clown",
            "the kid from Air Bud",
            "a professional cricket team",
            "my Tinder date",
            "Brendan"
    };
    private final static String[] delay = {
            "just shit the bed",
            "died in front of me",
            "won't stop telling me knock knock jokes",
            "is having a nervous breakdown",
            "gave me syphilis",
            "poured lemonade in my gas tank",
            "stabbed me",
            "found my box of human teeth",
            "stole my bicycle",
            "posted my nudes on Instagram"
    };
    private final Random random;

    public Reason() {
        this.setName("Reason");
        this.setDescription("Gives a very convincing reason");
        this.setCommandType(commandType.Other);

        random = new Random();
    }

    @Override
    public void execute(MessageReceivedEvent event) {
        event.getMessage().reply(intro[random.nextInt(intro.length)] + " " +
                scapeGoat[random.nextInt(intro.length)] + " " +
                delay[random.nextInt(delay.length)]).queue();
    }
}
