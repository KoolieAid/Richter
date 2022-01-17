package com.koolie.bot.richter;

import com.koolie.bot.richter.objects.guild.GuildConfig;

import java.util.HashMap;

public class GuildConfigManager {
    private static final HashMap<Long, GuildConfig> guildConfigs = new HashMap<>();
    //put static methods use database, abstract it so I don't have to deal with shit later on

    public static GuildConfig getConfig(Long guildId) {
        GuildConfig config = guildConfigs.get(guildId);
        if (config == null) {
            config = new GuildConfig(guildId);
            guildConfigs.put(guildId, config);
        }

        return config;
    }

}
