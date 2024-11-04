package ua.wyverno.notify.discord;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import ua.wyverno.config.ConfigLoader;
import ua.wyverno.twitch.DokiDokiMonitoring;

public class PauseCommand extends ListenerAdapter {

    private final DokiDokiMonitoring monitoring;

    public PauseCommand(DokiDokiMonitoring monitoring) {
        this.monitoring = monitoring;
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (event.getName().equals("pause")) {
            event.deferReply().queue();

            if (!event.getUser().getId().equals(ConfigLoader.getInstance().getProperty("discord-user-id-notification"))) {
                event.reply("Ви не можете використовувати цю команду!").queue();
                return;
            }

            if (this.monitoring.togglePause()) {
                event.getHook().sendMessage("Поставлено на паузу надходження сповіщень!").queue();
            } else {
                event.getHook().sendMessage("Відновлено надходження сповіщень.").queue();
            }
        }
    }
}
