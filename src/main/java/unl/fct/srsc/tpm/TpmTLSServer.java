package unl.fct.srsc.tpm;

import org.apache.commons.codec.binary.Hex;
import unl.fct.srsc.tpm.config.ServerConfig;

import javax.crypto.*;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.DHPublicKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.*;
import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.util.List;

public class TpmTLSServer {

    private static final String DIFFIE_HELLMAN = "DH";
    private static final String PROVIDER = "SunJCE";

    private static final String REQUEST_CODE = "0x00";
    private static final String RESPONSE_CODE = "0x01";
    private static final int ATTESTATION_REQUEST = 0;
    private static final int ATTESTATION_DH_PUB_N = 1;
    private static final int ATTESTATION_NONCE = 2;
    private static final int ATTESTATION_CIPHERSUITE = 3;
    private static final int ATTESTATION_CIPHERSUITE_PROVIDER = 4;
    private static final int ATTESTATION_CIPHERSUITE_KEYSIZE = 5;

    private static final String ERROR_MESSAGE = "Error Message";

    public static final String SECURITY_CONFIG_LOCATION = "configs/server/";
    private static ServerConfig serverConfig;

    // Parametro para o gerador do Grupo de Cobertura de P
    private static BigInteger g512 = new BigInteger(
            "153d5d6172adb43045b68ae8e1de1070b6137005686d29d3d73a7"
                    + "749199681ee5b212c9b96bfdcfa5b20cd5e3fd2044895d609cf9b"
                    + "410b7a0f12ca1cb9a428cc", 16);
    // Um grande numero primo P
    private static BigInteger p1024 = new BigInteger(
            "9494fec095f3b85ee286542b3836fc81a5dd0a0349b4c239dd387"
                    + "44d488cf8e31db8bcb7d33b41abb9e5a33cca9144b1cef332c94b"
                    + "f0573bf047a3aca98cdf3b"
                    + "9494fec095f3b85ee286542b3836fc81a5dd0a0349b4c239dd387"
                    + "44d488cf8e31db8bcb7d33b41abb9e5a33cca9144b1cef332c94b"
                    + "f0573bf047a3aca98cdf3b", 16);

    public static void main(String[] args) {
        serverConfig = Utils.readFromConfig();

        String[] confciphersuites = new String[serverConfig.getCiphersuites().size()];
        serverConfig.getCiphersuites().toArray(confciphersuites);

        String[] confprotocols = new String[serverConfig.getConfProtocols().size()];
        serverConfig.getConfProtocols().toArray(confprotocols);


        try {
            KeyStore ks = KeyStore.getInstance(serverConfig.getKeyStoreType());
            ks.load(new FileInputStream(SECURITY_CONFIG_LOCATION + serverConfig.getKeyStoreName()),
                    serverConfig.getKeyStorePassword().toCharArray());
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");

            kmf.init(ks, serverConfig.getKeyPassword().toCharArray());

            SSLContext sc = SSLContext.getInstance(serverConfig.getSslContext());
            sc.init(kmf.getKeyManagers(), null, null);
            SSLServerSocketFactory ssf = sc.getServerSocketFactory();
            SSLServerSocket s
                    = (SSLServerSocket) ssf.createServerSocket(9999);

            s.setEnabledProtocols(confprotocols);
            s.setEnabledCipherSuites(confciphersuites);

            System.out.println("TLS server ready to accept connections...");

            while (true) {
                SSLSocket c = (SSLSocket) s.accept();

                BufferedWriter w = new BufferedWriter(new OutputStreamWriter(
                        c.getOutputStream()));
                BufferedReader r = new BufferedReader(new InputStreamReader(
                        c.getInputStream()));

                String m;
                while ((m = r.readLine()) != null) {
                    System.out.println("Recieved request: " + m);
                    String rst = buildResponse(m);
                    w.write(rst, 0, rst.length());
                    w.newLine();
                    w.flush();
                }
                w.close();
                r.close();
                c.close();
                System.out.println("Session terminated...");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.toString());
        }
    }

    private static String buildResponse(String request) {
        String[] rq = request.split("\\|");
        try {

            if (rq[ATTESTATION_REQUEST].equals(REQUEST_CODE)) {
                List<String> tpmStatus = getTPMStatus();
                String noncePlusOne = new BigInteger(rq[ATTESTATION_NONCE]).add(BigInteger.ONE).toString();
                String ciphersuite = rq[ATTESTATION_CIPHERSUITE];
                String provider = rq[ATTESTATION_CIPHERSUITE_PROVIDER];
                int keySize = Integer.parseInt(rq[ATTESTATION_CIPHERSUITE_KEYSIZE]);

                DHParameterSpec dhParams = new DHParameterSpec(p1024, g512);
                KeyPairGenerator keyGen = KeyPairGenerator.getInstance(DIFFIE_HELLMAN, PROVIDER);
                keyGen.initialize(dhParams, new SecureRandom());

                KeyAgreement keyAgree = KeyAgreement.getInstance(DIFFIE_HELLMAN, PROVIDER);
                KeyPair pair = keyGen.generateKeyPair();
                keyAgree.init(pair.getPrivate());

                BigInteger y = new BigInteger(rq[ATTESTATION_DH_PUB_N]);
                PublicKey p = KeyFactory.getInstance(DIFFIE_HELLMAN).generatePublic(new DHPublicKeySpec(y, p1024, g512));

                keyAgree.doPhase(p, true);

                byte[] agreedKey = keyAgree.generateSecret();
                byte[] agreedCroppedKey = new byte[keySize / 8];
                System.arraycopy(agreedKey, 0, agreedCroppedKey, 0, keySize / 8);
                System.out.println("Cropped key: " + Hex.encodeHexString(agreedCroppedKey));

                String pubKey = ((DHPublicKey) pair.getPublic()).getY().toString();

                byte[] encAttestationStatus = encryptStatus(tpmStatus, ciphersuite, provider, agreedCroppedKey);

                return String.format("%s|%s:%s|%s", RESPONSE_CODE, pubKey, noncePlusOne,
                        Hex.encodeHexString(encAttestationStatus));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ERROR_MESSAGE;
    }

    private static List<String> getTPMStatus() throws IOException {

        return TpmStateData.getState();
    }

    private static byte[] encryptStatus(List<String> tpmStatus, String ciphersuite, String provider, byte[] key) throws NoSuchPaddingException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        String alg = ciphersuite.split("\\/")[0];

        Cipher c = Cipher.getInstance(ciphersuite, provider);
        c.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, alg));

        return c.doFinal(listToBytes(tpmStatus));
    }

    private static byte[] listToBytes(List<String> list) {

        String result = "";

        for (String line : list) {
            result += "#" + line;
        }
        return result.getBytes();
    }
}
