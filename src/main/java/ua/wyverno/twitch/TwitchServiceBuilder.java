package ua.wyverno.twitch;

import java.util.List;

public class TwitchServiceBuilder {
    private String clientID;
    private String clientSecret;
    private List<String> categoriesIds;

    public TwitchServiceBuilder clientID(String clientID) {
        this.clientID = clientID;
        return this;
    }

    public TwitchServiceBuilder clientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
        return this;
    }

    public TwitchServiceBuilder CATEGORIES_IDS(List<String> categoriesIds) {
        this.categoriesIds = categoriesIds;
        return this;
    }

    public TwitchService build() {
        return new TwitchService(clientID, clientSecret, categoriesIds);
    }

}