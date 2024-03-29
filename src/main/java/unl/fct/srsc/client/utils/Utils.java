package unl.fct.srsc.client.utils;

import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import unl.fct.srsc.client.config.Configurations;
import unl.fct.srsc.client.config.GosTpm;
import unl.fct.srsc.client.config.SecurityConfig;
import unl.fct.srsc.client.config.VmsTpm;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.*;
import java.security.cert.Certificate;

public class Utils {

    public static final String SECURITY_CONFIG_FILE = "ciphersuite.yml";
    public static final String SECURITY_CONFIG_LOCATION = "configs/client/";

    public static Configurations readFromConfig() {
        Constructor constructor = new Constructor(Configurations.class);
        TypeDescription configDescription = new TypeDescription(Configurations.class);

        configDescription.addPropertyParameters("securityConfig", SecurityConfig.class);
        configDescription.addPropertyParameters("vmsTpm", VmsTpm.class);
        configDescription.addPropertyParameters("gosTpm", GosTpm.class);
        constructor.addTypeDescription(configDescription);

        Yaml yaml = new Yaml(constructor);

        try {
            File f = new File(SECURITY_CONFIG_LOCATION + SECURITY_CONFIG_FILE);
            InputStream in = new FileInputStream(f);
            Configurations configs = yaml.loadAs(in, Configurations.class);

            return configs;

        } catch (Exception e) {
            e.printStackTrace();
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

    public static KeyPair getKeyPairFromKeyStore (SecurityConfig securityConfig) {
        try {
            KeyStore keyStore = KeyStore.getInstance(securityConfig.getKeyStoreType());
            // Keystore where symmetric keys are stored (type JCEKS)
            FileInputStream stream = new FileInputStream(SECURITY_CONFIG_LOCATION + securityConfig.getKeyStoreName());
            keyStore.load(stream, securityConfig.getKeyStorePassword().toCharArray());

            PrivateKey privateKey = (PrivateKey) keyStore.getKey(securityConfig.getSignatureKeyName(),
                    securityConfig.getSignatureKeyPassword().toCharArray());

            final Certificate cert = keyStore.getCertificate(securityConfig.getSignatureKeyName());
            final PublicKey publicKey = cert.getPublicKey();

            return new KeyPair(publicKey, privateKey);

        }catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static class FixedRand extends SecureRandom {
        MessageDigest sha;
        byte[] state;

        FixedRand() {
            try {
                this.sha = MessageDigest.getInstance("SHA-512");
                this.state = sha.digest();
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException("Nao existe suporte SHA-512!");
            }
        }

        public void nextBytes(
                byte[] bytes) {
            int off = 0;

            sha.update(state);

            while (off < bytes.length) {
                state = sha.digest();

                if (bytes.length - off > state.length) {
                    System.arraycopy(state, 0, bytes, off, state.length);
                } else {
                    System.arraycopy(state, 0, bytes, off, bytes.length - off);
                }

                off += state.length;

                sha.update(state);
            }
        }
    }

    /**
     * Retorna um SecureRandom de teste com o mesmo valor...
     *
     * @return random fixo
     */
    public static SecureRandom createFixedRandom() {
        return new FixedRand();
    }
}
