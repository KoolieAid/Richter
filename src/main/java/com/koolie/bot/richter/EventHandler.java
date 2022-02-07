package com.koolie.bot.richter;

import com.koolie.bot.richter.MusicUtil.GMManager;
import com.koolie.bot.richter.MusicUtil.MusicManagerFactory;
import com.koolie.bot.richter.commands.Shutdown;
import com.koolie.bot.richter.commands.*;
import com.koolie.bot.richter.commands.music.Queue;
import com.koolie.bot.richter.commands.music.*;
import com.koolie.bot.richter.commands.music.filters.Karaoke;
import com.koolie.bot.richter.commands.music.filters.Rotate;
import com.koolie.bot.richter.threading.ThreadUtil;
import com.koolie.bot.richter.util.BotConfigManager;
import io.sentry.Sentry;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.ShutdownEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.HashMap;
import java.util.Objects;

public class EventHandler extends ListenerAdapter {
    private static final Logger log = LoggerFactory.getLogger("Main Event Handler");
    private static final HashMap<String, Command> commandHashMap = new HashMap<>();
    public static String prefix = BotConfigManager.getPrefix();
    private boolean jdaReady = false;

    public EventHandler() {
        populateCommandHashMap();
    }

    public static HashMap<String, Command> getCommands() {
        return commandHashMap;
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!event.isFromGuild()) {
            event.reply("This command can only be used in a server.").setEphemeral(true).queue();
            return;
        }
        if (!jdaReady) {
            event.reply("I'm still starting up, please wait a bit").queue();
            return;
        }
//
//        System.out.println(event.getCommandIdLong());
//        System.out.println(event.getId());
        if (!commandHashMap.containsKey(event.getName())) {
            event.reply("That command is not implemented yet!").setEphemeral(true).queue();
            return;
        }

        try {
            commandHashMap.get(event.getName()).slash(event);
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
            event.reply("```" + stringBuilder + "```").setEphemeral(true).queue();

//            event.getGuild().deleteCommandById(event.getCommandIdLong()).queue();
        }

    }

    @Override
    public void onGuildVoiceJoin(@NotNull GuildVoiceJoinEvent event) {
        //Triggers when the bot is about to leave, but someone rejoins the channel
        if (event.getChannelJoined().getMembers().size() == 2) {
            if (event.getChannelJoined().getMembers().contains(event.getGuild().getSelfMember())) {
                if (MusicManagerFactory.isLeaving(event.getGuild())) {
                    MusicManagerFactory.cancelLeave(event.getGuild());
                }
            }
        }
    }

    @Override
    public void onGuildVoiceLeave(@NotNull GuildVoiceLeaveEvent event) {
        //Triggers when the bot leaves a voice channel
        if (event.getMember().getIdLong() == event.getJDA().getSelfUser().getIdLong()) {
            MusicManagerFactory.onLeave(event.getGuild());
        }

        //TODO: Figure out what to do when it plays another track since trackStart() cancels the leave
        //Triggers when someone leaves the bot alone in a channel
        if (event.getChannelLeft().getMembers().size() == 1) {
            if (event.getChannelLeft().getMembers().get(0).getIdLong() == event.getJDA().getSelfUser().getIdLong()){
//                MusicManagerFactory.timerLeave(event.getGuild());
                event.getGuild().getAudioManager().closeAudioConnection();
                GMManager manager = MusicManagerFactory.getGuildMusicManager(event.getGuild());
                EmbedBuilder embedBuilder = new EmbedBuilder();
                embedBuilder.setDescription("I've left the channel due to inactivity");
                manager.eventListener.sendMessageToChannel(embedBuilder.build());
            }
        }
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        if (!event.getMessage().isFromGuild()) return;
        if (!jdaReady) {
            event.getMessage().reply("I'm still starting up, please wait a bit").queue();
            return;
        }
        Message message = event.getMessage();

        String[] args = message.getContentRaw().split(" ");

        if (!args[0].startsWith(prefix)) return;
        String cmd = args[0].replaceFirst(prefix, "");

        if (commandHashMap.get(cmd) == null) return;
        try {
            commandHashMap.get(cmd).execute(event);
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
            message.reply("```" + stringBuilder + "```").queue();
        }
    }

    @Override
    public void onGuildJoin(@NotNull GuildJoinEvent event) {
        EmbedBuilder ebuilder = new EmbedBuilder();
        ebuilder.setTitle("Thank you for inviting me!")
                .setColor(Color.CYAN)
                .setDescription("""
                        Hi. Looks like you found me by chance. (i guess??)
                        Anyway, I'm just a general purpose bot that was originally created for a private server.

                        Things that I support: playing music, spotify playlist, YT search, and 8D audio filter
                                                
                        I'm usually just used for my music, but some people also use my `team` feature, where I randomly assign people into a team.
                                                
                        My prefix is `=`. To get started, just type `=help`.
                                                
                        If you know **Chad Thundercock**, say hi to him for me. (he's like my creator)
                        Don't forget to use `=donate`
                        """);
        Objects.requireNonNull(event.getGuild().getDefaultChannel()).sendMessageEmbeds(ebuilder.build()).queue();
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        jdaReady = true;
        JDA jda = event.getJDA();
        int guildCount = jda.getGuilds().size();
        log.info(jda.getShardInfo() + " is now listening to " + guildCount + " guilds");

        event.getJDA().upsertCommand(Commands.context(net.dv8tion.jda.api.interactions.commands.Command.Type.MESSAGE, "Add to queue")).queue();
//        event.getJDA().getGuildById("931181353507123242")
//                ("testing")
//                .queue(command -> {
//                    event.getJDA().getGuildById("931181353507123242").deleteCommandById(command.getIdLong()).queue();
//                });
        Commands.slash("play", "Plays music for you")
                .addOption(OptionType.STRING, "query", "The song that you're looking for", true, true);
    }

    @Override
    public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent event) {

    }

    @Override
    public void onMessageContextInteraction(@NotNull MessageContextInteractionEvent event) {
        log.debug(event.getInteraction().getName());
        if (event.getInteraction().getName().equals("Add to queue")) {
            commandHashMap.get("play").onContext(event);
        }
    }

    @Override
    public void onShutdown(@NotNull ShutdownEvent event) {
        ThreadUtil.shutDownAll();
    }

    /**
     * Populates the Hashmap of commands on the variables
     *
     * @author Erik Go
     */
    //TODO: Use reflection to get all commands
    /*
    Resource: https://github.com/freya022/BotCommands/blob/2.2.0/src/main/java/com/freya02/botcommands/internal/utils/ReflectionUtils.java#L34-L77
     */
    private void populateCommandHashMap() {
        commandHashMap.put("upgrade", new Upgrade());
        commandHashMap.put("downgrade", new Downgrade());
        commandHashMap.put("concertMode", new ConcertMode());
        //commandHashMap.put("prefix", new Prefix());
        commandHashMap.put("donate", new Donate());
        commandHashMap.put("reason", new Reason());
        commandHashMap.put("lockChannel", new LockChannel());
        commandHashMap.put("team", new Team());
        commandHashMap.put("nick", new Nick());
        commandHashMap.put("play", new Play());
        commandHashMap.put("disconnect", new Disconnect());
        commandHashMap.put("skip", new Skip());
        commandHashMap.put("pause", new Pause());
        commandHashMap.put("resume", new Resume());
        commandHashMap.put("nowplaying", new NowPlaying());
        commandHashMap.put("queue", new Queue());
        commandHashMap.put("shuffle", new Shuffle());
        commandHashMap.put("volume", new Volume());
        commandHashMap.put("clear", new Clear());
        commandHashMap.put("stop", new Stop());
        commandHashMap.put("help", new Help());
        commandHashMap.put("lyrics", new LyricsCommand());
        commandHashMap.put("8d", new Rotate());
        commandHashMap.put("shutdown", new Shutdown());
        commandHashMap.put("karaoke", new Karaoke());
        commandHashMap.put("invite", new Invite());
        commandHashMap.put("removequeue", new RemoveQueue());
        commandHashMap.put("repeat", new Repeat());
        commandHashMap.put("stats", new Stats());
        commandHashMap.put("eval", new Eval());
        commandHashMap.put("activity", new Activity());
        commandHashMap.put("playnext", new PlayNext());

        //Aliases like bruh this way is so stupid
        commandHashMap.put("p", commandHashMap.get("play"));
        commandHashMap.put("dc", commandHashMap.get("disconnect"));
        commandHashMap.put("leave", commandHashMap.get("disconnect"));
        commandHashMap.put("np", commandHashMap.get("nowplaying"));
        commandHashMap.put("q", commandHashMap.get("queue"));
        commandHashMap.put("rq", commandHashMap.get("removequeue"));
        commandHashMap.put("loop", commandHashMap.get("repeat"));
        commandHashMap.put("n", commandHashMap.get("skip"));
        commandHashMap.put("next", commandHashMap.get("skip"));
        commandHashMap.put("pn", commandHashMap.get("playnext"));
    }
}
