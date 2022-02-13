package com.koolie.bot.richter.commands;

import com.koolie.bot.richter.MusicUtil.MusicManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDAInfo;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.lang.management.ManagementFactory;

public class Stats implements TextCommand {
    public Stats() {}

    @NotNull
    @Override
    public String getName() {
        return "Stats";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Shows the stats";
    }

    @NotNull
    @Override
    public String getOperator() {
        return "stats";
    }

    @Nullable
    @Override
    public String[] getAliases() {
        return new String[] { "stat" };
    }

    @Override
    public void execute(Message message) {
        Runtime runtime = Runtime.getRuntime();
        long usedMem = runtime.totalMemory() - runtime.freeMemory();
        long maxMem = runtime.maxMemory();
        int threads = ManagementFactory.getThreadMXBean().getThreadCount();

        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(Color.BLUE);
        eb.setTitle("Stats");
        eb.addField("Servers", message.getJDA().getGuilds().size() + "", true);
        eb.addField("Memory Usage", usedMem / 1024 / 1024 + "MB/" + maxMem / 1024 / 1024 + "MB", true);
        eb.addField("Threads", threads + "", true);

        eb.addField("Active Music Players", MusicManager.getActivePlayers() + "", true);
        eb.addField("JDA Version", String.format("%s.%s.%s-%s", JDAInfo.VERSION_MAJOR, JDAInfo.VERSION_MINOR, JDAInfo.VERSION_MINOR, JDAInfo.VERSION_CLASSIFIER), true);

        message.replyEmbeds(eb.build()).queue();
    }
}
