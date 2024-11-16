package ua.wyverno;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.wyverno.config.ConfigLoader;
import ua.wyverno.logger.appenders.QueueLoggerEvents;
import ua.wyverno.notify.discord.DiscordBot;
import ua.wyverno.twitch.DokiDokiMonitoring;
import ua.wyverno.twitch.TwitchService;
import ua.wyverno.twitch.TwitchServiceBuilder;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        ConfigLoader config = ConfigLoader.getInstance();

        TwitchService twitchService = new TwitchServiceBuilder()
                .clientID(config.getProperty("twitch-client-id"))
                .clientSecret(config.getProperty("twitch-client-secret"))
                .CATEGORIES_IDS(config.getProperties("ddlc-category-id", "ddlc-plus-category-id"))
                .build();

        DokiDokiMonitoring monitoring = new DokiDokiMonitoring(twitchService);

        DiscordBot discordBot = new DiscordBot(monitoring, twitchService);

        monitoring.addNotificationListener(discordBot);

        discordBot.addStopCommandHook(() -> {
            twitchService.close();
            monitoring.close();
            if (QueueLoggerEvents.hasInstance()) {
                QueueLoggerEvents.getInstance().close();
            }
        });
    }
}
