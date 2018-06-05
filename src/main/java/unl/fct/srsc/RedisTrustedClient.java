package unl.fct.srsc;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;
import unl.fct.srsc.config.SecurityConfig;
import unl.fct.srsc.utils.Utils;

import javax.crypto.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.*;
import java.util.HashSet;
import java.util.Set;

public class RedisTrustedClient {

    private static final String REDIS_SERVER = "REDIS_SERVER";
    private static final String LOCALHOST = "localhost";

    private static SecurityConfig securityConfig;
    private static Jedis cli = null;

    private static Key keySecret = null;
    private static KeyPair keyPair = null;

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
        } catch (JedisConnectionException uh) {
            System.out.println("REDIS_SERVER didn't respond.");
            System.exit(1);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }


    private static void setup() throws NoSuchPaddingException, NoSuchAlgorithmException, NoSuchProviderException {
        String redisServer = System.getenv(REDIS_SERVER);
        redisServer = redisServer == null ? LOCALHOST : redisServer;

        securityConfig = Utils.readFromConfig();

        cli = new Jedis(redisServer, 6379);
        cli.ping(); //pinging database

        System.out.println(REDIS_SERVER + " : " + redisServer);

        cipher = Cipher.getInstance(securityConfig.getCiphersuite(), securityConfig.getProvider());

        keySecret = Utils.getKeyFromKeyStore(securityConfig);
        keyPair = Utils.getKeyPairFromKeyStore(securityConfig);
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

            prettyPrint(rst);

        } catch (Exception e) {
            System.out.println("An error occured...");
            e.printStackTrace();
        }
    }

    private static boolean jedisInsert(String name, String lastName, String salary) {

        String row = String.format("%s:%s:%s", name, lastName, salary);
        row = signRow(row);

        try {
            cipher.init(Cipher.ENCRYPT_MODE, keySecret);

            byte[] ecryptedCore = cipher.doFinal(row.getBytes());

            String key = String.valueOf(row.hashCode());
            System.out.println("Key: " + key);

            row = Hex.encodeHexString(ecryptedCore);

            Mac hMac = Mac.getInstance(securityConfig.getHmac(), securityConfig.getProvider());
            hMac.init(keySecret);
            String mac = Hex.encodeHexString(hMac.doFinal(row.getBytes()));

            String rowIntegrity = String.format("%s:%s", row, mac);

            cli.set(key, rowIntegrity);
            cli.sadd(String.valueOf(name.hashCode()), key);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static String signRow(String row) {

        try {
            Signature signature = Signature.getInstance(securityConfig.getSignatureAlgorithm(),
                    securityConfig.getSignatureAlgProvider());

            signature.initSign(keyPair.getPrivate(), Utils.createFixedRandom());

            signature.update(row.getBytes());
            byte[] sigBytes = signature.sign();

            return String.format("%s:%s", row, Hex.encodeHexString(sigBytes));

        } catch (Exception e) {
            e.printStackTrace();
            return null;
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

            if (checkIntegrity(uncheckedRow)) {
                try {
                    //split to remove integrity field
                    String[] splitted = uncheckedRow.split("\\:");

                    String row = decryptRow(splitted[0]);
                    String authenticRow = checkAuthenticity(row);
                    rst.add(authenticRow);
                } catch (Exception e) {
                    System.out.println("An error occurred while decrypting row...");
                    //e.printStackTrace();
                }
            }
        }
        return rst;
    }

    private static boolean checkIntegrity(String row) throws DecoderException, NoSuchProviderException, NoSuchAlgorithmException, InvalidKeyException {

        String[] columns = row.split("\\:");

        Mac hMac = Mac.getInstance(securityConfig.getHmac(), securityConfig.getProvider());
        hMac.init(keySecret);

        byte[] integrity = Hex.decodeHex(columns[1]);

        if (!MessageDigest.isEqual(hMac.doFinal(columns[0].getBytes()), integrity)) {
            System.out.println("Integrity Broken");
            return false;
        }

        System.out.println("Integrity OK");
        return true;
    }

    private static String checkAuthenticity(String row) {
        String[] splitted = row.split("\\:");

        //TODO: make it better
        String realRow = String.format("%s:%s:%s", splitted[0], splitted[1], splitted[2]);
        String signatureField = splitted[3];

        try {
            Signature signature = Signature.getInstance(securityConfig.getSignatureAlgorithm(),
                    securityConfig.getSignatureAlgProvider());

            signature.initVerify(keyPair.getPublic());

            signature.update(realRow.getBytes());

            if (signature.verify(Hex.decodeHex(signatureField))) {
                System.out.println("Assinatura validada - reconhecida");
                return realRow;
            } else {
                System.out.println("Assinatura nao reconhecida");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String decryptRow(String row) throws DecoderException, BadPaddingException, IllegalBlockSizeException {
        return new String(cipher.doFinal(Hex.decodeHex(row)));
    }

    private static void prettyPrint(Set<String> rows) {

        System.out.println("-----------------------------------------------------");
        System.out.println("|    FirstName    |    LastName    |      Salary    |");
        System.out.println("-----------------------------------------------------");

        for (String row : rows) {
            System.out.println(row);
        }
    }
}