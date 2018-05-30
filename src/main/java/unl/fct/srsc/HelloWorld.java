package unl.fct.srsc;

import org.bouncycastle.util.encoders.Hex;
import redis.clients.jedis.Jedis;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

public class HelloWorld {

    public static void main(String[] args) throws NoSuchPaddingException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        Jedis cli = new Jedis("localhost", 6379);
        Cipher c = c = Cipher.getInstance("blowfish/ECB/PKCS5Padding", "BC");

        Key keySecret = new SecretKeySpec("Passwords".getBytes(),"blowfish/ECB/PKCS5Padding");

        //Cipher mp + hash
        c.init(Cipher.ENCRYPT_MODE, keySecret);

        String message = "Miguel:Anciaes:94000:bajdjorazz";
        byte[] ecryptedCore = c.doFinal(message.getBytes());

        String key = "123";
        byte[] encryptedKey = c.doFinal(key.getBytes());

        System.out.println("Encrypted Key: " + Hex.toHexString(encryptedKey));
        System.out.println("Encrypted Value: " + Hex.toHexString(ecryptedCore));

        cli.set(Hex.toHexString(encryptedKey), Hex.toHexString(ecryptedCore));
        System.out.println(cli.get(Hex.toHexString(encryptedKey)));
    }
}
