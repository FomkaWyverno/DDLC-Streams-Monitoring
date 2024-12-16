package ua.wyverno.notify.discord;

import com.github.twitch4j.helix.domain.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.wyverno.config.ConfigLoader;
import ua.wyverno.twitch.BanStream;
import ua.wyverno.twitch.ListBanStream;
import ua.wyverno.twitch.TwitchService;

public class BanToRebootCommand extends ListenerAdapter {
    private final static Logger logger = LoggerFactory.getLogger(BanToRebootCommand.class);

    private final TwitchService twitchService;

    public BanToRebootCommand(TwitchService twitchService) {
        this.twitchService = twitchService;
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (event.getName().equals("ban-to-reboot")) {
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
            boolean addedBan = listBanStream
                    .addTemporary(new BanStream(userTwitch.getId(), userTwitch.getDisplayName()));

            String urlStream = "https://www.twitch.tv/"+userTwitch.getDisplayName();
            if (addedBan) {
                event.getHook().sendMessage("Успішно додано до списку прихованих до перезапуску бота \""
                        + userTwitch.getDisplayName() +"\"!\n"+urlStream).queue();
                logger.trace("Add to Reboot Discord Bot Ban Twitch Stream for user: {}\nURL: {}", userTwitch.getDisplayName(), urlStream);
            } else {
                event.getHook().sendMessage("Цей користувач вже у списку прихованих!\n"+urlStream).queue();
                logger.trace("{} - This user in ban list\nURL: {}", userTwitch.getDisplayName(), urlStream);
            }
        }
    }
}
