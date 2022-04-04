package com.koolie.bot.richter.commands;

import com.koolie.bot.richter.commands.Interfaces.TextCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.awt.*;

public class Eval implements TextCommand {
    static String imports = """
            import net.dv8tion.jda.api.JDA;
            import net.dv8tion.jda.api.managers.*;
            import java.util.*;
            import java.math.*;
            import net.dv8tion.jda.api.entities.*;
            import com.koolie.bot.richter.MusicUtil.*;
            """;
    private final ScriptEngine engine;

    public Eval() {
        engine = new ScriptEngineManager().getEngineByName("groovy");
    }

    @NotNull
    @Override
    public String getName() {
        return "Evaluate";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Evaluates code. ONLY FOR OWNER";
    }

    @NotNull
    @Override
    public CommandType getCommandType() {
        return CommandType.Power;
    }

    @NotNull
    @Override
    public String getOperator() {
        return "eval";
    }

    @Override
    public void execute(@NotNull Message message) {
        if (!message.getAuthor().getId().equals("343613515220647957") && !message.getAuthor().getId().equals("854248475876655104")) {
            message.getChannel().sendMessage("Only the owner can do that! This incident will be reported.").queue();
            return;
        }
        message.getChannel().sendTyping().queue();

        if (message.getContentRaw().split(" ").length < 2) {
            message.reply("""
                    ———————————No code?———————————
                    ⠀⣞⢽⢪⢣⢣⢣⢫⡺⡵⣝⡮⣗⢷⢽⢽⢽⣮⡷⡽⣜⣜⢮⢺⣜⢷⢽⢝⡽⣝
                    ⠸⡸⠜⠕⠕⠁⢁⢇⢏⢽⢺⣪⡳⡝⣎⣏⢯⢞⡿⣟⣷⣳⢯⡷⣽⢽⢯⣳⣫⠇
                    ⠀⠀⢀⢀⢄⢬⢪⡪⡎⣆⡈⠚⠜⠕⠇⠗⠝⢕⢯⢫⣞⣯⣿⣻⡽⣏⢗⣗⠏⠀
                    ⠀⠪⡪⡪⣪⢪⢺⢸⢢⢓⢆⢤⢀⠀⠀⠀⠀⠈⢊⢞⡾⣿⡯⣏⢮⠷⠁⠀⠀
                    ⠀⠀⠀⠈⠊⠆⡃⠕⢕⢇⢇⢇⢇⢇⢏⢎⢎⢆⢄⠀⢑⣽⣿⢝⠲⠉⠀⠀⠀⠀
                    ⠀⠀⠀⠀⠀⡿⠂⠠⠀⡇⢇⠕⢈⣀⠀⠁⠡⠣⡣⡫⣂⣿⠯⢪⠰⠂⠀⠀⠀⠀
                    ⠀⠀⠀⠀⡦⡙⡂⢀⢤⢣⠣⡈⣾⡃⠠⠄⠀⡄⢱⣌⣶⢏⢊⠂⠀⠀⠀⠀⠀⠀
                    ⠀⠀⠀⠀⢝⡲⣜⡮⡏⢎⢌⢂⠙⠢⠐⢀⢘⢵⣽⣿⡿⠁⠁⠀⠀⠀⠀⠀⠀⠀
                    ⠀⠀⠀⠀⠨⣺⡺⡕⡕⡱⡑⡆⡕⡅⡕⡜⡼⢽⡻⠏⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
                    ⠀⠀⠀⠀⣼⣳⣫⣾⣵⣗⡵⡱⡡⢣⢑⢕⢜⢕⡝⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
                    ⠀⠀⠀⣴⣿⣾⣿⣿⣿⡿⡽⡑⢌⠪⡢⡣⣣⡟⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
                    ⠀⠀⠀⡟⡾⣿⢿⢿⢵⣽⣾⣼⣘⢸⢸⣞⡟⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
                    ⠀⠀⠀⠀⠁⠇⠡⠩⡫⢿⣝⡻⡮⣒⢽⠋⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
                    —————————————————————————————
                    """).queue();
            return;
        }

        User author = message.getAuthor();
        JDA jda = message.getJDA();
        TextChannel channel = message.getTextChannel();
        engine.put("guild", channel.getGuild());
        engine.put("author", author);
        engine.put("member", message.getMember());
        engine.put("channel", channel);
        engine.put("jda", jda);
        engine.put("api", jda);
        engine.put("message", message);

        EmbedBuilder eb = new EmbedBuilder();
        eb.setFooter("Evaluated by " + author.getName(), author.getAvatarUrl());
        StringBuilder toEval = new StringBuilder();
        toEval.append(imports).append("\n");

        String code = message.getContentStripped().split(" ", 2)[1];
        toEval.append(code);
        try {
            Object evaluated = engine.eval(toEval.toString());
            if (evaluated == null) {
                return;
            }
            eb.setColor(Color.GREEN);
            eb.setDescription("```" + evaluated + "```");
        }
        catch (ScriptException ex) {
            eb.setColor(Color.RED);
            eb.setDescription("```" + ex.getMessage() + "```");
        }

        message.replyEmbeds(eb.build()).queue();
    }
}
