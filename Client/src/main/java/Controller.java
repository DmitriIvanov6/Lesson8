import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;

public class Controller {


    @FXML
    TextField msgField, usernameField, passwordField;

    @FXML
    TextArea msgArea;

    @FXML
    HBox loginPanel, msgPanel;

    @FXML
    ListView<String> clientList;

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String username;

    public void sendMsg(ActionEvent actionEvent) {
        if (!msgField.getText().isEmpty()){
            String msg = msgField.getText() + '\n';
            try {
                out.writeUTF(msg);
                msgField.clear();
            } catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Невозможно отправить сообщение", ButtonType.OK);
                alert.showAndWait();
            }
        }

    }

    public void login(ActionEvent actionEvent) {
        if (socket == null || socket.isClosed()) {
            connect();
            ClientHistory.loggingStart(usernameField.getText());
        }

        if (usernameField.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Имя пользователя не может быть пустым",
                    ButtonType.OK);
            alert.showAndWait();
            return;
        }
        if (passwordField.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Поле с паролем не может быть пустым",
                    ButtonType.OK);
            alert.showAndWait();
            return;
        }

        try {
            out.writeUTF("/login " + usernameField.getText() + " " + passwordField.getText());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setUsername(String username) {
        this.username = username;
        if (username != null) {
            loginPanel.setVisible(false);
            loginPanel.setManaged(false);
            msgPanel.setVisible(true);
            msgPanel.setManaged(true);
        } else {
            loginPanel.setVisible(true);
            loginPanel.setManaged(true);
            msgPanel.setVisible(false);
            msgPanel.setManaged(false);
        }
    }

    public void connect() {
        try {
            socket = new Socket("localhost", 8189);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            Thread dataThread = new Thread(() -> {
                try {
                    //цикл авторизации
                    while (true) {
                        String msg = in.readUTF();

                        //login_ok
                        if (msg.startsWith("/login_ok ")) {
                            setUsername(msg.split("\\s")[1]);
                            break;
                        }

                        if (msg.startsWith("/login_failed ")) {
                            String cause = msg.split("\\s", 2)[1];
                            msgArea.appendText(cause + '\n');
                        }

                    }

                    //цикл общения
                    while (true) {
                        String msg = in.readUTF();
                        if (msg.startsWith("/")) {
                            executeCommand(msg);
                            continue;
                        }
                        msgArea.appendText(msg);
                        ClientHistory.logHistory(msg);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    disconnect();
                }
            });
            dataThread.start();

        } catch (IOException e) {
            throw new RuntimeException("Unable to connect to server [localhost: 8189]");
        }
    }


    private void executeCommand(String cmd) {
        if (cmd.startsWith("/clients_list ")) {
            String[] tokens = cmd.split("\\s");

            Platform.runLater(() -> {
                clientList.getItems().clear();
                for (int i = 1; i < tokens.length; i++) {
                    clientList.getItems().add(tokens[i]);
                }
            });
        }
        if (cmd.startsWith(("/log "))){
            msgArea.appendText(ClientHistory.chatLog(cmd));

        }
    }

    public void disconnect() {
        ClientHistory.loggingStop();
        setUsername(null);
        if (socket != null) {
            try {
                socket.close();
                msgArea.clear();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
