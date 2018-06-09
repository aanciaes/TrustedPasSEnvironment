package unl.fct.srsc.tpm;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class TpmStateData {

    private static BufferedWriter p_stdin;
    private static Process p;
    private static List<String> state = new ArrayList<String>(10);

    public static void main(String[] args) throws IOException {

        startTerminal();
        runCommand("ps -ef");
        print();

        String line = state.get(1);
        String[] newLine = line.split("\\s+");

        if(!newLine[1].trim().equals("1") || !newLine[7].trim().equals("/sbin/init")){
            System.out.println("Corrupted");
        }
        else{
            System.out.println("All Ok!");
        }

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
