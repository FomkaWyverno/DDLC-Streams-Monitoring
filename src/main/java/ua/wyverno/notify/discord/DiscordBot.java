package ua.wyverno.notify.discord;

import com.github.twitch4j.helix.domain.Stream;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.wyverno.config.ConfigLoader;
import ua.wyverno.notify.Notification;
import ua.wyverno.twitch.DokiDokiMonitoring;
import ua.wyverno.twitch.TwitchService;

import java.util.List;

public class DiscordBot implements Notification {
    private static final Logger logger = LoggerFactory.getLogger(DiscordBot.class);
    private final JDA JDA;

    private final User targetUser;
    private final StopCommand stopCommand;
    private final TwitchService twitchService;

    public DiscordBot(DokiDokiMonitoring monitoring, TwitchService twitchService) {
        this.twitchService = twitchService;
        this.stopCommand = new StopCommand();
        this.JDA = JDABuilder
                .createDefault(ConfigLoader.getInstance().getProperty("discord-token"))
                .addEventListeners(new PauseCommand(monitoring))
                .addEventListeners(this.stopCommand)
                .addEventListeners(new BanCommand(this.twitchService))
                .addEventListeners(new UnBanCommand(this.twitchService))
                .enableIntents(GatewayIntent.GUILD_MEMBERS)
                .setMemberCachePolicy(MemberCachePolicy.ONLINE)
                .build();

        this.JDA.updateCommands()
                .addCommands(
                        Commands.slash("pause", "Ставить на паузу надходження сповіщення"),
                        Commands.slash("stop", "Зупиняє бота."),
                        Commands.slash("ban", "Перестати оповіщувати про певний етер")
                                .addOption(OptionType.STRING, "user-name", "Імя користувача на Твітчі", true),
                        Commands.slash("unban", "Відновити сповіщення про певний етер")
                                .addOption(OptionType.STRING, "user-name", "Імя користувача на Твітчі", true))
                .queue();

        String userID = ConfigLoader.getInstance().getProperty("discord-user-id-notification");
        this.targetUser = this.JDA.retrieveUserById(userID).complete();
        if (this.targetUser == null) {
            this.JDA.shutdown();
            throw new NullPointerException(String.format("Target user with id: %s - not EXISTS!", userID));
        }
    }

    public void addStopCommandHook(Runnable hook) {
        this.stopCommand.addStopHook(hook);
    }

    @Override
    public void sendNotification(List<Stream> streams) {
        this.targetUser.openPrivateChannel().queue(privateChannel -> {
            StringBuilder messageBuilder = new StringBuilder();
            messageBuilder.append("Зараз активні етери по Doki Doki:\n");
            streams.forEach(stream -> messageBuilder
                    .append(String.format("Назва: %s, Кількість глядачів: %d, Посилання - https://www.twitch.tv/%s%n",
                            stream.getTitle(),
                            stream.getViewerCount(),
                            stream.getUserName())));
            privateChannel.sendMessage(messageBuilder).queue();
        });
    }


}
