package unl.fct.srsc.client.config;

public class VmsTpm extends TpmConfig{

    private final String name = "VMS";

    public VmsTpm() {
        super();
    }

    public VmsTpm(String vmsHost, String vmsPort, String ciphersuite, String keySize, String provider) {
        super(vmsHost, vmsPort, ciphersuite, keySize, provider);
    }

    public String getName() {
        return name;
    }


}
