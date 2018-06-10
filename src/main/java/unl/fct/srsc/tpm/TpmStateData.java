package unl.fct.srsc.tpm;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TpmStateData {

    public static List<String> getState() throws IOException {

        List<String> result = new ArrayList<String>();
        Process process = Runtime.getRuntime().exec("ps -eo user,comm --no-heading");
        result.addAll(print(process));

        process = Runtime.getRuntime().exec("docker ps");
        String id = getRedisContainer(print(process));

        process = Runtime.getRuntime().exec("docker exec -t " + id + " sha256sum /user/local/bin/redis-server");

        result.addAll(clear(print(process)));
        return result;
    }

    private static List<String> clear(List<String> print) {
        List<String> rst = new ArrayList<String>();

        System.out.println("redis line size " + print.size());
        String[] t = print.get(0).split("\\s+");
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

    private static List<String> print(Process p) throws IOException {

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
        System.out.println("OUT PRINT");
        br.close();

        return state;
    }
}
