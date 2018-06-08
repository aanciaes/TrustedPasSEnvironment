package unl.fct.srsc.tpm;

import java.io.*;
import java.util.Scanner;

public class TpmStateData {

    private static BufferedWriter p_stdin;
    private static Process p;

    public static void main(String[] args) throws IOException {
        startTerminal();
        runCommand("ps -ef");
        print();

    }

    private static void runCommand(String Command) throws IOException {
        p_stdin.write(Command);
        p_stdin.newLine();
        p_stdin.flush();
    }

    private static void print() throws IOException {
        String line;
        BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));

        while ((line = br.readLine()) != null) {
            System.out.println( br.readLine() );
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
