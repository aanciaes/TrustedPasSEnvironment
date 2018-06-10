package unl.fct.srsc.client.tpm;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import unl.fct.srsc.client.config.TpmHostsConfig;
import unl.fct.srsc.tpm.Utils;

import javax.crypto.*;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.DHPublicKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;

public class TpmConnector {

    private static final String DIFFIE_HELLMAN = "DH";
    private static final String PROVIDER = "SunJCE";

    private static final String REQUEST_CODE = "0x00";
    private static final String RESPONSE_CODE = "0x01";
    private static final int ATTESTATION_RESPONSE = 0;
    private static final int ATTESTATION_SIGNATURE = 1;
    private static final int SIGNATURE_DH_PUB_N = 0;
    private static final int SIGNATURE_NONCE = 1;
    private static final int ATTESTATION_STATUS = 2;

    private static final String ERROR_MESSAGE = "Error Message";

    private String redisServer;
    private TpmHostsConfig tpmHostsConfig;

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


    private DHParameterSpec dhParams;
    private KeyPairGenerator keyGen;
    private KeyAgreement keyAgree;
    private KeyPair pair;

    public TpmConnector(String redisServer, TpmHostsConfig tpmHostsConfig) {
        this.redisServer = redisServer;
        this.tpmHostsConfig = tpmHostsConfig;
    }

    public boolean checkTpm() {

        SSLSocketFactory f =
                (SSLSocketFactory) SSLSocketFactory.getDefault();
        try {
            SSLSocket c = (SSLSocket) f.createSocket(redisServer, 9999);
            c.startHandshake();

            BufferedWriter w = new BufferedWriter(
                    new OutputStreamWriter(c.getOutputStream()));
            BufferedReader r = new BufferedReader(
                    new InputStreamReader(c.getInputStream()));

            startDiffieHellman();

            String m = buildRequest();

            String nonce = m.split("\\|")[2]; //Saving nonce for response

            w.write(m, 0, m.length());
            w.newLine();
            w.flush();

            String rst = r.readLine();
            System.out.println("Response: " + rst);
            boolean status = analizeResponse(rst, nonce);
            w.close();
            r.close();
            c.close();

            return status;
        } catch (Exception e) {
            System.err.println(e.toString());
            e.printStackTrace();
        }
        return false;
    }

    private String buildRequest() {
        String pubDH = ((DHPublicKey) pair.getPublic()).getY().toString();
        String nonce = Utils.randomNonce();

        return String.format("%s|%s|%s|%s|%s|%s", REQUEST_CODE, pubDH, nonce,
                tpmHostsConfig.getCiphersuite(), tpmHostsConfig.getProvider(), tpmHostsConfig.getKeySize());
    }

    private void startDiffieHellman() throws NoSuchProviderException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException {
        dhParams = new DHParameterSpec(p1024, g512);
        keyGen = KeyPairGenerator.getInstance(DIFFIE_HELLMAN, PROVIDER);
        ;

        keyGen.initialize(dhParams, new SecureRandom());
        keyAgree = KeyAgreement.getInstance(DIFFIE_HELLMAN, PROVIDER);

        pair = keyGen.generateKeyPair();
        keyAgree.init(pair.getPrivate());
    }

    private boolean analizeResponse(String response, String nonce) throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException, NoSuchPaddingException, BadPaddingException, IllegalBlockSizeException, DecoderException {
        if(!response.equals(ERROR_MESSAGE)) {
            String[] res = response.split("\\|");

            if (res[ATTESTATION_RESPONSE].equals(RESPONSE_CODE)) {
                String[] signature = res[ATTESTATION_SIGNATURE].split("\\:");

                String oldNoncePlusOne = new BigInteger(nonce).add(BigInteger.ONE).toString();

                if (oldNoncePlusOne.equals(signature[SIGNATURE_NONCE])) {
                    BigInteger y = new BigInteger(signature[SIGNATURE_DH_PUB_N]);
                    PublicKey p = KeyFactory.getInstance(DIFFIE_HELLMAN).generatePublic(new DHPublicKeySpec(y, p1024, g512));

                    keyAgree.doPhase(p, true);

                    byte[] agreedKey = keyAgree.generateSecret();
                    int keySize = Integer.parseInt(tpmHostsConfig.getKeySize()) / 8;
                    byte[] agreedCroppedKey = new byte[keySize];
                    System.arraycopy(agreedKey, 0, agreedCroppedKey, 0, keySize);

                    String alg = tpmHostsConfig.getCiphersuite().split("\\/")[0];

                    Cipher c = Cipher.getInstance(tpmHostsConfig.getCiphersuite(), tpmHostsConfig.getProvider());
                    c.init(Cipher.DECRYPT_MODE, new SecretKeySpec(agreedCroppedKey, alg));

                    byte[] decryptedCore = c.doFinal(Hex.decodeHex(res[ATTESTATION_STATUS]));

                    String processString = new String(decryptedCore);
                    System.out.println("Attestation Status: " + processString);

                    List<String> processList = stringTolist(processString);

                    return checkValidState(processList);
                }
            }
        }
        return false;
    }

    private boolean checkValidState(List<String> processList) {

        if(processList.size()>0){
            return true;
        }

        return false;
    }

    private List<String> stringTolist(String processString) {
        List<String> output = new ArrayList<String>();

        String[] lines = processString.split("#");

        for(String line: lines){
            output.add(line);
            System.out.println(line);
        }

        return output;

    }
}
