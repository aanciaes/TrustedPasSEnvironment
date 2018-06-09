package unl.fct.srsc.tpm;

import java.security.SecureRandom;
import java.util.Random;

public class Utils {
    private static final char STGC_TLS = 'M';
    private static final char STGC_SAP = 'S';

    private final static String INTEGER_NONCE = "123456789";

    private final static int nonceLength = 20;
    private static Random rnd = new SecureRandom();

    public static String randomNonce (){

        StringBuilder sb = new StringBuilder( nonceLength );
        for( int i = 0; i < nonceLength; i++ )
            sb.append( INTEGER_NONCE.charAt( rnd.nextInt(INTEGER_NONCE.length()) ) );
        return sb.toString();
    }
}
