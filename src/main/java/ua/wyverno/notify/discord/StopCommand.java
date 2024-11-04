package ua.wyverno.notify.discord;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import ua.wyverno.config.ConfigLoader;

import java.util.ArrayList;
import java.util.List;

public class StopCommand extends ListenerAdapter {

    private List<Runnable> hooks = new ArrayList<>();
    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (event.getName().equals("stop")) {
            event.deferReply().queue();

            if (event.getUser().getId().equals(ConfigLoader.getInstance().getProperty("discord-user-id-notification"))) {
                this.hooks.forEach(Runnable::run);
                event.getHook().sendMessage("Бота зупинено").queue(response -> event.getJDA().shutdown());
            } else {
                event.getHook().sendMessage("У вас немає прав, щоб це виконати!").queue();
            }
        }
    }

    protected void addStopHook(Runnable hook) {
        this.hooks.add(hook);
    }
}
