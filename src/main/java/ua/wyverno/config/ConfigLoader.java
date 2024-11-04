package ua.wyverno.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

public class ConfigLoader {
    private final Properties properties = new Properties();
    private static ConfigLoader instance;

    private ConfigLoader(String configFile) throws IOException {
        this.properties.load(Files.newInputStream(Paths.get(configFile)));
    }

    public static ConfigLoader getInstance() {
        if (instance == null) {
            try {
                instance = new ConfigLoader("config.properties");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return instance;
    }

    public String getProperty(String key) {
        return this.properties.getProperty(key);
    }

    public List<String> getProperties(String... keys) {
        return Arrays.stream(keys)
                .map(this.properties::getProperty)
                .collect(Collectors.toList());
    }
}
