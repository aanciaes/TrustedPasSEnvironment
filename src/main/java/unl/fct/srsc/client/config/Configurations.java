package unl.fct.srsc.client.config;

public class Configurations {

    private SecurityConfig securityConfig;
    private VmsTpm vmsTpm;
    private GosTpm gosTpm;

    public Configurations() {
    }

    public Configurations(SecurityConfig securityConfig, VmsTpm vmsTpm, GosTpm gosTpm) {
        this.securityConfig = securityConfig;
        this.vmsTpm = vmsTpm;
        this.gosTpm = gosTpm;
    }

    public SecurityConfig getSecurityConfig() {
        return securityConfig;
    }

    public void setSecurityConfig(SecurityConfig securityConfig) {
        this.securityConfig = securityConfig;
    }

    public VmsTpm getVmsTpm() {
        return vmsTpm;
    }

    public void setVmsTpm(VmsTpm vmsTpm) {
        this.vmsTpm = vmsTpm;
    }

    public GosTpm getGosTpm() {
        return gosTpm;
    }

    public void setGosTpm(GosTpm gosTpm) {
        this.gosTpm = gosTpm;
    }
}