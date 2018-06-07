package unl.fct.srsc.client.config;

import java.util.List;

public class Configurations {

    private SecurityConfig securityConfig;
    private TlsConfiguration tls;

    public Configurations() {
    }

    public Configurations(SecurityConfig securityConfig, TlsConfiguration tls) {
        this.securityConfig = securityConfig;
        this.tls = tls;
    }

    public SecurityConfig getSecurityConfig() {
        return securityConfig;
    }

    public void setSecurityConfig(SecurityConfig securityConfig) {
        this.securityConfig = securityConfig;
    }

    public TlsConfiguration getTls() {
        return tls;
    }

    public void setTls(TlsConfiguration tls) {
        this.tls = tls;
    }
}