package unl.fct.srsc.client.config;

public class GosTpm extends TpmConfig {

    private final String name = "GOS";

    public GosTpm() {
        super();
    }

    public GosTpm(String gosHost, String gosPort, String ciphersuite, String keySize, String provider) {
        super(gosHost, gosPort, ciphersuite, keySize, provider);
    }

    public String getName() {
        return name;
    }
}
