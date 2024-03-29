package com.koolie.bot.richter;

import com.koolie.bot.richter.MusicUtil.MusicManager;
import com.koolie.bot.richter.commands.Interfaces.*;
import com.koolie.bot.richter.objects.Ignored;
import com.koolie.bot.richter.threading.ThreadUtil;
import com.koolie.bot.richter.util.BotConfigManager;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import io.sentry.Sentry;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.events.session.ShutdownEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.UnavailableGuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class EventHandler extends ListenerAdapter {
    private static final Logger log = LoggerFactory.getLogger("Main Event Handler");
    private static final HashMap<String, TextCommand> textCommands = new HashMap<>();
    private static final HashMap<String, String> aliases = new HashMap<>();
    private static final HashMap<String, SlashCommand> slashCommands = new HashMap<>();
    private static final HashMap<String, ContextCommand> contextCommands = new HashMap<>();
    private final HashMap<String, AutoSlashCommand> autoSlashCommands = new HashMap<>();
    public static String prefix = BotConfigManager.getPrefix();
    private boolean jdaReady = false;

    public EventHandler() {
        generateCommands();
    }

    public static HashMap<String, TextCommand> getCommands() {
        return textCommands;
    }

    public static HashMap<String, String> getAliases() {
        return aliases;
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!event.isFromGuild()) {
            event.reply("Woah there! I'm not a Discord mod to respond to you in DMs!").setEphemeral(true).queue();
            return;
        }
        if (!jdaReady) {
            event.reply("I'm still starting up, please wait a bit").queue();
            return;
        }

        if (!slashCommands.containsKey(event.getName())) {
            event.reply("That command is not implemented yet!").setEphemeral(true).queue();
            return;
        }

        try {
            slashCommands.get(event.getName()).onSlash(event);
        } catch (InsufficientPermissionException e) {
            event.reply("Seems like I don't have the necessary permission for that!\n"
                    + "I needed `" + e.getPermission().getName() + "`").setEphemeral(true).queue();
        } catch (Exception e) {
            log.error("Exception in executing slash command", e);
            Sentry.captureException(e, event.getName());
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(e).append("\n");
            for (int i = 0; i < 5; i++) {
                stringBuilder.append(e.getStackTrace()[i]).append("\n");
            }
            event.reply("```" + stringBuilder + "```" + "This is automatically reported to the developer.").setEphemeral(true).queue();

//            event.getGuild().deleteCommandById(event.getCommandIdLong()).queue();
        }

    }

    @Override
    public void onGuildVoiceUpdate(@NotNull GuildVoiceUpdateEvent event) {
        if (event.getChannelJoined() == null)
        {
            onGuildVoiceLeave(event);
            return;
        }

        if (event.getChannelLeft() == null)
            onGuildVoiceJoin(event);
        else
            onGuildVoiceMove(event);
    }

    public void onGuildVoiceJoin(@NotNull GuildVoiceUpdateEvent event) {
        //Triggers when the bot is about to leave, but someone rejoins the channel
        if (event.getChannelJoined().getMembers().size() == 2) {
            if (event.getChannelJoined().getMembers().contains(event.getGuild().getSelfMember())) {
                if (MusicManager.isLeaving(event.getGuild())) {
                    MusicManager.cancelLeave(event.getGuild());
                }
            }
        }
    }

    public void onGuildVoiceLeave(@NotNull GuildVoiceUpdateEvent event) {
        // Triggers when the bot leaves a voice channel
        if (event.getMember().getIdLong() == event.getJDA().getSelfUser().getIdLong()) {
            MusicManager.onLeave(event.getGuild());
        }

        // onTrackStart event has a connection to this code.
        // Triggers when someone leaves the bot alone in a channel
        if (event.getChannelLeft().getMembers().size() == 1) {
            if (event.getChannelLeft().getMembers().get(0).getIdLong() == event.getJDA().getSelfUser().getIdLong()) {
                MusicManager.timerLeave(event.getGuild());
            }
        }
    }

    public void onGuildVoiceMove(@NotNull GuildVoiceUpdateEvent event) {
        // Checker when bot is moved to a lone channel
        if (event.getMember().equals(event.getGuild().getSelfMember())) {
            if (event.getChannelJoined().getMembers().size() == 1) {
                if (event.getChannelJoined().getMembers().size() == 1) {
                    MusicManager.timerLeave(event.getGuild());
                }
            }
        }

        // Checker when user leaves bot alone in channel
        if (event.getChannelLeft().getMembers().size() == 1) {
            if (event.getChannelLeft().getMembers().get(0).equals(event.getGuild().getSelfMember())) {
                MusicManager.timerLeave(event.getGuild());
            }
        }
    }

    @Override
    public void onGuildLeave(@NotNull GuildLeaveEvent event) {
        MusicManager.guildManagerMap.remove(event.getGuild().getIdLong());
    }

    @Override
    public void onUnavailableGuildLeave(@NotNull UnavailableGuildLeaveEvent event) {
        MusicManager.guildManagerMap.remove(event.getGuildIdLong());
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        CompletableFuture.runAsync(() -> {

        if (event.getAuthor().isBot()) return;
        if (!event.getMessage().isFromGuild()) return;
        if (!jdaReady) {
            event.getMessage().reply("I'm still starting up, please wait a bit").queue();
            return;
        }
        Message message = event.getMessage();

        String[] args = message.getContentRaw().split(" ");

        if (!args[0].startsWith(prefix)) return;
        String cmd = args[0].replaceFirst(prefix, "").toLowerCase();

        if (!textCommands.containsKey(cmd) && !aliases.containsKey(cmd)) return;

        try {
            if (textCommands.containsKey(cmd) && !aliases.containsKey(cmd)) {
                textCommands.get(cmd).execute(event.getMessage());
                return;
            }
            textCommands.get(aliases.get(cmd)).execute(event.getMessage());
        } catch (InsufficientPermissionException e) {
            event.getMessage().reply("Seems like I don't have the necessary permission for that!\n"
                    + "I needed `" + e.getPermission().getName() + "`").queue();
        } catch (Exception e) {
            log.error("Exception in executing command", e);
            Sentry.captureException(e, event.getMessage().getContentRaw());
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(e).append("\n");
            for (int i = 0; i < 5; i++) {
                stringBuilder.append(e.getStackTrace()[i]).append("\n");
            }
            message.reply("```" + stringBuilder + "```" + "This is automatically reported to the developer.").queue();
        }


        });
    }

    @Override
    public void onGuildJoin(@NotNull GuildJoinEvent event) {
        EmbedBuilder ebuilder = new EmbedBuilder();
        ebuilder.setTitle("Thank you for inviting me!")
                .setColor(Color.CYAN)
                .setDescription("""
                        Hi. Looks like you found me by chance. (i guess??)
                        Anyway, I'm just a general purpose bot that was originally created for a private server.
                                                
                        I'm usually just used for my music, but some people also use my `team` feature, where I randomly assign people into a team.
                        I also support Discord Game activities, just type /activity on a channel to activate it.
                        The most useful feature I have is the segment skipping feature, which is activated by typing `=skipsegments` on a channel.
                        It automatically skips the non-music parts of a music video, or intermission/credits of a song.
                                                
                        My prefix is `=`. To get started, just type `=help`.
                                                
                        If you know **Chad Thundercock**, say hi to him for me. (he's like my creator)
                        Don't forget to use `=donate`
                        """);
        Objects.requireNonNull(event.getGuild().getDefaultChannel()).asTextChannel().sendMessageEmbeds(ebuilder.build()).queue();
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        jdaReady = true;
        JDA jda = event.getJDA();
        int guildCount = jda.getGuilds().size();
        log.info(jda.getShardInfo() + " is now listening to " + guildCount + " guilds");

//        event.getJDA().upsertCommand(Commands.context(net.dv8tion.jda.api.interactions.commands.Command.Type.MESSAGE, "Add to queue")).queue();
//        event.getJDA()
//                .upsertCommand(Commands.context(net.dv8tion.jda.api.interactions.commands.Command.Type.MESSAGE, "Execute")).queue();
        Commands.slash("play", "Plays music for you")
                .addOption(OptionType.STRING, "query", "The song that you're looking for", true, true);

        //Play Commmand
//        event.getJDA().upsertCommand(
//          Commands.slash("play", "Plays music")
//                  .addOption(OptionType.STRING, "query", "The song that you're looking for", true, true)
//        ).queue();

//        // Queue Command
//        var q = Commands.slash("queue", "Shows the current queue");
//
//        OptionData data = new OptionData(OptionType.INTEGER, "page", "The page you want to see", false, false)
//                .setRequiredRange(Integer.MIN_VALUE, Integer.MAX_VALUE);
//
//        q.addOptions(data);

        // Feedback Command

//        var f = Commands.slash("feedback", "Send feedback to the developers. Suggestions are also welcome!");
//
//        event.getJDA().getGuildById("931181353507123242").upsertCommand(f).queue();

    }

    @Override
    public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent event) {

        try {
            autoSlashCommands.get(event.getInteraction().getName()).completeOption(event.getInteraction());
        } catch (Exception e) {
            Sentry.captureException(e, event.getInteraction().getFocusedOption().getValue());
            log.error("Exception in auto completing command", e);
        }
    }

    @Override
    public void onMessageContextInteraction(@NotNull MessageContextInteractionEvent event) {
        if (!event.isFromGuild()) {
            event.reply("Woah there! I'm not a Discord mod to respond to you in DMs!").setEphemeral(true).queue();
            return;
        }

        try {
            contextCommands.get(event.getInteraction().getName()).onContext(event);
        } catch (InsufficientPermissionException e) {
            event.getInteraction().reply("Seems like I don't have the necessary permission for that!\n"
                    + "I needed `" + e.getPermission().getName() + "`").setEphemeral(true).queue();
        } catch (Exception e) {
            log.error("Exception in executing context command", e);
            Sentry.captureException(e, event.getTarget().getContentRaw());
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(e).append("\n");
            for (int i = 0; i < 5; i++) {
                stringBuilder.append(e.getStackTrace()[i]).append("\n");
            }
            event.getInteraction().reply("```" + stringBuilder + "```").setEphemeral(true).queue();
        }
    }

    @Override
    public void onShutdown(@NotNull ShutdownEvent event) {
        ThreadUtil.shutDownAll();
    }

    /*
    Resource: https://github.com/freya022/BotCommands/blob/2.2.0/src/main/java/com/freya02/botcommands/internal/utils/ReflectionUtils.java#L34-L77
     */

    private void generateCommands() {
        ScanResult result = new ClassGraph()
                .acceptPackages("com.koolie.bot.richter.commands")
                .enableAnnotationInfo()
                .scan(ThreadUtil.getThreadExecutor(), 10);

        result.getAllStandardClasses()
                .filter(clazz -> !clazz.hasAnnotation(Ignored.class))
                .forEach(clazz -> {
                    try {
                        Command instance = (Command) clazz.loadClass().getDeclaredConstructor().newInstance();
                        if (instance instanceof TextCommand) {
                            TextCommand command = (TextCommand) instance;
                            textCommands.put(command.getOperator(), command);
                            String[] aliases = command.getAliases();
                            if (aliases != null) {
                                for (String alias : aliases) {
                                    EventHandler.aliases.put(alias, command.getOperator());
                                }
                            }
                        }
                        if (instance instanceof SlashCommand) {
                            slashCommands.put(((SlashCommand) instance).getEffectiveCommand(), (SlashCommand) instance);
                        }
                        if (instance instanceof ContextCommand) {
                            contextCommands.put(((ContextCommand) instance).getEffectiveName(), (ContextCommand) instance);
                        }
                        if (instance instanceof AutoSlashCommand) {
                            autoSlashCommands.put(((AutoSlashCommand) instance).getEffectiveCommand(), (AutoSlashCommand) instance);
                        }
                    } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                        log.error("Failed to load command", e);
                    }
                });

        result.close();
    }
}
