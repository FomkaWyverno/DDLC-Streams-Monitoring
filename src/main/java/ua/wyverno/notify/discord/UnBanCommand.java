package ua.wyverno.notify.discord;

import com.github.twitch4j.helix.domain.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.wyverno.config.ConfigLoader;
import ua.wyverno.twitch.ListBanStream;
import ua.wyverno.twitch.TwitchService;

public class UnBanCommand extends ListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(UnBanCommand.class);
    private final TwitchService twitchService;

    public UnBanCommand(TwitchService twitchService) {
        this.twitchService = twitchService;
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (event.getName().equals("unban")) {
            event.deferReply().queue();
            logger.trace("Executing {} command! By user: {} id: {}", event.getCommandString(), event.getUser().getName(), event.getUser().getIdLong());

            if (!event.getUser().getId().equals(ConfigLoader.getInstance().getProperty("discord-user-id-notification"))) {
                event.reply("Ви не можете використовувати цю команду!").queue();
                logger.trace("No permission executing command for user: {}, id {}", event.getUser().getName(), event.getUser().getIdLong());
                return;
            }

            OptionMapping optionMapping = event.getOption("user-name");
            if (optionMapping == null) {
                event.getHook().sendMessage("Ви не ввели нік!").queue();
                logger.trace("Not print display name for ban!");
                return;
            }

            String user_name = optionMapping.getAsString();
            User userTwitch = this.twitchService.searchUser(user_name);

            if (userTwitch == null) {
                event.getHook().sendMessage("Не змогли знайти користувача за ніком: " + user_name).queue();
                logger.trace("Not found user on Twitch for username - {}", user_name);
                return;
            }

            ListBanStream listBanStream = ListBanStream.getInstance();

            if (!listBanStream.contains(userTwitch.getId())) {
                event.getHook().sendMessage("Такого користувача немає в списку прихованих!").queue();
                logger.trace("User: {}, ID: {} - Not in BanList", userTwitch.getDisplayName(), userTwitch.getId());
                return;
            }
            boolean removed = listBanStream.removeById(userTwitch.getId());

            String urlTwitch = "https://www.twitch.tv/" + userTwitch.getDisplayName();
            if (removed) {
                event.getHook().sendMessage("Успішно видалено зі списку прихованих користувача\n"+urlTwitch).queue();
                logger.trace("Successful remove from ban list: {}", userTwitch.getDisplayName());
            } else {
                event.getHook().sendMessage("Виникла помилка при видалені користувача зі списку прихованих!").queue();
                logger.trace("Fail remove user: {}, ID: {} from ban list", userTwitch.getDisplayName(), userTwitch.getId());
            }
        }
    }
}
