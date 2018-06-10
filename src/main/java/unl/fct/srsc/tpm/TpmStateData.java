package unl.fct.srsc.tpm;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class TpmStateData {

    public static List<String> getState() throws IOException {

        Process process = Runtime.getRuntime().exec("ps -U root");
        return print(process);

    }

    private static List<String> print(Process p) throws IOException {

        List<String> state = new ArrayList<String>(10);
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
