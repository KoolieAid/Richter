package com.koolie.bot.richter.commands;

import com.koolie.bot.richter.commands.Interfaces.SlashCommand;
import com.koolie.bot.richter.objects.Ignored;
import io.sentry.Sentry;
import io.sentry.UserFeedback;
import io.sentry.protocol.SentryId;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import org.jetbrains.annotations.NotNull;

public class Feedback implements SlashCommand {
    @NotNull
    @Override
    public String getName() {
        return "Feedback";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Brings out a form for you to input feedback. Feedbacks are not stored locally, but stored in Sentry. It is also allowed to put suggestions";
    }

    @NotNull
    @Override
    public CommandType getCommandType() {
        return CommandType.General;
    }

    @NotNull
    @Override
    public String getEffectiveCommand() {
        return "feedback";
    }

    @Override
    public void onSlash(SlashCommandInteractionEvent event) {

        TextInput name = TextInput.create("name", "Name", TextInputStyle.SHORT)
                .setPlaceholder("Your Name")
                .setRequired(false)
                .setRequiredRange(3, 20)
                .setValue("Anonymous")
                .build();

        TextInput email = TextInput.create("email", "Email", TextInputStyle.SHORT)
                .setPlaceholder("Your Email")
                .setRequiredRange(10, 50)
                .setRequired(false)
                .setValue("anonymous@email.com")
                .build();

        TextInput feedback = TextInput.create("feedback", "Feedback", TextInputStyle.PARAGRAPH)
                .setPlaceholder("Your Feedback")
                .setRequiredRange(10, 500)
                .setRequired(true)
                .build();

        Modal m = Modal.create(event.getId(), "Feedback Form")
                .addActionRows(ActionRow.of(name), ActionRow.of(email), ActionRow.of(feedback))
                .build();

        event.replyModal(m).queue(v -> {
            event.getJDA().addEventListener(new FeedbackModal(event.getId()));
        });

    }

    @Ignored
    private class FeedbackModal extends ListenerAdapter {
        private final String id;

        public FeedbackModal(String id) {
            this.id = id;
        }

        @Override
        public void onModalInteraction(@NotNull ModalInteractionEvent event) {
            if (!event.getModalId().equals(id)) return;

            event.deferReply(true).queue();

            InteractionHook hook = event.getHook();

            String name = event.getInteraction().getValue("name").getAsString();

            String email = event.getInteraction().getValue("email").getAsString();

            String feedback = event.getInteraction().getValue("feedback").getAsString();

            SentryId sentryId = Sentry.captureMessage("Raw Feedback");
            
            UserFeedback userFeedback = new UserFeedback(sentryId);
            userFeedback.setName(name);
            userFeedback.setEmail(email);
            userFeedback.setComments(feedback);

            Sentry.captureUserFeedback(userFeedback);

            hook.editOriginal("Thank you for your feedback, " + name + "! We will look into it as soon as possible.").queue();
        }
    }
}
