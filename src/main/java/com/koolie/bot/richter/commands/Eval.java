package com.koolie.bot.richter.commands;

import groovy.util.GroovyScriptEngine;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.*;
import java.awt.*;

public class Eval extends Command {
    private ScriptEngine engine;
    public Eval() {
        setName("eval");
        setDescription("Evaluates code");
        setCommandType(commandType.Power);

        engine = new ScriptEngineManager().getEngineByName("groovy");
    }
    static String imports = """
            import net.dv8tion.jda.api.JDA;
            import net.dv8tion.jda.api.managers.*;
            import java.util.*;
            import java.math.*;
            import net.dv8tion.jda.api.entities.*;
            """;

    @Override
    public void execute(MessageReceivedEvent event) {
        engine.put("jda", event.getJDA());
        engine.put("event", event);

        String code = imports + "\n" + event.getMessage().getContentStripped().split(" ", 2)[1];

        try {
            Object output = engine.eval(code);
            event.getMessage().reply("```" + output.toString() + "```").queue();
        } catch (ScriptException e) {
            event.getMessage().reply("```" + e.getMessage() + "```").queue();
        }
    }
}
