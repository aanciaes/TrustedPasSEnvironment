package unl.fct.srsc.tpm.config;

public class ServerConfigurations {

    private ServerConfig serverConfig;

    public ServerConfigurations() {
    }

    public ServerConfigurations(ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
    }

    public ServerConfig getServerConfig() {
        return serverConfig;
    }

    public void setServerConfig(ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
    }
}
