package unl.fct.srsc.client.config;

import java.util.List;

public class VmsTpm extends TpmConfig{

    private final String name = "VMS";

    public VmsTpm() {
        super();
    }

    public VmsTpm(String vmsHost, String vmsPort, String ciphersuite, String keySize, String provider, List<String> runningPrograms, List<String> attestationHashes) {
        super(vmsHost, vmsPort, ciphersuite, keySize, provider, runningPrograms, attestationHashes);
    }

    public String getName() {
        return name;
    }
}
