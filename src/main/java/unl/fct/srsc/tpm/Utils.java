package unl.fct.srsc.tpm;

import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import unl.fct.srsc.tpm.config.ServerConfig;
import unl.fct.srsc.tpm.config.ServerConfigurations;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.Random;

public class Utils {

    public static final String SECURITY_CONFIG_FILE = "ciphersuite.yml";
    public static final String SECURITY_CONFIG_LOCATION = "configs/server/";

    private final static String INTEGER_NONCE = "123456789";

    private final static int nonceLength = 20;
    private static Random rnd = new SecureRandom();

    public static String randomNonce() {

        StringBuilder sb = new StringBuilder(nonceLength);
        for (int i = 0; i < nonceLength; i++)
            sb.append(INTEGER_NONCE.charAt(rnd.nextInt(INTEGER_NONCE.length())));
        return sb.toString();
    }

    public static ServerConfig readFromConfig() {
        Constructor constructor = new Constructor(ServerConfigurations.class);
        TypeDescription configDescription = new TypeDescription(ServerConfigurations.class);

        configDescription.addPropertyParameters("serverConfig", ServerConfig.class);
        constructor.addTypeDescription(configDescription);

        Yaml yaml = new Yaml(constructor);

        try {
            File f = new File(SECURITY_CONFIG_LOCATION + SECURITY_CONFIG_FILE);
            InputStream in = new FileInputStream(f);
            ServerConfigurations configs = yaml.loadAs(in, ServerConfigurations.class);

            return configs.getServerConfig();

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error reading from security configurations");
            return null;
        }
    }
}
