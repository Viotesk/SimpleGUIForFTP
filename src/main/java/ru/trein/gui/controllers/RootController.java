package ru.trein.gui.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import ru.trein.gui.nodes.FilePathTreeItem;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;

public class RootController {

    @FXML
    private TreeView<String> localRepositoryBrowser;

    public void initialize() {
        TreeItem<String> rootNode = new TreeItem<String>("MyComputer", new ImageView(new Image(getClass().getResourceAsStream("/img/computer.png"))));

        Iterable<Path> rootDirectories= FileSystems.getDefault().getRootDirectories();

        for (Path path : rootDirectories) {
            FilePathTreeItem treeNode = new FilePathTreeItem(new File(path.toString()));
            rootNode.getChildren().add(treeNode);
        }

        rootNode.setExpanded(true);
        localRepositoryBrowser.setRoot(rootNode);
        localRepositoryBrowser.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }
}
