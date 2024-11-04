package ua.wyverno.twitch;

import com.github.twitch4j.helix.domain.Stream;
import ua.wyverno.config.ConfigLoader;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class StreamFilter {
    private static final List<String> UKRAINIAN_TAGS = Arrays.stream(ConfigLoader.getInstance().getProperty("ua-tags").split(" "))
            .map(String::toLowerCase)
            .collect(Collectors.toList());

    public static List<Stream> filterUkrainianStreams(List<Stream> streams) {
        return streams.stream()
                .filter(stream -> stream.getLanguage().equals("uk") || hasUkrainianTags(stream.getTags()))
                .collect(Collectors.toList());
    }

    public static boolean hasUkrainianTags(List<String> tags) {
        tags = tags.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toList());
        return UKRAINIAN_TAGS.stream().anyMatch(tags::contains);
    }
}
