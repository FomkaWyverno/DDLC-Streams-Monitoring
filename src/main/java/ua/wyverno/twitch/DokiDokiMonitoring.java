package ua.wyverno.twitch;

import com.github.twitch4j.helix.domain.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.wyverno.notify.Notification;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class DokiDokiMonitoring {
    private static final Logger logger = LoggerFactory.getLogger(DokiDokiMonitoring.class);
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final TwitchService twitchService;

    private final List<Notification> notifications;

    private final AtomicBoolean isPause = new AtomicBoolean(false);

    public DokiDokiMonitoring(TwitchService twitchService) {
        this.twitchService = twitchService;
        this.notifications = new ArrayList<>();
        scheduler.scheduleAtFixedRate(this::monitoringStreams, 0, 10, TimeUnit.SECONDS);
    }

    public void addNotificationListener(Notification notification) {
        this.notifications.add(notification);
    }

    private void monitoringStreams() {
        if (this.isPause.get()) return;
        List<Stream> ukrainianStreams = this.twitchService.getUkrainianStreams();
        if (!ukrainianStreams.isEmpty()) {
            logger.info("Found ukrainian streams");
            this.notifications.forEach(notification -> notification.sendNotification(ukrainianStreams));
        } else {
            logger.info("Not found ukrainian streams");
        }
    }

    public boolean togglePause() {
        boolean isPaused = !this.isPause.getAndSet(!this.isPause.get());
        if (isPaused) {
            logger.debug("Monitoring is paused.");
        } else {
            logger.debug("Monitoring resuming.");
        }

        return isPaused;
    }

    public void close() {
        scheduler.shutdown();
    }
}