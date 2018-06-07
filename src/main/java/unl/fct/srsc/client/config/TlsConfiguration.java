package unl.fct.srsc.client.config;

public class TlsConfiguration {

    private String tls;

    public TlsConfiguration () {

    }

    public TlsConfiguration(String tls) {
        this.tls = tls;
    }

    public String getTls() {
        return tls;
    }

    public void setTls(String tls) {
        this.tls = tls;
    }
}
