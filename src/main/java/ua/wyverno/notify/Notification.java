package ua.wyverno.notify;

import com.github.twitch4j.helix.domain.Stream;

import java.util.List;

public interface Notification {
    void sendNotification(List<Stream> streams);
}
