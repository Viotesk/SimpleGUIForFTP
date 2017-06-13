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
import javafx.scene.layout.VBox;
import ru.trein.gui.MainApp;
import ru.trein.gui.nodes.FileTreeItem;
import ru.trein.gui.nodes.Node;
import ru.trein.gui.util.*;
import ru.trein.gui.util.builders.LocalChildrenBuilder;
import ru.trein.gui.util.builders.RemoteChildrenBuilder;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.regex.Pattern;

public class RootController {

    @FXML
    private VBox remoteBox;
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
    private TableView<Node> remoteFilesTable;
    @FXML
    private TableColumn<Node, ImageView> remoteImg;
    @FXML
    private TableColumn<Node, String> remoteFileName;
    @FXML
    private TableColumn<Node, Long> remoteFileSize;
    @FXML
    private TableColumn<Node, Long> remoteFileModify;


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
    private RemoteChildrenBuilder remoteChildrenBuilder = new RemoteChildrenBuilder();

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

                TreeItem ti = localRepositoryBrowser.getSelectionModel().getSelectedItem();

                if (!(ti instanceof FileTreeItem))
                    return;

                FileTreeItem selectedItem = (FileTreeItem) ti;


                showFilesTable(selectedItem, localFilesTable);
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

        WriterGUI.getInstance().writeStatus("Попытка подключения к " + hostField.getText());

        if (FTP.getInstance().connect(hostField.getText(), port))
            WriterGUI.getInstance().writeStatus("Подключение выполнено успешно!");
        else {
            WriterGUI.getInstance().writeStatus("Не удалось подключится, проверьте хост и порт.");
            return;
        }


        WriterGUI.getInstance().writeStatus("Авторизация...");
        if (FTP.getInstance().login(loginField.getText(), password))
            WriterGUI.getInstance().writeStatus("Авторизовались!");
        else {
            WriterGUI.getInstance().writeStatus("Авторизация не удалась.");
            return;
        }


        initRemoteRepoBrowser();
        initRemoteFilesTable();
        remoteBox.setDisable(false);
    }

    private void initRemoteRepoBrowser() {
        remoteServerLabel.setText("Удаленный сайт " + hostField.getText() + ": ");
        Node rootNode = new Node(true, 0, 0, "/", "/", remoteChildrenBuilder);
        FileTreeItem rootItem = new FileTreeItem(rootNode);
        remoteRepoBrowser.setRoot(rootItem);
    }

    private void initRemoteFilesTable() {
        remoteFilesTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        remoteImg.setCellValueFactory(new PropertyValueFactory<>("img"));
        remoteFileName.setCellValueFactory(new PropertyValueFactory<>("name"));
        remoteFileSize.setCellValueFactory(new PropertyValueFactory<>("size"));
        remoteFileModify.setCellValueFactory(new PropertyValueFactory<>("modify"));

        remoteRepoBrowser.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.getButton() != MouseButton.PRIMARY)
                    return;

                FileTreeItem selectedItem = (FileTreeItem) remoteRepoBrowser.getSelectionModel().getSelectedItem();

                if (selectedItem == null)
                    return;

                showFilesTable(selectedItem, remoteFilesTable);

            }
        });
    }

    private boolean fieldIsEmpty(TextField field) {
        return field.getText().isEmpty() || field.getText() == null;
    }

    @FXML
    private void handleUpload(MouseEvent event) {
        if (event.getButton() != MouseButton.PRIMARY)
            return;

        FileTreeItem selectedRemoteItem = (FileTreeItem) remoteRepoBrowser.getSelectionModel().getSelectedItem();

        if (selectedRemoteItem == null) {
            WriterGUI.getInstance().writeStatus("Выберите папку на сервере!");
            return;
        }

        ObservableList<Node> selectedItems = localFilesTable.getSelectionModel().getSelectedItems();

        if(selectedItems.size() == 0) {
            WriterGUI.getInstance().writeStatus("Выберите файлы для передачи!");
            return;
        }

        LoadController loadController = new LoadController(selectedItems, selectedRemoteItem, remoteChildrenBuilder);
        loadController.upload();
    }

    @FXML
    public void handleDownload(MouseEvent event) {
        if (event.getButton() != MouseButton.PRIMARY)
            return;

        FileTreeItem selectedLocatItem = (FileTreeItem) localRepositoryBrowser.getSelectionModel().getSelectedItem();

        if (selectedLocatItem == null) {
            WriterGUI.getInstance().writeStatus("Выберите папку на ПК!");
            return;
        }

        ObservableList<Node> selectedItems = remoteFilesTable.getSelectionModel().getSelectedItems();

        if(selectedItems.size() == 0) {
            WriterGUI.getInstance().writeStatus("Выберите файлы для загрузки!");
            return;
        }

        LoadController loadController = new LoadController(selectedItems, selectedLocatItem, localChildrenBuilder);
        loadController.download();
    }

    private void showFilesTable(FileTreeItem treeItem, TableView<Node> table) {
        treeItem.getChildren();

        ObservableList<Node> list = FXCollections.observableArrayList(treeItem.getNode().getChildren());

        table.setItems(list);
    }

}
