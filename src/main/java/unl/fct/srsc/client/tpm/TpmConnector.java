package unl.fct.srsc.client.tpm;

import unl.fct.srsc.client.config.TpmHostsConfig;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;

public class TpmConnector {

    private String redisServer;
    private TpmHostsConfig tpmHostsConfig;

    public TpmConnector (String redisServer, TpmHostsConfig tpmHostsConfig) {
        this.redisServer = redisServer;
        this.tpmHostsConfig = tpmHostsConfig;
    }

    public boolean checkTpm () {

        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        PrintStream out = System.out;

        SSLSocketFactory f =
                (SSLSocketFactory) SSLSocketFactory.getDefault();
        try {
            SSLSocket c = (SSLSocket) f.createSocket(redisServer, 9999);


            c.startHandshake();
            BufferedWriter w = new BufferedWriter(
                    new OutputStreamWriter(c.getOutputStream()));
            BufferedReader r = new BufferedReader(
                    new InputStreamReader(c.getInputStream()));
            String m = "Hello World";
            w.write(m, 0, m.length());
            w.newLine();
            w.flush();
            w.close();
            r.close();
            c.close();

            return true;
        } catch (IOException e) {
            System.err.println(e.toString());
        }
        return false;
    }
}
