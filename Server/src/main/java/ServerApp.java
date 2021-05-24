import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class ServerApp {
    private static Connection connection;
    private static Statement stmt;

    private static void connect() throws  Exception {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:chat.db");
        stmt = connection.createStatement();
    }
    private static void disconnect()  {
        try {
            stmt.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        try {
            connection.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public ServerApp() {
    }

    public static void main(String[] args) {

        try {
            connect();
            System.out.println("Db connect OK");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            disconnect();
        }

        new Server(8189);
    }

}