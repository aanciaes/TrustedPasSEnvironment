package unl.fct.srsc.tpm;

import javax.net.ssl.*;
import java.io.*;
import java.security.KeyStore;

public class TpmTLSServer {

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

                while(true) {
                    SSLSocket c = (SSLSocket) s.accept();

                    BufferedWriter w = new BufferedWriter(new OutputStreamWriter(
                            c.getOutputStream()));
                    BufferedReader r = new BufferedReader(new InputStreamReader(
                            c.getInputStream()));

                    String m = "";
                    while ((m = r.readLine()) != null) {
                        System.out.println(m);
                        String rst = "Hello From Server";
                        w.write(rst, 0, rst.length());
                        w.newLine();
                        w.flush();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println(e.toString());
            }
    }
}
