package unl.fct.srsc.client;

import com.github.javafaker.Faker;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;
import unl.fct.srsc.client.config.Configurations;
import unl.fct.srsc.client.config.SecurityConfig;
import unl.fct.srsc.client.config.TpmHostsConfig;
import unl.fct.srsc.client.tpm.TpmConnector;
import unl.fct.srsc.client.utils.Utils;

import javax.crypto.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.URI;
import java.security.*;
import java.util.*;

public class RedisTrustedClient {

    private static int numberOfOps = 1000;

    private static final String REDIS_SERVER = "REDIS_SERVER";

    private static SecurityConfig securityConfig;
    private static TpmHostsConfig tpmHostsConfig;

    private static String redisServer;
    private static Jedis cli = null;

    private static List<String> indexes = new ArrayList<String>();

    private static Key keySecret = null;
    private static KeyPair keyPair = null;

    private static Cipher cipher = null;

    public static void main(String[] args) {
        try {
            setup();

            long globalStart = System.currentTimeMillis();

            if (checkTpm()) {
                connectRedis();

                System.out.println("-------------- Start Benchmark ------------");
                System.out.println("Set " + numberOfOps + " entries");
                long setTime = processPopulate();

                System.out.println("Get " + numberOfOps + " entries");
                long getTime = getAll();

                System.out.println("Remove " + numberOfOps + " entries\n");
                long removeTime = remove();

                long globalEnd = System.currentTimeMillis();

                System.out.println("Total set time -------> " + setTime + "ms");
                System.out.println("Total get time -------> " + getTime + "ms");
                System.out.println("Total remove time -------> " + removeTime + "ms\n");

                System.out.println("Total time  -------> " + (removeTime + getTime + setTime) + "ms");
                System.out.println("Total time with TPM check  -------> " + (globalEnd - globalStart) + "ms");
                System.out.println("Total operations  -------> 3000 ops");
                System.out.println("Total operations per second -------> " + 3000 / ((removeTime + getTime + setTime) / 1000) + "ops/s\n");

                /*String command = "";

                while (!(command = br.readLine().trim()).equals("exit")) {

                    if (command.equals("insert")) {
                        processInsert(br);
                    }
                    if (command.equals("get")) {
                        processGetByName(br);
                    }
                    if (command.equals("populate")) {
                        processPopulate(br);
                    }

                    if (command.equals("getAll")) {
                        printAllEntries();
                    }
                }
                */
            }

            System.out.println("Bye");
        } catch (JedisConnectionException uh) {
            uh.printStackTrace();
            System.out.println("REDIS_SERVER didn't respond.");
            System.exit(1);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }


    private static void setup() throws NoSuchPaddingException, NoSuchAlgorithmException, NoSuchProviderException {
        String numberOfOpsEnv = System.getenv("NUMBER_OF_OPS");
        numberOfOps = numberOfOpsEnv == null ? numberOfOps : Integer.parseInt(numberOfOpsEnv);

        //Configurations
        Configurations confs = Utils.readFromConfig();
        securityConfig = confs.getSecurityConfig();
        tpmHostsConfig = confs.getTpmHosts();

        cipher = Cipher.getInstance(securityConfig.getCiphersuite(), securityConfig.getProvider());

        keySecret = Utils.getKeyFromKeyStore(securityConfig);
        keyPair = Utils.getKeyPairFromKeyStore(securityConfig);
    }

    private static void connectRedis() {

        redisServer = securityConfig.getRedisServer();
        System.out.println(REDIS_SERVER + " : " + redisServer);

        cli = new Jedis("localhost", 6379);
        if (securityConfig.getRedisPassword() != null) {
            cli.auth(securityConfig.getRedisPassword());
        }
        cli.ping(); //pinging database
    }


    private static boolean checkTpm() {
        TpmConnector tpmConnector = new TpmConnector(tpmHostsConfig);

        return tpmConnector.checkTpm();
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
            //System.out.println("Key: " + key);

            row = Hex.encodeHexString(ecryptedCore);

            Mac hMac = Mac.getInstance(securityConfig.getHmac(), securityConfig.getProvider());
            hMac.init(keySecret);
            String mac = Hex.encodeHexString(hMac.doFinal(row.getBytes()));

            String rowIntegrity = String.format("%s:%s", row, mac);

            cli.set(key, rowIntegrity);
            cli.sadd(String.valueOf(name.hashCode()), key);

            indexes.add(String.valueOf(key));

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
                    e.printStackTrace();
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

        //System.out.println("Integrity OK");
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
                //System.out.println("Assinatura validada - reconhecida");
                return realRow;
            } else {
                System.out.println("Assinatura nao reconhecida");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String decryptRow(String row) throws DecoderException, BadPaddingException, IllegalBlockSizeException, InvalidKeyException {
        cipher.init(Cipher.DECRYPT_MODE, keySecret);
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

    private static long processPopulate() throws BadPaddingException {
        Faker faker = new Faker();
        long start = System.currentTimeMillis();

        for (int i = 0; i < numberOfOps; i++) {
            String firstName = faker.name().firstName();
            String lastName = faker.name().lastName();
            Random rand = new Random();
            int integerValue = rand.nextInt(200000);
            String value = String.valueOf(integerValue);

            jedisInsert(firstName, lastName, value);
        }
        return System.currentTimeMillis() - start;
    }


    private static long getAll() throws InvalidKeyException, DecoderException, NoSuchProviderException, NoSuchAlgorithmException {
        long startTime = System.currentTimeMillis();

        for (String id : indexes) {
            String uncheckedRow = cli.get(id);

            if (checkIntegrity(uncheckedRow)) {
                try {
                    //split to remove integrity field
                    String[] splitted = uncheckedRow.split("\\:");

                    String row = decryptRow(splitted[0]);
                    String authenticRow = checkAuthenticity(row);

                } catch (Exception e) {
                    System.out.println("An error occurred while decrypting row...");
                    e.printStackTrace();
                }
            }
        }
        return System.currentTimeMillis() - startTime;
    }

    private static long remove() {
        long start = System.currentTimeMillis();

        try {
            for (int i = 0; i < numberOfOps; i++) {

                Random r = new Random();
                int rdm = r.nextInt(indexes.size());
                String keyword = indexes.get(rdm);

                String uncheckedRow = cli.get(keyword);
                if (checkIntegrity(uncheckedRow)) {
                    //split to remove integrity field
                    String[] splitted = uncheckedRow.split("\\:");

                    String row = decryptRow(splitted[0]);
                    String authenticRow = checkAuthenticity(row);

                    for (int j = 1; j < splitted.length; j++) {
                        cli.srem(String.valueOf(splitted[j].hashCode()), keyword);
                    }

                    String randomString = generateRandomStr(100);
                    cli.set(keyword, randomString); //No need to encrypt random data
                    assert cli.get(keyword).equals(randomString);

                    cli.del(keyword);
                    indexes.remove(rdm);
                }
            }
        } catch (Exception e) {
            System.out.println("An error occurred while decrypting row...");
            e.printStackTrace();
        }
        return System.currentTimeMillis() - start;
    }

    private static String generateRandomStr(int size) {
        String str = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        Random r = new Random();
        StringBuilder s = new StringBuilder(size);
        for (int i = 0; i < size; i++)
            s.append(str.charAt(r.nextInt(str.length())));
        return s.toString();
    }
}