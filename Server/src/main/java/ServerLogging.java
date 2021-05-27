import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class ServerLogging {

    public static void logging(String msg) {
        try (OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream("serverLog.txt", true), StandardCharsets.UTF_8)) {
            out.write(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static String readingLog() {
        StringBuilder sb = new StringBuilder();
        if (!Files.exists(Paths.get("serverLog.txt"))){
            return "";
        }
        try {
            List<String> log = Files.readAllLines(Paths.get("serverLog.txt"));
            int startAt = 0;
            if (log.size() > 100) {
                startAt = log.size() - 100;
            }
            for (int i = startAt; i < log.size(); i++){
                sb.append(log.get(i)).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

}
