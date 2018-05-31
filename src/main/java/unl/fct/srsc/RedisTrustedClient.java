package unl.fct.srsc;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import redis.clients.jedis.Jedis;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.HashSet;
import java.util.Set;

public class RedisTrustedClient {

    private static Jedis cli = null;
    private static Key keySecret = null;
    private static Cipher cipher = null;


    public static void main(String[] args) {
        try {
            setup();

            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

            String command = "";

            while (!(command = br.readLine().trim()).equals("exit")) {

                if (command.equals("insert")) {
                    processInsert(br);
                }
                if (command.equals("get")) {
                    processGetByName(br);
                }
            }

            System.out.println("Bye");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static void setup() throws NoSuchPaddingException, NoSuchAlgorithmException, NoSuchProviderException {
        String redisServer = System.getenv("REDIS_SERVER");
        System.out.println("REDIS_SERVER: " + redisServer != null ? redisServer : "localhost");

        cli = new Jedis(redisServer, 6379);
        cipher = Cipher.getInstance("blowfish/ECB/PKCS5Padding", "SunJCE");

        keySecret = new SecretKeySpec("Passwords".getBytes(), "blowfish");
    }

    private static void processInsert(BufferedReader br) throws IOException {
        System.out.println("Insert First Name:\n");
        String name = br.readLine().trim();

        System.out.println("Insert Last Name:\n");
        String lastName = br.readLine().trim();

        System.out.println("Insert Salary:\n");
        String salary = br.readLine().trim();

        boolean inserted = jedisInsert(name, lastName, salary);

        System.out.println("Insert " + (inserted ? "Success" : "Failure"));
        System.out.println("-----------\n");
    }

    private static void processGetByName(BufferedReader br) throws InvalidKeyException, DecoderException, BadPaddingException, IllegalBlockSizeException, IOException {
        System.out.println("Search by Name:");
        String name = br.readLine().trim();

        Set<String> rst = new HashSet();

        String key = String.valueOf(name.hashCode());
        Set<String> indexes = cli.smembers(key);
        System.out.println("Number of entries: " + indexes.size());

        cipher.init(Cipher.DECRYPT_MODE, keySecret);

        for (String id : indexes) {
            rst.add(new String(cipher.doFinal(Hex.decodeHex(cli.get(id)))));
        }

        for (String row : rst) {
            System.out.println(row);
        }
    }

    private static boolean jedisInsert(String name, String lastName, String salary) {

        String row = String.format("%s:%s:%s", name, lastName, salary);

        try {
            cipher.init(Cipher.ENCRYPT_MODE, keySecret);

            byte[] ecryptedCore = cipher.doFinal(row.getBytes());

            String key = String.valueOf(row.hashCode());
            System.out.println("Key: " + key);

            cli.set(key, Hex.encodeHexString(ecryptedCore));
            cli.sadd(String.valueOf(name.hashCode()), key);

            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
