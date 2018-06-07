package unl.fct.srsc.client.config;

public class Configurations {

    private SecurityConfig securityConfig;
    private TpmHostsConfig tpmHosts;

    public Configurations() {
    }

    public Configurations(SecurityConfig securityConfig, TpmHostsConfig tpmHosts) {
        this.securityConfig = securityConfig;
        this.tpmHosts = tpmHosts;
    }

    public SecurityConfig getSecurityConfig() {
        return securityConfig;
    }

    public void setSecurityConfig(SecurityConfig securityConfig) {
        this.securityConfig = securityConfig;
    }

    public TpmHostsConfig getTpmHosts() {
        return tpmHosts;
    }

    public void setTpmHosts(TpmHostsConfig tpmHosts) {
        this.tpmHosts = tpmHosts;
    }
}