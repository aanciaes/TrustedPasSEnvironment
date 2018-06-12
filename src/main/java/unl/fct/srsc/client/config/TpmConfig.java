package unl.fct.srsc.client.config;

import java.util.List;

public abstract class TpmConfig {

    private String host;
    private String port;

    private String ciphersuite;
    private String keySize;
    private String provider;
    private List<String> runningPrograms;
    private List<String> attestationHashes;

    public TpmConfig() {
    }

    public TpmConfig(String host, String port, String ciphersuite, String keySize, String provider, List<String> runningPrograms, List<String> attestationHashes) {
        this.host = host;
        this.port = port;
        this.ciphersuite = ciphersuite;
        this.keySize = keySize;
        this.provider = provider;
        this.runningPrograms = runningPrograms;
        this.attestationHashes = attestationHashes;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
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

    public List<String> getRunningPrograms() {
        return runningPrograms;
    }

    public void setRunningPrograms(List<String> runningPrograms) {
        this.runningPrograms = runningPrograms;
    }

    public List<String> getAttestationHashes() {
        return attestationHashes;
    }

    public void setAttestationHashes(List<String> attestationHashes) {
        this.attestationHashes = attestationHashes;
    }
}
