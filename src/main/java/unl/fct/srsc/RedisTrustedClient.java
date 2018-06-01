package unl.fct.srsc;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import redis.clients.jedis.Jedis;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.*;
import java.util.HashSet;
import java.util.Set;

public class RedisTrustedClient {

    private static Jedis cli = null;
    private static Key keySecret = null;
    private static Cipher cipher = null;

    private static final int COLUMN_NUMBER = 3;


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
        System.out.println("Insert First Name:");
        String name = br.readLine().trim();

        System.out.println("\nInsert Last Name:");
        String lastName = br.readLine().trim();

        System.out.println("\nInsert Salary:");
        String salary = br.readLine().trim();

        boolean inserted = jedisInsert(name, lastName, salary);

        System.out.println("Insert " + (inserted ? "Success" : "Failure"));
        System.out.println("-----------\n");
    }

    private static void processGetByName(BufferedReader br) throws IOException {
        System.out.println("Search by Name:");
        String name = br.readLine().trim();

        try {
            Set<String> rst = jedisGetByName(name);

            for (String row : rst) {
                System.out.println(row);
            }

        }catch (Exception e) {
            System.out.println("An error occured...");
            e.printStackTrace();
        }
    }

    private static boolean jedisInsert(String name, String lastName, String salary) {

        String row = String.format("%s:%s:%s", name, lastName, salary);

        try {
            cipher.init(Cipher.ENCRYPT_MODE, keySecret);

            byte[] ecryptedCore = cipher.doFinal(row.getBytes());

            String key = String.valueOf(row.hashCode());
            System.out.println("Key: " + key);

            row = Hex.encodeHexString(ecryptedCore);

            Mac hMac = Mac.getInstance("HMacSHA1", "SunJCE");
            hMac.init(keySecret);
            String mac = Hex.encodeHexString(hMac.doFinal(row.getBytes()));

            String rowIntegrity = String.format("%s:%s", row, mac);

            cli.set(key, rowIntegrity);
            cli.sadd(String.valueOf(name.hashCode()), key);

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static Set<String> jedisGetByName(String name) throws InvalidKeyException, DecoderException, BadPaddingException, IllegalBlockSizeException, NoSuchProviderException, NoSuchAlgorithmException {
        Set<String> rst = new HashSet();

        String key = String.valueOf(name.hashCode());
        Set<String> indexes = cli.smembers(key);
        System.out.println("Number of entries: " + indexes.size());

        cipher.init(Cipher.DECRYPT_MODE, keySecret);

        for (String id : indexes) {
            String uncheckedRow = cli.get(id);

            if (checkIntegrity(uncheckedRow)){
                try {
                    String row = decryptRow(uncheckedRow);
                    rst.add(row);
                }catch (Exception e){}
            }
        }
        return rst;
    }

    private static boolean checkIntegrity (String row) throws DecoderException, NoSuchProviderException, NoSuchAlgorithmException, InvalidKeyException {

        String[] columns = row.split("\\:");

        Mac hMac = Mac.getInstance("HMacSHA1", "SunJCE");
        hMac.init(keySecret);

        byte[] integrity = Hex.decodeHex(columns[1]);

        if (!MessageDigest.isEqual(hMac.doFinal(columns[0].getBytes()), integrity)) {
            System.out.println("Integrity Broken");
            return false;
        }

        System.out.println("Integrity OK");
        return true;
    }

    private static String decryptRow (String row) throws DecoderException, BadPaddingException, IllegalBlockSizeException {
        return new String(cipher.doFinal(Hex.decodeHex(row)));
    }
}
