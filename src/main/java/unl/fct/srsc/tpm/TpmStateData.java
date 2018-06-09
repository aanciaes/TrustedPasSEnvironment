package unl.fct.srsc.tpm;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class TpmStateData {

    private static BufferedWriter p_stdin;
    private static Process p;
    private static List<String> state = new ArrayList<String>(10);

    public static List<String> getState() throws IOException {

        startTerminal();
        runCommand("ps -ef");
        print();
        return state;
    }

    private static void runCommand(String Command) throws IOException {
        p_stdin.write(Command);
        p_stdin.newLine();
        p_stdin.flush();
    }

    private static void print() throws IOException {

        String line;
        BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
        int n = 0;

        while ((line = br.readLine()) != null && n < 10) {
            String[] newLine = line.split("\\s+");
            String finalLine = "";
            for(int x= 0; x < newLine.length; x++){
                finalLine += "|" + newLine[x];
            }

            state.add(n++, line);
            System.out.println(line);
        }

        br.close();
    }

    private static void startTerminal() {

        ProcessBuilder builder = new ProcessBuilder("/bin/bash");
        p = null;

        try {
            p = builder.start();
        } catch (IOException e) {
            System.out.println(e);
        }
        //get stdin of shell
        p_stdin = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
    }
}
