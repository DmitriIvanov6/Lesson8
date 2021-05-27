import java.sql.*;

public class SQLSupport {
    private static Connection connection;
    private static Statement stmt;
    public static void connectSQL() throws Exception {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:chat.db");
        stmt = connection.createStatement();
    }

    public static void disconnectSQL() {
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

    public static void sqlChangeName(String lgn, String newLgn) {
        try {
            connectSQL();
            PreparedStatement ps = connection.prepareStatement("UPDATE chatLogin SET login=? WHERE login=?;");
            ps.setString(1, newLgn);
            ps.setString(2, lgn);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            disconnectSQL();
        }

    }

    public static void sqlRegister(String lgn, String pswrd) {
        try {
            connectSQL();
            PreparedStatement ps = connection.prepareStatement("INSERT INTO chatLogin (login, password) VALUES (?, ?);");
            ps.setString(1, lgn);
            ps.setString(2, pswrd);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            disconnectSQL();
        }
    }

    public static boolean sqlCheckLogin(String lgn) {
        boolean check = false;
        try {
            connectSQL();
            PreparedStatement ps = connection.prepareStatement("SELECT login FROM chatLogin WHERE login = ?;");
            ps.setString(1, lgn);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                check = true;
            }
            rs.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            disconnectSQL();
        }
        return check;
    }

    public static boolean sqlCheckPassword(String lgn, String pswrd) {
        boolean check = false;
        try {
            connectSQL();
            PreparedStatement ps = connection.prepareStatement("SELECT login, password FROM chatLogin WHERE login = ?;");
            ps.setString(1, lgn);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String pass = rs.getString("password");
                if (pass.equals(pswrd)) {
                    check = true;
                }
            }
            rs.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            disconnectSQL();
        }
        return check;
    }
}
