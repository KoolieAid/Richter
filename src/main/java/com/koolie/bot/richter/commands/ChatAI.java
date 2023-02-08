package com.koolie.bot.richter.commands;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.koolie.bot.richter.commands.Interfaces.TextCommand;
import com.theokanning.openai.OpenAiService;
import com.theokanning.openai.completion.CompletionRequest;
import net.dv8tion.jda.api.entities.Message;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;

public class ChatAI implements TextCommand {
    public ChatAI() {
        service = new OpenAiService("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", Duration.ofSeconds(10));
    }

    @NotNull
    @Override
    public String getName() {
        return "Chat";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Automated Response from OpenAi";
    }

    @NotNull
    @Override
    public CommandType getCommandType() {
        return CommandType.Other;
    }

    @NotNull
    @Override
    public String getOperator() {
        return "chat";
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

        try {
            HttpURLConnection con = (HttpURLConnection) new URL("https://api.openai.com/v1/completions").openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Authorization", "Bearer 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");

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
        } catch (Exception e){
            e.printStackTrace();
        }

    }
}
