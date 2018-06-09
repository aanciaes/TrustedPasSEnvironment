package unl.fct.srsc.tpm;

import org.apache.commons.codec.binary.Hex;

import javax.crypto.KeyAgreement;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.DHPublicKeySpec;
import javax.net.ssl.*;
import java.io.*;
import java.math.BigInteger;
import java.security.*;

public class TpmTLSServer {

    private static final String REQUEST_CODE = "0x00";
    private static final String RESPONSE_CODE = "0x01";
    private static final int ATTESTATION_REQUEST = 0;
    private static final int ATTESTATION_DH_PUB_N = 1;
    private static final int ATTESTATION_NONCE = 2;

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


    public static void main(String[] args) {
        String[] confciphersuites = {"TLS_RSA_WITH_AES_256_CBC_SHA256"};
        String[] confprotocols = {"TLSv1.2"};

        try {
            KeyStore ks = KeyStore.getInstance("JKS");
            ks.load(new FileInputStream("configs/server/server.jks"), "P4s5w0rd".toCharArray());
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");

            kmf.init(ks, "P4s5w0rd".toCharArray());

            SSLContext sc = SSLContext.getInstance("TLS");
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
                    System.out.println(m);
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
                String tpmStatus = getTPMStatus();
                String noncePlusOne = new BigInteger(rq[ATTESTATION_NONCE]).add(BigInteger.ONE).toString();

                DHParameterSpec dhParams = new DHParameterSpec(p512, g512);
                KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DH", "SunJCE");
                keyGen.initialize(dhParams, new SecureRandom());

                KeyAgreement keyAgree = KeyAgreement.getInstance("DH", "SunJCE");
                KeyPair pair = keyGen.generateKeyPair();
                keyAgree.init(pair.getPrivate());

                BigInteger y = new BigInteger(rq[ATTESTATION_DH_PUB_N]);
                PublicKey p = KeyFactory.getInstance("DH").generatePublic(new DHPublicKeySpec(y, p512, g512));

                keyAgree.doPhase(p, true);

                String secret = Hex.encodeHexString(keyAgree.generateSecret());
                System.out.println("GENERATED SECRET: " + secret);
                String pubKey = ((DHPublicKey) pair.getPublic()).getY().toString();

                return String.format("%s|%s:%s|%s", RESPONSE_CODE, pubKey, noncePlusOne, tpmStatus);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "error";
    }

    private static String getTPMStatus() {
        return "tpm_status_ok";
    }
}
