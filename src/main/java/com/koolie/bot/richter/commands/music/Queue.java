package com.koolie.bot.richter.commands.music;

import com.koolie.bot.richter.MusicUtil.MusicManager;
import com.koolie.bot.richter.commands.Interfaces.SlashCommand;
import com.koolie.bot.richter.commands.Interfaces.TextCommand;
import com.koolie.bot.richter.objects.Ignored;
import com.koolie.bot.richter.threading.ThreadUtil;
import com.koolie.bot.richter.util.MusicUtil;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.apache.commons.collections4.ListUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Queue implements TextCommand, SlashCommand {
    public Queue() {
    }

    @NotNull
    @Override
    public String getName() {
        return "Queue";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Lists the current queue";
    }

    @NotNull
    @Override
    public CommandType getCommandType() {
        return CommandType.Music;
    }

    @NotNull
    @Override
    public String getOperator() {
        return "queue";
    }

    @Nullable
    @Override
    public String[] getAliases() {
        return new String[]{"q"};
    }

    @NotNull
    @Override
    public String getEffectiveCommand() {
        return "queue";
    }

    @Override
    public void execute(@NotNull Message message) {
        if (!MusicManager.isPresent(message.getGuild())) {
            message.reply("I'm not playing music in the server right now. Play something with `=p`.").queue();
            return;
        }

        MusicManager manager = MusicManager.of(message.getGuild());

        if (manager.eventListener.queue.size() == 0 && manager.audioPlayer.getPlayingTrack() == null) {
            message.reply("There are no tracks queued").queue();
            return;
        }

        int page = 0;
        try {
            page = Integer.parseInt(message.getContentRaw().split(" ")[1]) - 1;
        } catch (Exception ignored) {
        }

        QueueMessage queueMessage = new QueueMessage(message.getJDA(), message.getChannel().getIdLong(), manager.audioPlayer, manager.eventListener.queue, page);
        message.getJDA().addEventListener(queueMessage);

        message.replyEmbeds(queueMessage.makeEmbed())
                .addActionRow((ItemComponent)queueMessage.getActionRow())
                .queue(m -> queueMessage.setMessageId(m.getId()),
                        e -> message.getJDA().removeEventListener(queueMessage));
    }

    @Override
    public void onSlash(SlashCommandInteractionEvent event) {
        if (!MusicManager.isPresent(event.getGuild())) {
            event.reply("I'm not playing music in the server right now. Play something with `=p`.").setEphemeral(true).queue();
            return;
        }

        MusicManager manager = MusicManager.of(event.getGuild());

        if (manager.eventListener.queue.size() == 0 && manager.audioPlayer.getPlayingTrack() == null) {
            event.reply("There are no tracks queued").setEphemeral(true).queue();
            return;
        }

        int page = 0;
        if (event.getOption("page") != null) {
            page = event.getOption("page").getAsInt() - 1;
        }

        QueueMessage queueMessage = new QueueMessage(event.getJDA(), event.getChannel().getIdLong(), manager.audioPlayer, manager.eventListener.queue, page);
        event.getJDA().addEventListener(queueMessage);

        event.replyEmbeds(queueMessage.makeEmbed())
                .addActionRow((ItemComponent)queueMessage.getActionRow())
                .flatMap(InteractionHook::retrieveOriginal)
                .queue(m -> queueMessage.setMessageId(m.getId()),
                        e -> event.getJDA().removeEventListener(queueMessage));
    }

    @Ignored
    private class QueueMessage extends ListenerAdapter {
        private final Deque<AudioTrack> queueReference;
        private final ActionRow actionRow; // To set all buttons disabled
        private final AudioPlayer audioPlayer; // To get playing track

        //Both needed in case a user doesn't press a button, thus, no Interaction Hook given
        private final JDA jda;
        private final long channelId;

        private String messageId; // can't final it since Queue#execute is separate
        private int currentPage = 0;
        private InteractionHook hook; // can't final bc its null at the start
        private ScheduledFuture<?> invalidateSchedule = null; // To automatically invalidate the queue

        public QueueMessage(JDA jda, long channelId, AudioPlayer player, Deque<AudioTrack> queueReference, int initialPage) {
            this.jda = jda;
            this.channelId = channelId;
            this.audioPlayer = player;
            this.queueReference = queueReference;
            this.currentPage = initialPage;

            actionRow = ActionRow.of(
                    Button.primary("previous", Emoji.fromUnicode("\u25C0")),
                    Button.primary("next", Emoji.fromUnicode("\u25B6")),
                    Button.danger("invalidate", Emoji.fromUnicode("\u2716"))
            );

            scheduleInvalidation(10, TimeUnit.SECONDS);
        }

        @Override
        public void onButtonInteraction(ButtonInteractionEvent event) {
            if (!event.getMessageId().equals(messageId)) return;
            event.deferEdit().queue();
            hook = event.getHook();

            cancelInvalidation();
            if (event.getComponentId().equals("next")) {
                currentPage++;
            } else if (event.getComponentId().equals("previous")) {
                currentPage--;
            } else if (event.getComponentId().equals("invalidate")) {
                invalidate();
                cancelInvalidation();
                return;
            }

            scheduleInvalidation(10, TimeUnit.SECONDS);

            hook.editOriginalEmbeds(makeEmbed()).queue();

        }

        public MessageEmbed makeEmbed() {
            LinkedList<AudioTrack> queue = new LinkedList<>(queueReference);
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setColor(Color.CYAN);

            if (audioPlayer.getPlayingTrack() != null) {
                embedBuilder.setTitle("Now Playing:")
                        .setDescription("[" + audioPlayer.getPlayingTrack().getInfo().title + "](" + audioPlayer.getPlayingTrack().getInfo().uri + ")")
                        .setThumbnail("http://img.youtube.com/vi/" + audioPlayer.getPlayingTrack().getIdentifier() + "/maxresdefault.jpg");
            }

            if (queue.size() == 0) {
                embedBuilder.addField("Songs Queued:", "No songs at the moment :(", false)
                        .setFooter("It feels lonely here...");
                return embedBuilder.build();
            }

            List<List<AudioTrack>> fullList = ListUtils.partition(queue, 10);

            if (currentPage >= fullList.size())
                currentPage = 0;

            if (currentPage < 0)
                currentPage = fullList.size() - 1;

            List<AudioTrack> page = fullList.get(currentPage);

            StringBuilder stringBuilder = new StringBuilder();

            for (int i = 0; i < page.size(); i++) {
                String durationString = MusicUtil.getReadableMusicTime(page.get(i).getDuration());

                stringBuilder.append(i + 1 + (currentPage * 10) + ". " + page.get(i).getInfo().title + " **[" + durationString + "]**")
                        .append(" ").append(page.get(i).getUserData()).append("\n");
            }

            embedBuilder.addField("Songs Queued: ", stringBuilder.toString(), false)
                    .setFooter("Total Songs: " + queue.size() + " | Page: " + (currentPage + 1) + "/" + fullList.size());


            return embedBuilder.build();
        }

        public void setMessageId(String messageId) {
            this.messageId = messageId;
            LoggerFactory.getLogger(getClass()).info("Set message id to " + messageId);
        }

        private void invalidate() {
            if (hook == null) {
                jda.getTextChannelById(channelId).editMessageComponentsById(messageId, Collections.emptyList()).queue();
                jda.removeEventListener(this);
                return;
            }
            hook.editOriginalComponents()
                    .setComponents()
                    .queue();

            hook.getJDA().removeEventListener(this);
        }

        public ActionRow getActionRow() {
            return actionRow;
        }

        private void cancelInvalidation() {
            if (invalidateSchedule == null) return;
            invalidateSchedule.cancel(true);
            invalidateSchedule = null;
        }

        private void scheduleInvalidation(int delay, TimeUnit unit) {
            if (invalidateSchedule != null) return;
            invalidateSchedule = ThreadUtil.getScheduler().schedule(this::invalidate, delay, unit);
            LoggerFactory.getLogger(getClass()).info("Invalidation scheduled");
        }
    }
}