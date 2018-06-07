package unl.fct.srsc.client.config;

public class TlsConfiguration {

    private String vmsHost;
    private String vmsPort;

    private String gosHost;
    private String gosPort;

    public TlsConfiguration () {

    }

    public TlsConfiguration(String vmsHost, String vmsPort, String gosHost, String gosPort) {
        this.vmsHost = vmsHost;
        this.vmsPort = vmsPort;
        this.gosHost = gosHost;
        this.gosPort = gosPort;
    }

    public String getVmsHost() {
        return vmsHost;
    }

    public void setVmsHost(String vmsHost) {
        this.vmsHost = vmsHost;
    }

    public String getVmsPort() {
        return vmsPort;
    }

    public void setVmsPort(String vmsPort) {
        this.vmsPort = vmsPort;
    }

    public String getGosHost() {
        return gosHost;
    }

    public void setGosHost(String gosHost) {
        this.gosHost = gosHost;
    }

    public String getGosPort() {
        return gosPort;
    }

    public void setGosPort(String gosPort) {
        this.gosPort = gosPort;
    }
}
