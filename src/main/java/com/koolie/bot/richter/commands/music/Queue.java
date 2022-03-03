package com.koolie.bot.richter.commands.music;

import com.koolie.bot.richter.MusicUtil.MusicManager;
import com.koolie.bot.richter.commands.TextCommand;
import com.koolie.bot.richter.objects.Ignored;
import com.koolie.bot.richter.threading.ThreadUtil;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.apache.commons.collections4.ListUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.time.Duration;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Queue implements TextCommand {
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

    @Override
    public void execute(Message message) {
        MusicManager manager = MusicManager.of(message.getGuild());

        if (manager.eventListener.queue.size() == 0 && manager.audioPlayer.getPlayingTrack() == null) {
            message.reply("There are no tracks queued").queue();
            return;
        }

        QueueMessage queueMessage = new QueueMessage(message.getJDA(), message.getChannel().getIdLong(), manager.audioPlayer, manager.eventListener.queue);
        message.getJDA().addEventListener(queueMessage);

        message.replyEmbeds(queueMessage.makeEmbed())
                .setActionRows(queueMessage.getActionRow())
                .queue(m -> queueMessage.setMessageId(m.getId()));
    }

    //TODO: free memory of when bot disconnects from channel and clears in the hashmap of players
    @Ignored
    private class QueueMessage extends ListenerAdapter {
        private String messageId; // can't final it since Queue#execute is separate
        private final Deque<AudioTrack> queueReference;
        private int currentPage = 0;
        private InteractionHook hook; // can't final bc its null at the start
        private final ActionRow actionRow; // To set all buttons disabled
        private final AudioPlayer audioPlayer; // To get playing track
        private ScheduledFuture<?> invalidateSchedule = null; // To automatically invalidate the queue

        //Both needed in case a user doesn't press a button, thus, no Interaction Hook given
        private final JDA jda;
        private final long channelId;

        public QueueMessage(JDA jda, long channelId, AudioPlayer player, Deque<AudioTrack> queueReference) {
            this.jda = jda;
            this.channelId = channelId;
            this.audioPlayer = player;
            this.queueReference = queueReference;

            actionRow = ActionRow.of(
                    Button.primary("previous", Emoji.fromUnicode("\u25C0")),
                    Button.primary("next", Emoji.fromUnicode("\u25B6")),
                    Button.danger("clear", Emoji.fromUnicode("\u2716"))
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
                if (currentPage < 0) currentPage = 0;
            } else if (event.getComponentId().equals("clear")) {
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

            if (currentPage >= fullList.size()) {
                currentPage = fullList.size() - 1;
            }

            List<AudioTrack> page = fullList.get(currentPage);

            StringBuilder stringBuilder = new StringBuilder();

            for (int i = 0; i < page.size(); i++) {

                Duration fullDuration = Duration.ofMillis(page.get(i).getDuration());
                int fullHours = fullDuration.toHoursPart();
                int fullMinutes = fullDuration.toMinutesPart();
                int fullSeconds = fullDuration.toSecondsPart();

                String durationString;
                if (fullHours == 0) {
                    durationString = String.format("%02d:%02d", fullMinutes, fullSeconds);
                } else {
                    durationString = String.format("%02d:%02d:%02d", fullHours, fullMinutes, fullSeconds);
                }

                stringBuilder.append(i + 1 + (currentPage * 10) + ". " + page.get(i).getInfo().title + " **[" + durationString + "]**" + "\n");
            }

            embedBuilder.addField("Songs Queued: ", stringBuilder.toString(), false)
                    .setFooter("Total Songs: " + queue.size() + " | Page: " + (currentPage + 1) + "/" + fullList.size());


            return embedBuilder.build();
        }

        public void setMessageId(String messageId) {
            this.messageId = messageId;
        }

        public void invalidate() {
            if (hook == null) {
                jda.getTextChannelById(channelId).editMessageComponentsById(messageId, actionRow.asDisabled()).queue();
                jda.removeEventListener(this);
                return;
            }
            hook.editOriginalComponents()
                    .setActionRows(actionRow.asDisabled())
                    .queue();

            hook.getJDA().removeEventListener(this);
        }

        public ActionRow getActionRow() {
            return actionRow;
        }

        public void cancelInvalidation() {
            if (invalidateSchedule == null) return;
            invalidateSchedule.cancel(true);
            invalidateSchedule = null;
        }

        public void scheduleInvalidation(int delay, TimeUnit unit) {
            if (invalidateSchedule != null) return;
            invalidateSchedule = ThreadUtil.getScheduler().schedule(this::invalidate, delay, unit);
            LoggerFactory.getLogger(getClass()).info("Invalidation scheduled");
        }
    }
}