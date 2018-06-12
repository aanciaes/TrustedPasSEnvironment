package unl.fct.srsc.client.config;

import java.util.List;

public class GosTpm extends TpmConfig {

    private final String name = "GOS";

    public GosTpm() {
        super();
    }

    public GosTpm(String gosHost, String gosPort, String ciphersuite, String keySize, String provider, List<String> runningPrograms, List<String> attestationHashes) {
        super(gosHost, gosPort, ciphersuite, keySize, provider, runningPrograms, attestationHashes);
    }

    public String getName() {
        return name;
    }
}
