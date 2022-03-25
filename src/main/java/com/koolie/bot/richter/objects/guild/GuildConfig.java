package com.koolie.bot.richter.objects.guild;

import com.koolie.bot.richter.util.BotConfigManager;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.HashMap;

public class GuildConfig {
    private static final Logger logger;

    private static final HashMap<Long, GuildConfig> guildConfigs = new HashMap<>();
    private @Getter final Long guildId;
    private @Getter int playerVolume;
    private @Getter String prefix;

    static {
        logger = LoggerFactory.getLogger("Guild Config");
    }

    private @Getter static Connection connection = null;

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

    private GuildConfig(Long id) {
        guildId = id;

        try {
            Statement statement = connection.createStatement();

            String query = " SELECT * FROM servers WHERE server_id = " + id + ";";

            ResultSet result = statement.executeQuery(query);

            if (!result.next()){
                statement.execute("INSERT INTO servers (server_id) VALUES (" + id + ");");

                prefix = BotConfigManager.getPrefix();
                playerVolume = 20;

                result.close();
                statement.close();
                return;
            }

            long l = result.getLong("server_id");
            System.out.println("LONG ID " + l);

            int playerVol = result.getInt("player_volume");
            if (playerVol == 0) {
                this.playerVolume = 20;
            } else {
                this.playerVolume = playerVol;
            }


            String prefix = result.getString("prefix");
            if (prefix == null) {
                this.prefix = BotConfigManager.getPrefix();
            } else {
                this.prefix = prefix;
            }

            result.close();
            statement.close();
        } catch (SQLException e) {
            logger.error("Error creating statement", e);
            playerVolume = 20;
            prefix = BotConfigManager.getPrefix();
        }
    }

    public static GuildConfig of(Long guildId) {
        GuildConfig config = guildConfigs.get(guildId);
        if (config == null) {
            config = new GuildConfig(guildId);
            guildConfigs.put(guildId, config);
        }

        return config;
    }

    public void setPlayerVolume(int volume) {
        playerVolume = volume;

        try {
            Statement statement = connection.createStatement();

            String query = "UPDATE servers SET player_volume = " + volume + " WHERE server_id = " + guildId + ";";

            statement.execute(query);
            statement.close();
        } catch (SQLException e) {
            logger.error("Error creating statement", e);
        }
    }

}
