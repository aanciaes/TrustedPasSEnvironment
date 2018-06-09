package unl.fct.srsc.tpm.config;

import java.util.List;

public class ServerConfig {

    private String keyStoreType;
    private String keyStoreName;
    private String keyStorePassword;
    private String keyPassword;

    private List<String> ciphersuites;
    private List<String> confProtocols;

    private String sslContext;

    public ServerConfig() {

    }

    public ServerConfig(String keyStoreType, String keyStoreName, String keyStorePassword, String keyPassword, List<String> ciphersuites, List<String> confProtocols, String sslContext) {
        this.keyStoreType = keyStoreType;
        this.keyStoreName = keyStoreName;
        this.keyStorePassword = keyStorePassword;
        this.keyPassword = keyPassword;
        this.ciphersuites = ciphersuites;
        this.confProtocols = confProtocols;
        this.sslContext = sslContext;
    }

    public String getKeyStoreType() {
        return keyStoreType;
    }

    public void setKeyStoreType(String keyStoreType) {
        this.keyStoreType = keyStoreType;
    }

    public String getKeyStoreName() {
        return keyStoreName;
    }

    public void setKeyStoreName(String keyStoreName) {
        this.keyStoreName = keyStoreName;
    }

    public String getKeyStorePassword() {
        return keyStorePassword;
    }

    public void setKeyStorePassword(String keyStorePassword) {
        this.keyStorePassword = keyStorePassword;
    }

    public String getKeyPassword() {
        return keyPassword;
    }

    public void setKeyPassword(String keyPassword) {
        this.keyPassword = keyPassword;
    }

    public List<String> getCiphersuites() {
        return ciphersuites;
    }

    public void setCiphersuites(List<String> ciphersuites) {
        this.ciphersuites = ciphersuites;
    }

    public List<String> getConfProtocols() {
        return confProtocols;
    }

    public void setConfProtocols(List<String> confProtocols) {
        this.confProtocols = confProtocols;
    }

    public String getSslContext() {
        return sslContext;
    }

    public void setSslContext(String sslContext) {
        this.sslContext = sslContext;
    }
}
