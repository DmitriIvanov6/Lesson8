import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    private Socket socket;
    private Server server;
    private DataInputStream in;
    private DataOutputStream out;
    private String username;
    private String password;





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
                        ServerLogging.logging(this.username + ": " + msg);
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
            if (!SQLSupport.sqlCheckLogin(newLogin)) {
                SQLSupport.sqlChangeName(this.username, newLogin);
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
                if (!SQLSupport.sqlCheckLogin(this.username)) {
                    SQLSupport.sqlRegister(this.username, this.password);
                }
                if (SQLSupport.sqlCheckPassword(this.username, this.password)) {
                    this.sendMessage("/login_ok " + this.username);
                    server.subscribe(this);
                    this.sendMessage("/log " + ServerLogging.readingLog());
                } else {
                    this.sendMessage("/login_failed Неправильный пароль!");
                }

            } else {
                this.sendMessage("/login_failed Пользователь уже в чате!");
            }
        }
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
