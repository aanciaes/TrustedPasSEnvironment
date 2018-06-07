package unl.fct.srsc.client.config;

import java.util.List;

public class Configurations {

    private List<SecurityConfig> config;

    public Configurations() {
    }

    public Configurations(List<SecurityConfig> config) {
        this.config = config;
    }

    public void setConfig(List<SecurityConfig> config) {
        this.config = config;
    }

    public List<SecurityConfig> getConfig() {
        return this.config;
    }

    public SecurityConfig getChatRoomConfig() {
        for (SecurityConfig crc : config) {
            return crc;
        }
        return null;
    }
}