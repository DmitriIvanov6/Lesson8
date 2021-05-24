import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.*;

public class ClientHandler {
    private Socket socket;
    private Server server;
    private DataInputStream in;
    private DataOutputStream out;
    private String username;
    private String password;
    private static Connection connection;
    private static Statement stmt;

    private static void connectSQL() throws Exception {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:chat.db");
        stmt = connection.createStatement();
    }

    private static void disconnectSQL() {
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


    public ClientHandler(Socket socket, Server server) throws IOException {
        this.socket = socket;
        this.in = new DataInputStream(socket.getInputStream());
        this.out = new DataOutputStream(socket.getOutputStream());
        this.server = server;
        (new Thread(() -> {
            try {
                while (true) {
                    String msg = this.in.readUTF();
                    if (msg.startsWith("/")) {
                        this.executeCommand(msg);
                    } else {
                        server.broadcastMessage(this.username + ": " + msg);
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                this.disconnect();
            }

        })).start();
    }

    private void executeCommand(String cmd) throws IOException {
        if (cmd.startsWith("/rename")) {
            String newLogin = cmd.split("\\s")[1];
            if (!sqlCheckLogin(newLogin)) {
                sqlChangeName(this.username, newLogin);
                this.sendMessage("Вы сменили ник на " + cmd.split("\\s")[1] + ". Войдите заново.\n");
                disconnect();
            } else {
                this.sendMessage("Данный ник уже занят");
            }

        }

        if (cmd.startsWith("/w ")) {
            String[] tokens = cmd.split("\\s", 3);
            this.server.sendPrivateMessage(this, tokens[1], tokens[2]);
        }
        if (cmd.startsWith("/exit")) {
            sendMessage("Вы разлогинились\n");
            disconnect();
            return;
        }
        if (cmd.startsWith("/who_am_i")) {
            sendMessage("Ваш никнейм: " + getUsername() + "\n");

        }
        if (cmd.startsWith("/login ")) {
            String usernameFromLogin = cmd.split("\\s")[1];
            String passwordFromLogin = cmd.split("\\s")[2];
            if (!server.isUserOnline(usernameFromLogin)) {
                this.username = usernameFromLogin;
                this.password = passwordFromLogin;
                if (!sqlCheckLogin(this.username)) {
                    sqlRegister(this.username, this.password);
                }
                if (sqlCheckPassword(this.username, this.password)) {
                    this.sendMessage("/login_ok " + this.username);
                    server.subscribe(this);
                } else {
                    this.sendMessage("/login_failed Неправильный пароль!");
                }

            } else {
                this.sendMessage("/login_failed Пользователь уже в чате!");
            }
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

    public boolean sqlCheckLogin(String lgn) {
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

    public boolean sqlCheckPassword(String lgn, String pswrd) {
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


    private void disconnect() {
        this.server.unsubscribe(this);
        if (this.in != null) {
            try {
                this.in.close();
            } catch (IOException var4) {
                var4.printStackTrace();
            }
        }

        if (this.out != null) {
            try {
                this.out.close();
            } catch (IOException var3) {
                var3.printStackTrace();
            }
        }

        if (this.socket != null) {
            try {
                this.socket.close();
            } catch (IOException var2) {
                var2.printStackTrace();
            }
        }

    }

    public void sendMessage(String message) {
        try {
            this.out.writeUTF(message);
        } catch (IOException var3) {
            this.disconnect();
        }

    }

    public String getUsername() {
        return this.username;
    }
}
