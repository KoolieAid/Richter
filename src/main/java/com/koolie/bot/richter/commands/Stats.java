package com.koolie.bot.richter.commands;

import com.koolie.bot.richter.MusicUtil.MusicManagerFactory;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDAInfo;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;
import java.lang.management.ManagementFactory;

public class Stats extends Command {
    public Stats() {
        setName("Stats");
        setDescription("Shows the stats");
    }

    @Override
    public void execute(MessageReceivedEvent event) {
        Runtime runtime = Runtime.getRuntime();
        long usedMem = runtime.totalMemory() - runtime.freeMemory();
        long maxMem = runtime.maxMemory();
        int threads = ManagementFactory.getThreadMXBean().getThreadCount();

        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(Color.BLUE);
        eb.setTitle("Stats");
        eb.addField("Servers", event.getJDA().getGuilds().size() + "", true);
        eb.addField("Memory Usage", usedMem / 1024 / 1024 + "MB/" + maxMem / 1024 / 1024 + "MB", true);
        eb.addField("Threads", threads + "", true);

        eb.addField("Active Music Players", MusicManagerFactory.getActivePlayers() + "", true);
        eb.addField("JDA Version", String.format("%s.%s.%s-%s", JDAInfo.VERSION_MAJOR, JDAInfo.VERSION_MINOR, JDAInfo.VERSION_MINOR, JDAInfo.VERSION_CLASSIFIER), true);

        event.getMessage().replyEmbeds(eb.build()).queue();
    }
}
