package ua.wyverno.twitch;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class BanStream {
    private String user_id;
    private String user_name;

    @JsonCreator
    public BanStream(@JsonProperty("userId")
                     String user_id,
                     @JsonProperty("userName")
                     String user_name) {
        this.user_id = user_id;
        this.user_name = user_name;
    }

    public String getUserId() {
        return user_id;
    }

    public String getUserName() {
        return user_name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BanStream banStream = (BanStream) o;

        if (!Objects.equals(user_id, banStream.user_id)) return false;
        return Objects.equals(user_name, banStream.user_name);
    }

    @Override
    public int hashCode() {
        int result = user_id != null ? user_id.hashCode() : 0;
        result = 31 * result + (user_name != null ? user_name.hashCode() : 0);
        return result;
    }
}
