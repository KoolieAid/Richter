package com.koolie.bot.richter;

import com.koolie.bot.richter.MusicUtil.MusicManager;
import com.koolie.bot.richter.objects.guild.GuildConfig;
import com.koolie.bot.richter.threading.ThreadUtil;
import com.koolie.bot.richter.util.BotConfigManager;
import com.sedmelluq.discord.lavaplayer.jdaudp.NativeAudioSendFactory;
import io.sentry.Sentry;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;

import javax.security.auth.login.LoginException;
import java.io.FileNotFoundException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class Richter {
    public static ShardManager shardManager;

    public Richter() throws LoginException{
        try {
            BotConfigManager.loadJSON();
        } catch (FileNotFoundException e) {
            System.out.println("No config file found. Please create one.");

            System.out.println("Stacktrace: ");
            e.printStackTrace();
            return;
        }

        shardManager = DefaultShardManagerBuilder.createDefault(BotConfigManager.getToken())
                .addEventListeners(new EventHandler())
                .setAudioSendFactory(new NativeAudioSendFactory())
                .build();

        setOptions();
//        setUpSlash();
        MusicManager.loadSources();
    }

    public static void main(String[] args) throws LoginException {
//        GuildConfig.loadDatabase();

        try {
            new Richter();
        } catch ( LoginException e ) {
            System.out.println("Login failed. Please check your token.");
        }
    }

    public void setOptions() {
        Activity[] activities = {
                Activity.playing("Minecraft"),
                Activity.listening("your moans"),
                Activity.competing("a Valorant Tournament"),
                Activity.listening("your commands"),
                Activity.streaming("music", "https://www.twitch.tv/koolieaid"),
                Activity.listening("your complaints"),
                Activity.playing("with your feelings"),
                Activity.playing("with your mom"),
                Activity.playing("Sex Offender Simulator"),
                Activity.competing("the Ultimate showdown of Ultimate destiny"),
                Activity.playing("Push the P"),
        };

//        ThreadUtil.getScheduler().scheduleAtFixedRate(() -> jda.getPresence().setActivity(activities[new Random().nextInt(activities.length)]),
//                0, 30, TimeUnit.MINUTES);

        ThreadUtil.getScheduler().scheduleAtFixedRate(() -> shardManager.setPresence(OnlineStatus.ONLINE, activities[new Random().nextInt(activities.length)]),
                0, 30, TimeUnit.MINUTES);

        MessageAction.setDefaultMentionRepliedUser(false);

        Sentry.init(options -> {
            options.setDsn(BotConfigManager.getSentryDsn());
            // Set traces_sample_rate to 1.0 to capture 100% of transactions for performance monitoring.
            // We recommend adjusting this value in production.
            options.setTracesSampleRate(1.0);
            // When first trying Sentry it's good to see what the SDK is doing:
            options.setDebug(true);
        });
    }

    public void setUpSlash() {
        CommandData cmData = Commands.slash("upgrade", "Gives the mentioned user the everyone role")
                .addOption(OptionType.USER, "target", "The user to give the everyone role to");


//        CommandData cmData1 = new CommandData("upgrade", "Gives the mentioned user the everyone role");
//        cmData1.addOption(OptionType.USER, "target", "Gives the target the everyone role", true);


        CommandData cmData2 = Commands.slash("downgrade", "Removes the everyone role from specified user")
                .addOption(OptionType.USER, "target", "The user to remove the everyone role from", true);

//        CommandData cmData = new CommandData("downgrade", "Removes the everyone role from specified user");
//        cmData.addOption(OptionType.USER, "target", "Gives the target the everyone role", true);

        //jda.getGuildById("759999287270047745").upsertCommand(cmData).queue();
        //jda.getGuildById("759999287270047745").upsertCommand(cmData1).queue();

        //Testing
//        CommandData cmData3 = new CommandData("test", "testing for subcommand group");
//        SubcommandGroupData subcommandGroupData = new SubcommandGroupData("subcommandgroup", "subcommand group");
//        SubcommandData subcommandData = new SubcommandData("subcommand", "a subcommand");
//        subcommandData.addOption(OptionType.BOOLEAN, "subbool", "subcommand bool");
//
//        subcommandGroupData.addSubcommands(subcommandData);
//        cmData3.addSubcommandGroups(subcommandGroupData);

//        CommandData activitiesData = new CommandData("activity", "Creates a Discord Game Activity");


        OptionData optionData = new OptionData(OptionType.STRING, "game", "The game that you want to play.", true);
        optionData.addChoice("Youtube Together", "880218394199220334");
        optionData.addChoice("Poker", "755827207812677713");
        optionData.addChoice("Betrayal.io", "773336526917861400");
        optionData.addChoice("Fishington.io", "814288819477020702");
        optionData.addChoice("Chess", "832012586023256104");
        optionData.addChoice("Awkword", "879863881349087252");
        optionData.addChoice("Spellcast", "852509694341283871");
        optionData.addChoice("DoodleCrew", "878067389634314250");
        optionData.addChoice("Wordsnack", "879863976006127627");
        optionData.addChoice("Lettertile", "879863686565621790");

        CommandData activitiesData = Commands.slash("activity", "Creates a Discord Game Activity").addOptions(optionData);

//        shardManager.getShardById(0).upsertCommand(activitiesData).queue();
//        shardManager.getGuildById("759999287270047745").upsertCommand(activitiesData).queue();
        shardManager.getGuildById("931181353507123242").upsertCommand(Commands.context(Command.Type.MESSAGE, "testing")).queue();
    }

}


