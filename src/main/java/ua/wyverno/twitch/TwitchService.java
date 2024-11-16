package ua.wyverno.twitch;

import com.github.twitch4j.ITwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.helix.TwitchHelix;
import com.github.twitch4j.helix.domain.Stream;
import com.github.twitch4j.helix.domain.StreamList;
import com.github.twitch4j.helix.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class TwitchService {
    private static final Logger logger = LoggerFactory.getLogger(TwitchService.class);
    private final List<String> CATEGORIES_IDS;
    private final ITwitchClient twitchClient;
    private final TwitchHelix twitchHelix;

    public TwitchService(String clientID, String clientSecret, List<String> CATEGORIES_IDS) {
        this.twitchClient = TwitchClientBuilder.builder()
                .withClientId(clientID)
                .withClientSecret(clientSecret)
                .withEnableHelix(true)
                .build();
        this.twitchHelix = twitchClient.getHelix();
        this.CATEGORIES_IDS = CATEGORIES_IDS;
    }

    public List<Stream> getUkrainianStreams() {
        List<Stream> streams = new ArrayList<>();
        Set<String> seenStreamIDs = new HashSet<>();

        String paginationCursor = null;
        do {
            StreamList resultStreamList = this.twitchHelix // Робимо запит до Твітч АПІ, на отримання стрімів
                    .getStreams(null, paginationCursor, null, 100, this.CATEGORIES_IDS, null, null, null)
                    .execute();
            // Перевіряємо чи немає дубльованих етерів, за допомогою айді, якщо буде дубльований айді, seenStreamsIDs у методі add поверне false,
            // бо такий айді вже присутній у Сеті, тому що цей айді був доданий до Сету, через те, що це виконується у фільтрі
            // це не додасть до листа етерів цей дубльований етер
            resultStreamList.getStreams().stream()
                    .filter(stream -> seenStreamIDs.add(stream.getId()))
                    .forEach(streams::add);

            paginationCursor = resultStreamList.getPagination().getCursor(); // Записуємо курсор у змінну
        } while (paginationCursor != null); // Якщо курсор присутній продовжуємо дивитись всі етери

        return StreamFilter.filterUkrainianStreams(streams);
    }

    public User searchUser(String display_name) {
        logger.trace("Search user by name: {}", display_name);
        List<User> users = this.twitchHelix // Шукаємо юзера за логіном
                .getUsers(null, null, Collections.singletonList(display_name.toLowerCase()))
                .execute().getUsers();

        if (users.size() > 0) { // Якщо когось знайшли відаємо першого
            logger.trace("Found user by name: {}", display_name);
            return users.get(0);
        } else {
            return null;
        }
    }

    public void close() {
        this.twitchClient.close();
    }
}
