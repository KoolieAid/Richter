package com.koolie.bot.richter.objects.guild;

import java.util.HashMap;

public class GuildConfig {
    private final Long guildId;
    private String prefix;
    private String concertModeChannelId;
    private Long concertModeMCId;
    private String lockChannelId;
    private Long[] lockChannelMembers;

    private static final HashMap<Long, GuildConfig> guildConfigs = new HashMap<>();
    //put static methods use database, abstract it so I don't have to deal with shit later on

    public GuildConfig(Long id) {
        guildId = id;
    }

    public static GuildConfig of(Long guildId) {
        GuildConfig config = guildConfigs.get(guildId);
        if (config == null) {
            config = new GuildConfig(guildId);
            guildConfigs.put(guildId, config);
        }

        return config;
    }

    public String getPrefix(){
        return prefix;
    }

}
