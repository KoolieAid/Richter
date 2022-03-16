package com.koolie.bot.richter.objects.guild;

import com.koolie.bot.richter.util.BotConfigManager;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;

public class GuildConfig {
    private static final Logger logger;

    private static final HashMap<Long, GuildConfig> guildConfigs = new HashMap<>();
    private @Getter final Long guildId;
    private @Getter int playerVolume;
    private @Getter String prefix;
    private @Getter String concertModeChannelId;
    private @Getter Long concertModeMCId;
    private @Getter String lockChannelId;
    private @Getter long[] lockChannelMembers;

    static {
        logger = LoggerFactory.getLogger("Guild Config");
    }

    private static Connection connection = null;

    public static void loadDatabase() {
        try {
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection(BotConfigManager.getDB_URL(), BotConfigManager.getDB_USER(), BotConfigManager.getDB_PASS());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            logger.error("Error connecting to database", e);
        }
    }

    public static void closeDatabase() {
        try {
            connection.close();
        } catch (SQLException e) {
            logger.error("Error closing database connection", e);
        }
    }

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

}
