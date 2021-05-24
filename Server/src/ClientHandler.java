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

            } catch (IOException var7) {
                var7.printStackTrace();
            } finally {
                this.disconnect();
            }

        })).start();
    }

    private void executeCommand(String cmd) throws IOException {

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
            if (!server.isUserOnline(usernameFromLogin)) {
                this.username = usernameFromLogin;
                this.sendMessage("/login_ok " + this.username);
                server.subscribe(this);
            } else {
                this.sendMessage("/login_failed Current nickname has already been occupied");
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
