package com.koolie.bot.richter.commands;

import com.koolie.bot.richter.objects.Ignored;
import net.dv8tion.jda.api.entities.Message;
import org.jetbrains.annotations.NotNull;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

@Ignored
public class Eval implements TextCommand {
    static String imports = """
            import net.dv8tion.jda.api.JDA;
            import net.dv8tion.jda.api.managers.*;
            import java.util.*;
            import java.math.*;
            import net.dv8tion.jda.api.entities.*;
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
        return "Evaluates code";
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
    public void execute(Message message) {
        if (!message.getAuthor().getId().equals("343613515220647957")) {
            message.getChannel().sendMessage("You are not allowed to use this command.").queue();
            return;
        }

        engine.put("jda", message.getJDA());
        engine.put("message", message);

        String code = imports + "\n" + message.getContentStripped().split(" ", 2)[1];

        try {
            Object output = engine.eval(code);
            message.reply("```" + output.toString() + "```").queue();
        } catch (ScriptException e) {
            message.reply("```" + e.getMessage() + "```").queue();
        }
    }
}
