package unl.fct.srsc;

import org.apache.commons.codec.binary.Hex;
import redis.clients.jedis.Jedis;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.Key;

public class HelloWorld {

    public static void main(String[] args) {
        try {

            System.out.println("REDIS_SERVER: " + System.getenv("REDIS_SERVER"));

            Jedis cli = new Jedis(System.getenv("REDIS_SERVER"), 6379);
            Cipher c = c = Cipher.getInstance("blowfish/ECB/PKCS5Padding", "SunJCE");

            Key keySecret = new SecretKeySpec("Passwords".getBytes(), "blowfish");

            //Cipher mp + hash
            c.init(Cipher.ENCRYPT_MODE, keySecret);

            String message = "Miguel:Anciaes:94000:bajdjorazz";
            byte[] ecryptedCore = c.doFinal(message.getBytes());

            String key = "123";
            byte[] encryptedKey = c.doFinal(key.getBytes());

            System.out.println("Encrypted Key: " + Hex.encodeHexString(encryptedKey));
            System.out.println("Encrypted Value: " + Hex.encodeHexString(ecryptedCore));

            cli.set(Hex.encodeHexString(encryptedKey), Hex.encodeHexString(ecryptedCore));
            System.out.println(cli.get(Hex.encodeHexString(encryptedKey)));

        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
