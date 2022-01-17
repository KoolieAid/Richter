package com.koolie.bot.richter.objects.guild;

public class GuildConfig {
    private final Long guildId;
    private String concertModeChannelId;
    private Long concertModeMCId;
    private String lockChannelId;
    private Long[] lockChannelMembers;

    public GuildConfig(Long id) {
        guildId = id;
    }


}
