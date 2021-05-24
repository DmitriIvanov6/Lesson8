import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Server {
    private int port;
    private List<ClientHandler> clients;

    public Server(int port) {
        this.port = port;
        this.clients = new ArrayList();

        try {
            ServerSocket serverSocket = new ServerSocket(port);
            Throwable var3 = null;

            try {
                System.out.println("Сервер запущен на порту " + port + "...");

                while (true) {
                    System.out.println("Ждём нового клиента...");
                    Socket socket = serverSocket.accept();
                    System.out.println("Клиент подключился");
                    new ClientHandler(socket, this);
                }
            } catch (Throwable var12) {
                var3 = var12;
                throw var12;
            } finally {
                if (serverSocket != null) {
                    if (var3 != null) {
                        try {
                            serverSocket.close();
                        } catch (Throwable var11) {
                            var3.addSuppressed(var11);
                        }
                    } else {
                        serverSocket.close();
                    }
                }

            }
        } catch (IOException var14) {
            var14.printStackTrace();
        }
    }

    public void subscribe(ClientHandler clientHandler) {
        this.clients.add(clientHandler);
        this.broadcastMessage("Клиент " + clientHandler.getUsername() + " подлючился \n");
        this.broadcastClientList();
    }

    public void unsubscribe(ClientHandler clientHandler) {
        this.clients.remove(clientHandler);
        this.broadcastMessage("Клиент " + clientHandler.getUsername() + " отключился \n");
        this.broadcastClientList();
    }

    public void broadcastMessage(String message) {
        Iterator var2 = this.clients.iterator();

        while (var2.hasNext()) {
            ClientHandler clientHandler = (ClientHandler) var2.next();
            clientHandler.sendMessage(message);
        }

    }

    public void sendPrivateMessage(ClientHandler sender, String receiverUsername, String message) {
        Iterator var4 = this.clients.iterator();

        ClientHandler client;
        do {
            if (!var4.hasNext()) {
                sender.sendMessage("Невозможно отправить сообщение пользователю: " + receiverUsername + ". Такого пользователя нет.");
                return;
            }

            client = (ClientHandler) var4.next();
        } while (!client.getUsername().equals(receiverUsername));

        client.sendMessage("От: " + sender.getUsername() + " Сообщение: " + message);
        sender.sendMessage("Пользователю: " + receiverUsername + " Сообщение: " + message);
    }

    public boolean isUserOnline(String nickname) {
        Iterator var2 = this.clients.iterator();

        ClientHandler clientHandler;
        do {
            if (!var2.hasNext()) {
                return false;
            }

            clientHandler = (ClientHandler) var2.next();
        } while (!clientHandler.getUsername().equals(nickname));

        return true;
    }

    private void broadcastClientList() {
        StringBuilder stringBuilder = new StringBuilder("/clients_list ");
        Iterator var2 = this.clients.iterator();

        while (var2.hasNext()) {
            ClientHandler client = (ClientHandler) var2.next();
            stringBuilder.append(client.getUsername()).append(" ");
        }

        stringBuilder.setLength(stringBuilder.length() - 1);
        String clientsList = stringBuilder.toString();
        Iterator var6 = this.clients.iterator();

        while (var6.hasNext()) {
            ClientHandler clientHandler = (ClientHandler) var6.next();
            clientHandler.sendMessage(clientsList);
        }

    }
}
