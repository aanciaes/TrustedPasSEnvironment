package unl.fct.srsc.config;

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.Key;
import java.security.KeyStore;

public class Utils {

    public static final String SECURITY_CONFIG_FILE = "cyphersuite.yml";
    public static final String SECURITY_CONFIG_LOCATION = "configs/";

    public static SecurityConfig readFromConfig() {
        Yaml yaml = new Yaml();

        try {
            File f = new File(SECURITY_CONFIG_LOCATION + SECURITY_CONFIG_FILE);
            InputStream in = new FileInputStream(f);
            Configurations configs = yaml.loadAs(in, Configurations.class);
            return configs.getChatRoomConfig();

        } catch (Exception e) {
            //e.printStackTrace();
            System.out.println("Error reading from security configurations");
            return null;
        }
    }

    public static Key getKeyFromKeyStore(SecurityConfig securityConfig) {

        try {
            KeyStore keyStore = KeyStore.getInstance(securityConfig.getKeyStoreType());
            // Keystore where symmetric keys are stored (type JCEKS)
            FileInputStream stream = new FileInputStream(SECURITY_CONFIG_LOCATION + securityConfig.getKeyStoreName());
            keyStore.load(stream, securityConfig.getKeyStorePassword().toCharArray());

            return keyStore.getKey(securityConfig.getKeyName(), securityConfig.getKeyPassword().toCharArray());

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
