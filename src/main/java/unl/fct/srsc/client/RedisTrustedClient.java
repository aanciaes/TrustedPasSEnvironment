package unl.fct.srsc.client;

import com.github.javafaker.Faker;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;
import unl.fct.srsc.client.config.Configurations;
import unl.fct.srsc.client.config.GosTpm;
import unl.fct.srsc.client.config.SecurityConfig;
import unl.fct.srsc.client.config.VmsTpm;
import unl.fct.srsc.client.tpm.TpmConnector;
import unl.fct.srsc.client.utils.Utils;

import javax.crypto.*;
import java.security.*;
import java.util.*;

public class RedisTrustedClient {

    private static int numberOfOps = 1000;

    private static final String REDIS_SERVER = "REDIS_SERVER";
    private static final String LOCALHOST = "localhost";

    private static SecurityConfig securityConfig;
    private static VmsTpm vmsTpm;
    private static GosTpm gosTpm;

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
                System.out.println("Total operations  -------> " + (numberOfOps * 3) + " ops");
                System.out.println("Total operations per second -------> " + (numberOfOps*3) / ((removeTime + getTime + setTime) / 1000) + "ops/s\n");
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
        vmsTpm = confs.getVmsTpm();
        gosTpm = confs.getGosTpm();

        cipher = Cipher.getInstance(securityConfig.getCiphersuite(), securityConfig.getProvider());

        keySecret = Utils.getKeyFromKeyStore(securityConfig);
        keyPair = Utils.getKeyPairFromKeyStore(securityConfig);
    }

    private static void connectRedis() {

        redisServer = securityConfig.getRedisServer();
        System.out.println(REDIS_SERVER + " : " + redisServer);

        cli = new Jedis(LOCALHOST, 6379);
        if (securityConfig.getRedisPassword() != null) {
            cli.auth(securityConfig.getRedisPassword());
        }
        cli.ping(); //pinging database
    }


    private static boolean checkTpm() {
        TpmConnector tpmConnector = new TpmConnector(vmsTpm);

        return tpmConnector.checkTpm();
    }

    private static boolean jedisInsert(String name, String lastName, String salary, String address, String catName, String gotHouse) {

        String row = String.format("%s:%s:%s:%s:%s:%s", name, lastName, salary, address, catName, gotHouse);
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
            cli.sadd(String.valueOf(lastName.hashCode()), key);
            cli.sadd(String.valueOf(salary.hashCode()), key);
            cli.sadd(String.valueOf(address.hashCode()), key);
            cli.sadd(String.valueOf(catName.hashCode()), key);
            cli.sadd(String.valueOf(gotHouse.hashCode()), key);

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
        String realRow = String.format("%s:%s:%s:%s:%s:%s", splitted[0], splitted[1], splitted[2], splitted[3], splitted[4], splitted[5]);
        String signatureField = splitted[6];

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

        System.out.println("--------------------------------------------------------------------------------------------------------");
        System.out.println("|    FirstName    |    LastName    |      Salary    |    Address    |    CatName    |      GOTHouse    |");
        System.out.println("--------------------------------------------------------------------------------------------------------");

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
            String streetAddress = faker.address().streetAddress();
            String catName = faker.cat().name();
            String gotHouse = faker.gameOfThrones().house();
            Random rand = new Random();
            int integerValue = rand.nextInt(200000);
            String value = String.valueOf(integerValue);

            jedisInsert(firstName, lastName, value, streetAddress, catName, gotHouse);
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