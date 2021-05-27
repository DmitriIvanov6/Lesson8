import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class ServerApp {


    public ServerApp() {
    }

    public static void main(String[] args) {

        try {
            SQLSupport.connectSQL();
            System.out.println("Db connect OK");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            SQLSupport.disconnectSQL();
        }

        new Server(8189);
    }

}