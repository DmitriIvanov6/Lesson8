import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class ClientHistory {
    private static OutputStreamWriter out;

    public static void loggingStart(String lgn) {

        try {
            out = new OutputStreamWriter(new FileOutputStream("history_" + lgn + ".txt", true), StandardCharsets.UTF_8);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void logHistory(String msg) {
        try {
            out.write(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loggingStop() {
        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String chatLog(String msg) {
        String[] arr = msg.split("\\s", 2);
        StringBuilder sb = new StringBuilder();
        String log = "";
        for (int i = 1; i < arr.length; i++) {
            sb.append(arr[i]).append("\n");
            log = sb.toString();
        }
        return log;

    }


}
