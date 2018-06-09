package unl.fct.srsc.client.config;

public class TpmHostsConfig {

    private String vmsHost;
    private String vmsPort;

    private String gosHost;
    private String gosPort;

    private String ciphersuite;
    private String keySize;
    private String provider;

    public TpmHostsConfig() {

    }

    public TpmHostsConfig(String vmsHost, String vmsPort, String gosHost, String gosPort, String ciphersuite, String keySize, String provider) {
        this.vmsHost = vmsHost;
        this.vmsPort = vmsPort;
        this.gosHost = gosHost;
        this.gosPort = gosPort;
        this.ciphersuite = ciphersuite;
        this.keySize = keySize;
        this.provider = provider;
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

    public String getCiphersuite() {
        return ciphersuite;
    }

    public void setCiphersuite(String ciphersuite) {
        this.ciphersuite = ciphersuite;
    }

    public String getKeySize() {
        return keySize;
    }

    public void setKeySize(String keySize) {
        this.keySize = keySize;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }
}
