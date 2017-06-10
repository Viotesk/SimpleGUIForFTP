package ru.trein.gui.nodes;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.File;

public class FilePathTreeItem extends TreeItem<String> {

    private static Image folderClosedImg = new Image("/img/folder.png");
    private static Image folderOpenImg = new Image("/img/folder-open.png");
    private static Image fileImage = new Image("/img/text-x-generic.png");

    private final File file;
    private boolean isDirectory;

    private boolean isLeaf;
    private boolean isFirstTimeLeaf = true;
    private boolean isFirstTimeChildren = true;

    @SuppressWarnings("unchecked")
    public FilePathTreeItem(File file) {
        super(file.getPath());

        this.file = file;
        this.isDirectory = file.isDirectory();

        if (this.isDirectory) {
            this.setGraphic(new ImageView(folderClosedImg));

            //event handlers for change img if folder opened/closed
            this.addEventHandler(TreeItem.branchCollapsedEvent(), new EventHandler() {
                public void handle(Event event) {
                    FilePathTreeItem source = (FilePathTreeItem) event.getSource();
                    if (!source.isExpanded()) {
                        ImageView iv = (ImageView) source.getGraphic();
                        iv.setImage(folderClosedImg);
                    }
                }
            });

            this.addEventHandler(TreeItem.branchExpandedEvent(), new EventHandler() {
                public void handle(Event e) {
                    FilePathTreeItem source = (FilePathTreeItem) e.getSource();
                    if (source.isExpanded()) {
                        ImageView iv = (ImageView) source.getGraphic();
                        iv.setImage(folderOpenImg);
                    }
                }
            });
        } else {
            this.setGraphic(new ImageView(fileImage));
            return;
        }
    }

    public File getFile() {
        return file;
    }

    @Override
    public boolean isLeaf() {
        if (isFirstTimeLeaf) {
            isFirstTimeLeaf = false;
            isLeaf = this.file.isFile();
        }
        return isLeaf;
    }

    @Override
    public ObservableList<TreeItem<String>> getChildren() {
        if (isFirstTimeChildren) {
            isFirstTimeChildren = false;
            super.getChildren().setAll(buildChildren(this));
        }
        return (super.getChildren());
    }

    private ObservableList<FilePathTreeItem> buildChildren(FilePathTreeItem treeItem) {
        File f = treeItem.getFile();
        if ((f != null) && (f.isDirectory())) {
            File[] files = f.listFiles();
            if (files != null) {
                ObservableList<FilePathTreeItem> children = FXCollections.observableArrayList();
                for (File childFile : files) {
                    children.add(new FilePathTreeItem(childFile));
                }
                return (children);
            }
        }
        return FXCollections.emptyObservableList();
    }
}
