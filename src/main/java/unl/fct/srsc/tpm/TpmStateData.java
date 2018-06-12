package unl.fct.srsc.tpm;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TpmStateData {

    private static final String PS = "ps -o comm --no-heading";
    private static final String DOCKER_PS = "docker ps";
    private static final String DOCKER_EXEC = "docker exec -t";
    private static final String SHA_REDIS = "sha256sum /usr/local/bin/redis-server";
    private static final String DOCKER_EXEC_PS = "ps -eo comm --no-heading";

    public static List<String> getState() throws IOException {

        List<String> result = new ArrayList<String>();
        result.add(0,runCommand(PS));

        String id = getRedisContainer(runCommand(DOCKER_PS));
        result.add(1, clear(runCommand(DOCKER_EXEC + " " + id + " " + SHA_REDIS)));

        result.set(0, result.get(0) + runCommand(DOCKER_EXEC + " " + id + " " + DOCKER_EXEC_PS));

        return result;
    }

    private static String clear(String print) {
        String[] rst = print.split("&");

        return rst[0];

    }

    private static String getRedisContainer(String ps) {

        String[] splitted = ps.split(":");
        for(String line : splitted){
            if(line.contains("redis")){
                String[] splited = line.split("&");
                return splited[0];
            }
        }
        return "";
    }

    private static String runCommand(String command) throws IOException {

        Process p = Runtime.getRuntime().exec(command);

        String state ="";
        String line;
        BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
        int n = 0;

        while ((line = br.readLine()) != null) {
            String[] newLine = line.split("\\s+");
            String finalLine = "";
            for(int x= 0; x < newLine.length; x++){
                finalLine += newLine[x] + "&";
            }

            state += finalLine + ":";
        }
        br.close();

        return state;
    }
}
