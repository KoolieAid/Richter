package com.koolie.bot.richter.commands.music;

import com.koolie.bot.richter.commands.Interfaces.TextCommand;
import com.koolie.bot.richter.objects.guild.GuildConfig;
import net.dv8tion.jda.api.entities.Message;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SkipSegment implements TextCommand {
    @NotNull
    @Override
    public String getName() {
        return "Segment Skipper";
    }

    @NotNull
    @Override
    public String getDescription() {
        return """
                Ever wanted to skip those non music parts of a music video? This command will skip that for you.
                This command only works in videos that have segments in the [SponsorBlock](https://sponsor.ajay.app) database.
                """;
    }

    @NotNull
    @Override
    public CommandType getCommandType() {
        return CommandType.Music;
    }

    @NotNull
    @Override
    public String getOperator() {
        return "skipsegments";
    }

    @Nullable
    @Override
    public String[] getAliases() {
        return new String[]{"skipsegment", "segmentskipping", "segmentskip", "sponsorblock", "blocksponsors"};
    }

    @Override
    public void execute(@NotNull Message message) {
        GuildConfig config = GuildConfig.of(message.getGuild().getIdLong());

        if (!config.isSegmentSkippingEnabled()) {
            config.setSegmentSkipping(true);

            message.reply("Segment skipping is now **enabled**. Take note: some videos won't work and won't be accurate").queue();
            return;
        }

        config.setSegmentSkipping(false);
        message.reply("Segment skipping is now **disabled**").queue();
    }
}
