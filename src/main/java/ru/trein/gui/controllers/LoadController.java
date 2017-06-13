package ru.trein.gui.controllers;


import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import ru.trein.gui.Abortable;
import ru.trein.gui.MainApp;
import ru.trein.gui.nodes.FileTreeItem;
import ru.trein.gui.nodes.Node;

import ru.trein.gui.util.FTP;
import ru.trein.gui.util.builders.ChildrenBuilder;
import ru.trein.gui.util.loaders.Downloader;
import ru.trein.gui.util.loaders.Uploader;

import java.io.*;

public class LoadController {

    @FXML
    private Button controlButton;
    @FXML
    private Label fileName;
    @FXML
    private ProgressBar progress;
    @FXML
    private Label processName;

    private ObservableList<Node> nodes;
    private FileTreeItem selectedItem;
    private ChildrenBuilder childrenBuilder;
    private Thread th;
    private Stage stg;

    private Abortable currentLoader;

    private void initLoadWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(LoadController.class.getResource("/view/LoadWindow.fxml"));
            loader.setController(this);
            stg = new Stage(StageStyle.UNDECORATED);
            Parent loadWindow = loader.load();
            Scene scene = new Scene(loadWindow);
            stg.setScene(scene);

            stg.initModality(Modality.WINDOW_MODAL);
            stg.initOwner(MainApp.getPrimartStage());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public LoadController() {
    }


    public LoadController(ObservableList<Node> nodes, FileTreeItem selectedItem, ChildrenBuilder childrenBuilder) {
        this.nodes = nodes;
        this.selectedItem = selectedItem;
        this.childrenBuilder = childrenBuilder;
        initLoadWindow();
    }

    public void upload() {
        stg.show();
        processName.setText("Процесс загрузки на сервер...");
        Uploader uploader = new Uploader(nodes, selectedItem, childrenBuilder, this);
        currentLoader = uploader;
        th = new Thread(uploader);
        th.setName("Uploader");
        th.start();
    }

    public void download() {
        stg.show();
        processName.setText("Процесс скачивания с сервера...");
        Downloader downloader = new Downloader(nodes, selectedItem, childrenBuilder, this);
        currentLoader = downloader;
        th = new Thread(downloader);
        th.setName("Downloader");
        th.start();
    }

    public void setFileName(String name) {
        fileName.setText(name);
    }

    public void updateProgress(double value) {
        progress.setProgress(value);
    }

    public void setButtonText(String text) {
        controlButton.setText(text);
    }

    @FXML
    private void handleControlButton(MouseEvent event) {
        if (event.getButton() != MouseButton.PRIMARY)
            return;

        if(th.isAlive()) {
            FTP.getInstance().abort();
            currentLoader.setAborted(true);
        }

        stg.close();

    }
}
