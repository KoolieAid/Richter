package com.koolie.bot.richter.commands;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.koolie.bot.richter.commands.Interfaces.TextCommand;
import com.koolie.bot.richter.util.BotConfigManager;
import com.theokanning.openai.OpenAiService;
import com.theokanning.openai.completion.CompletionRequest;
import io.sentry.Sentry;
import net.dv8tion.jda.api.entities.Message;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;

public class ChatAI implements TextCommand {
    public ChatAI() {
        service = new OpenAiService(BotConfigManager.getOpenAIKey(), Duration.ofSeconds(10));
    }

    @NotNull
    @Override
    public String getName() {
        return "Chat";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Automated Response from OpenAI ***BETA FEATURE***";
    }

    @NotNull
    @Override
    public CommandType getCommandType() {
        return CommandType.Other;
    }

    @NotNull
    @Override
    public String getOperator() {
        return "ask";
    }

    @Nullable
    @Override
    public String[] getAliases() {
        return null;
    }

    private OpenAiService service;

    @Override
    public void execute(@NotNull Message message) {
        var content = message.getContentRaw().split("", 2)[1];


        var request = CompletionRequest.builder()
                .echo(true)
                .model("text-davinci-003")
                .prompt(content)
                .user("testing")
                .build();

//        try {
//            var out = service.createCompletion(request).getChoices().get(0);
//
//            message.reply(out.getText()).queue();
//        } catch (Exception e){
//            e.printStackTrace();
//
//        }
        message.getChannel().sendTyping().queue();
        
        try {
            HttpURLConnection con = (HttpURLConnection) new URL("https://api.openai.com/v1/completions").openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Authorization", "Bearer " + BotConfigManager.getOpenAIKey());

            JsonObject data = new JsonObject();

            data.addProperty("model", "text-davinci-003");
            data.addProperty("prompt", content);
            data.addProperty("max_tokens", 4000);
            data.addProperty("temperature", 1.0);

            con.setDoOutput(true);
            con.getOutputStream().write(data.toString().getBytes());

            String output = new BufferedReader(new InputStreamReader(con.getInputStream())).lines()
                    .reduce((a, b) -> a + b).get();

            var o = JsonParser.parseString(output)
                    .getAsJsonObject()
                    .getAsJsonArray("choices")
                    .get(0).getAsJsonObject()
                    .get("text").getAsString();

            message.reply(o).queue();
        } catch (IOException e){
            if (e.getMessage().contains("500")) {
                message.reply("Seems like the server is overloaded. Check back later!").queue();
                return;
            }
            message.reply("It appears that the command malfunctioned. As this is just a beta feature, don't expect perfect outputs").queue();
            Sentry.captureException(e);
        }

    }
}
