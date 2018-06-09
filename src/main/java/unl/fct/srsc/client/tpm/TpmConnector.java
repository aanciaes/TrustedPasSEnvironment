package unl.fct.srsc.client.tpm;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import unl.fct.srsc.client.config.TpmHostsConfig;
import unl.fct.srsc.tpm.Utils;

import javax.crypto.KeyAgreement;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.DHPublicKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.spec.InvalidKeySpecException;

public class TpmConnector {

    private static final String REQUEST_CODE = "0x00";
    private static final String RESPONSE_CODE = "0x01";

    private String redisServer;
    private TpmHostsConfig tpmHostsConfig;

    // Parametro para o gerador do Grupo de Cobertura de P
    private static BigInteger g512 = new BigInteger(
            "153d5d6172adb43045b68ae8e1de1070b6137005686d29d3d73a7"
                    + "749199681ee5b212c9b96bfdcfa5b20cd5e3fd2044895d609cf9b"
                    + "410b7a0f12ca1cb9a428cc", 16);
    // Um grande numero primo P
    private static BigInteger p512 = new BigInteger(
            "9494fec095f3b85ee286542b3836fc81a5dd0a0349b4c239dd387"
                    + "44d488cf8e31db8bcb7d33b41abb9e5a33cca9144b1cef332c94b"
                    + "f0573bf047a3aca98cdf3b", 16);


    private DHParameterSpec dhParams;
    private KeyPairGenerator keyGen;
    private KeyAgreement keyAgree;
    private KeyPair pair;

    public TpmConnector (String redisServer, TpmHostsConfig tpmHostsConfig) {
        this.redisServer = redisServer;
        this.tpmHostsConfig = tpmHostsConfig;
    }

    public boolean checkTpm () {

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
            w.write(m, 0, m.length());
            w.newLine();
            w.flush();

            String rst = r.readLine();
            System.out.println("Response: " + rst);
            analizeResponse (rst);
            w.close();
            r.close();
            c.close();

            return true;
        } catch (Exception e) {
            System.err.println(e.toString());
        }
        return false;
    }

    private String buildRequest () {
        String pubDH = ((DHPublicKey)pair.getPublic()).getY().toString();
        String nonce = Utils.randomNonce();

        return String.format("%s|%s|%s", REQUEST_CODE, pubDH, nonce);
    }

    private void startDiffieHellman () throws NoSuchProviderException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException {
        dhParams = new DHParameterSpec(p512, g512);
        keyGen = KeyPairGenerator.getInstance("DH", "SunJCE");;

        keyGen.initialize(dhParams, new SecureRandom());
        keyAgree = KeyAgreement.getInstance("DH", "SunJCE");

        pair = keyGen.generateKeyPair();
        keyAgree.init(pair.getPrivate());
    }

    private void analizeResponse (String response) throws DecoderException, InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException {
        String [] res = response.split("\\|");

        BigInteger y = new BigInteger(res[1]);
        PublicKey p = KeyFactory.getInstance("DH").generatePublic(new DHPublicKeySpec(y, p512, g512));

        keyAgree.doPhase(p, true);

        String secret = Hex.encodeHexString(keyAgree.generateSecret());
        System.out.println("GENERATED SECRET: " + secret);
    }
}
