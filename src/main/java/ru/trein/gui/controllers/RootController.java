package ru.trein.gui.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import ru.trein.gui.MainApp;
import ru.trein.gui.nodes.FileTreeItem;
import ru.trein.gui.nodes.Node;
import ru.trein.gui.nodes.RemoteRepoTreeItem;
import ru.trein.gui.util.FTP;
import ru.trein.gui.util.LocalChildrenBuilder;
import ru.trein.gui.util.ResponseParser;
import ru.trein.gui.util.WriterGUI;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.UnknownHostException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.regex.Pattern;

public class RootController {

    @FXML
    private TableView<Node> localFilesTable;
    @FXML
    private TableColumn<Node, ImageView> localImg;
    @FXML
    private TableColumn<Node, String> localFileName;
    @FXML
    private TableColumn<Node, Long> localFileSize;
    @FXML
    private TableColumn<Node, Long> localFileModify;


    @FXML
    private Label remoteServerLabel;
    @FXML
    private TextField hostField;
    @FXML
    private TextField loginField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField portField;

    @FXML
    private TextArea statusArea;
    @FXML
    private TreeView<String> localRepositoryBrowser;
    @FXML
    private TreeView<String> remoteRepoBrowser;

    private LocalChildrenBuilder localChildrenBuilder = new LocalChildrenBuilder();

    public void initialize() {
        TreeItem<String> rootNode = new TreeItem<String>("MyComputer", new ImageView(new Image(getClass().getResourceAsStream("/img/computer.png"))));

        Iterable<Path> rootDirectories = FileSystems.getDefault().getRootDirectories();

        for (Path path : rootDirectories) {
            File file = new File(path.toString());
            Node node = new Node(file.isDirectory(), file.lastModified(), file.length(), file.getAbsolutePath(), file.getAbsolutePath(), localChildrenBuilder);
            if (node.isDirectory())
                rootNode.getChildren().add(new FileTreeItem(node));
        }

        rootNode.setExpanded(true);
        localRepositoryBrowser.setRoot(rootNode);

        Pattern p = Pattern.compile("(\\d+\\.?\\d*)?");
        portField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!p.matcher(newValue).matches()) {
                portField.setText(oldValue);
                return;
            }
            if (!newValue.isEmpty() && Integer.parseInt(newValue) > 65535)
                portField.setText("65535");
        });

        localRepositoryBrowser.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.getButton() != MouseButton.PRIMARY)
                    return;

                FileTreeItem selectedItem = (FileTreeItem) localRepositoryBrowser.getSelectionModel().getSelectedItem();

                if (selectedItem == null)
                    return;

//                System.err.println(selectedItem.getNode().getName());
                showLocalFilesTable(selectedItem);


            }
        });


        localFilesTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        localImg.setCellValueFactory(new PropertyValueFactory<>("img"));
        localFileName.setCellValueFactory(new PropertyValueFactory<>("name"));
        localFileSize.setCellValueFactory(new PropertyValueFactory<>("size"));
        localFileModify.setCellValueFactory(new PropertyValueFactory<>("modify"));


        WriterGUI.getInstance().setOut(statusArea);
        WriterGUI.getInstance().writeStatus("Добро пожаловать!");
    }


    public void handleConnect(MouseEvent mouseEvent) {
        int port;
        char[] password;

        if (mouseEvent.getButton() != MouseButton.PRIMARY)
            return;

        if (fieldIsEmpty(hostField)) {
            MainApp.makeAlertDialog("Поле хоста не может быть пустым!");
            return;
        }

        if (fieldIsEmpty(loginField))
            loginField.setText("anonymous");

        if (fieldIsEmpty(passwordField))
            password = new char[]{};
        else
            password = passwordField.getText().toCharArray();

        if (fieldIsEmpty(portField))
            port = 21;
        else
            port = Integer.parseInt(portField.getText());


        try {
            WriterGUI.getInstance().writeStatus("Попытка подключения к " + hostField.getText());

            if (FTP.getInstance().connect(hostField.getText(), port))
                WriterGUI.getInstance().writeStatus("Подключение выполнено успешно!");
            else {
                WriterGUI.getInstance().writeStatus("Не удалось подключится, проверьте хост и порт.");
                return;
            }

        } catch (UnknownHostException e) {
            WriterGUI.getInstance().writeStatus("Не удалось подключится, проверьте хост и порт.");
            try {
                FTP.getInstance().disconnect();
            } catch (IOException e1) {
                MainApp.makeAlertDialog(e.getMessage());
                return;
            }
            return;
        } catch (IOException e) {
            MainApp.makeAlertDialog(e.getMessage());
            return;
        }

        try {
            WriterGUI.getInstance().writeStatus("Авторизация...");
            if (FTP.getInstance().login(loginField.getText(), password))
                WriterGUI.getInstance().writeStatus("Авторизовались!");
            else {
                WriterGUI.getInstance().writeStatus("Авторизация не удалась.");
                return;
            }
        } catch (IOException e) {
            MainApp.makeAlertDialog(e.getMessage());
            return;
        }

        initRemoteRepoBrowser();
    }

    private void initRemoteRepoBrowser() {
        remoteServerLabel.setText("Удаленный сайт " + hostField.getText() + ": ");

        OutputStream outputStream = new ByteArrayOutputStream();
        try {
            FTP.getInstance().openPassiveDataConnection();
            FTP.getInstance().sendCommand("MLSD");
            FTP.getInstance().readData(outputStream);
        } catch (IOException e) {
            MainApp.makeAlertDialog(e.getMessage());
            return;
        }
        TreeItem<String> rootNode = new TreeItem<String>("/", new ImageView(new Image(getClass().getResourceAsStream("/img/folder-open.png"))));
        rootNode.setExpanded(true);
        remoteRepoBrowser.setRoot(rootNode);

        String list = outputStream.toString();
        String[] nodes = list.split("\\n");

        for (String node : nodes) {
            RemoteRepoTreeItem treeItem = ResponseParser.parseMLSD(node);
            treeItem.setPath("/");
            rootNode.getChildren().add(treeItem);
        }

    }

    private void showLocalFilesTable(FileTreeItem treeItem) {

        treeItem.getChildren();

        ObservableList<Node> list = FXCollections.observableArrayList(treeItem.getNode().getChildren());

        localFilesTable.setItems(list);
    }

    private boolean fieldIsEmpty(TextField field) {
        return field.getText().isEmpty() || field.getText() == null;
    }

}
