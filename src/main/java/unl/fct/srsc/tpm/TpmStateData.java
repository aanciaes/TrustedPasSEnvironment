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
    private static final String DOCKER_EXEC_PS = "ps -eo user,comm --no-heading";

    public static List<String> getState() throws IOException {

        List<String> result = new ArrayList<String>();
        result.addAll(runCommand(PS));

        String id = getRedisContainer(runCommand(DOCKER_PS));
        result.addAll(clear(runCommand(DOCKER_EXEC + " " + id + " " + SHA_REDIS)));

        result.addAll(runCommand(DOCKER_EXEC + " " + id + " " + DOCKER_EXEC_PS));

        return result;
    }

    private static List<String> clear(List<String> print) {
        List<String> rst = new ArrayList<String>();
        String[] t = print.get(0).split("&");
        rst.add(t[0]);

        return rst;
    }

    private static String getRedisContainer(List<String> ps) {

        for(String line : ps){
            if(line.contains("redis")){
                String[] splited = line.split("&");
                return splited[0];
            }
        }
        return "";
    }

    private static List<String> runCommand(String command) throws IOException {

        Process p = Runtime.getRuntime().exec(command);

        List<String> state = new ArrayList<String>();
        String line;
        BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
        int n = 0;

        while ((line = br.readLine()) != null) {
            String[] newLine = line.split("\\s+");
            String finalLine = "";
            for(int x= 0; x < newLine.length; x++){
                finalLine += newLine[x] + "&";
            }

            state.add(n++, finalLine);
            System.out.println(finalLine);
        }
        br.close();

        return state;
    }
}
