package ua.wyverno.twitch;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ListBanStream {
    private static final Logger logger = LoggerFactory.getLogger(ListBanStream.class);
    private static ListBanStream instance;
    private static final File JSON_FILE = new File("banned-streams.json");
    private final ObjectMapper mapper;
    private final List<BanStream> banlist;
    private final List<BanStream> temporaryBanList;

    private ListBanStream(List<BanStream> banlist) {
        this.mapper = new ObjectMapper();
        this.banlist = banlist;
        this.temporaryBanList = new ArrayList<>();
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    public static ListBanStream getInstance() {
        if (instance != null) return instance;
        if (!JSON_FILE.exists() || JSON_FILE.isDirectory()) { // Якщо файл не існує, або це директорія
            logger.trace("{} not exists, creating empty ban list", JSON_FILE);
            instance = new ListBanStream(new ArrayList<>()); // Створюємо обєкт з порожнім списом
            return instance;
        }
        try { // Інакше читаємо файл та завантажуємо список забанених
            logger.trace("{} is exists, start reading...", JSON_FILE);
            ObjectMapper mapper = new ObjectMapper();
            List<BanStream> banStreams = mapper.readValue(JSON_FILE, new TypeReference<List<BanStream>>(){});
            instance = new ListBanStream(banStreams);
            return instance;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean contains(String user_id) {
        boolean contains = this.banlist.stream() // Якщо існує у вічному бан-листі
                .anyMatch(banStream -> banStream.getUserId().equals(user_id));
        if (contains) return true; // Якщо було знайдено, повертаємо що знайдено.
        return this.temporaryBanList.stream() // Якщо не було знайдено у вічному бан-листу, шукаємо у тимчасовому бан-листу
                .anyMatch(banStream -> banStream.getUserId().equals(user_id));
    }

    public boolean add(BanStream banStream) {
        if (!this.contains(banStream.getUserId())) {
            this.banlist.add(banStream);
            this.updateFile();
            return true;
        }
        return false;
    }

    /**
     * Додає бан стріма без збереження у файлі
     * @param banStream бан стрім
     * @return якщо було додано true, якщо ні false
     */
    public boolean addTemporary(BanStream banStream) {
        if (!this.contains(banStream.getUserId())) {
            this.temporaryBanList.add(banStream);
            return true;
        }
        return false;
    }

    public boolean remove(BanStream banStream) {
        boolean removed = this.banlist.remove(banStream);
        if (removed) { // Якщо видалено з вічного бану, оновлюємо файл
            this.updateFile();
            return true;
        } // Якщо видалиться з тимчасових, не оновлюємо файл
        return this.temporaryBanList.remove(banStream);
    }
    public BanStream getByID(String user_id) {
        return this.banlist.stream().filter(banStream -> banStream.getUserId().equals(user_id))
                .findFirst()
                .orElse(this.temporaryBanList.stream()
                        .filter(banStream -> banStream.getUserId().equals(user_id))
                        .findFirst()
                        .orElse(null));
    }
    public boolean removeById(String user_id) {
        logger.trace("Ban-list: {}", this.banlist);
        logger.trace("Temporary ban-list: {}", this.temporaryBanList);
        BanStream banStream = this.getByID(user_id);
        if (banStream != null) return this.remove(banStream);
        return false;
    }

    private void updateFile() {
        try {
            this.mapper.writeValue(JSON_FILE, this.banlist);
            logger.trace("Update JSON File");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
