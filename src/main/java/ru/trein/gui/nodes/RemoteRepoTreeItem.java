package ru.trein.gui.nodes;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import ru.trein.gui.MainApp;
import ru.trein.gui.util.FTP;
import ru.trein.gui.util.ResponseParser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;


public class RemoteRepoTreeItem extends TreeItem<String> {

    private static Image folderClosedImg = new Image("/img/folder.png");
    private static Image folderOpenImg = new Image("/img/folder-open.png");
    private static Image fileImage = new Image("/img/text-x-generic.png");

    private String itemType;
    private long modify;
    private int size = -1;
    private String name;
    private String path;

    private boolean isFirstTimeLeaf = true;
    private boolean isFirstTimeChildren = true;

    private boolean isLeaf;

    public RemoteRepoTreeItem(String itemType, long modify, int size, String name) {
        this(itemType, modify, name);
        this.size = size;
    }

    @SuppressWarnings("unchecked")
    public RemoteRepoTreeItem(String itemType, long modify, String name) {
        super(name);

        this.itemType = itemType;
        this.modify = modify;
        this.name = name;

        if (itemType.equals("dir")) {
            this.setGraphic(new ImageView(folderClosedImg));

            this.addEventHandler(TreeItem.branchCollapsedEvent(), new EventHandler() {
                public void handle(Event event) {
                    RemoteRepoTreeItem source = (RemoteRepoTreeItem) event.getSource();
                    ImageView iv = (ImageView) source.getGraphic();
                    iv.setImage(folderClosedImg);
                }
            });

            this.addEventHandler(TreeItem.branchExpandedEvent(), new EventHandler() {
                public void handle(Event event) {
                    RemoteRepoTreeItem source = (RemoteRepoTreeItem) event.getSource();
                    ImageView iv = (ImageView) source.getGraphic();
                    iv.setImage(folderOpenImg);

                }
            });
        } else {
            this.setGraphic(new ImageView(fileImage));
        }
    }

    @Override
    public boolean isLeaf() {
        if (isFirstTimeLeaf) {
            isFirstTimeLeaf = false;
            isLeaf = this.getItemType().equals("file");
        }
        return isLeaf;
    }

    @Override
    public ObservableList<TreeItem<String>> getChildren() {
        if (isFirstTimeChildren) {
            isFirstTimeChildren = false;
            super.getChildren().setAll(buildChildren(this));
        }
        return super.getChildren();
    }

    private ObservableList<RemoteRepoTreeItem> buildChildren(RemoteRepoTreeItem treeItem) {
//        File f = treeItem.getFile();
//        if ((f != null) && (f.isDirectory())) {
//            File[] files = f.listFiles();
//            if (files != null) {
//                ObservableList<FilePathTreeItem> children = FXCollections.observableArrayList();
//                for (File childFile : files) {
//                    children.add(new FilePathTreeItem(childFile));
//                }
//                return (children);
//            }
//        }
//        return FXCollections.emptyObservableList();
        if (treeItem.getItemType().equals("dir")) {
            OutputStream outputStream = new ByteArrayOutputStream();
            String path = treeItem.getPath() + treeItem.getName();
            try {
                FTP.getInstance().setDirectory(path);

                FTP.getInstance().openPassiveDataConnection();

                FTP.getInstance().sendCommand("MLSD");

                FTP.getInstance().readData(outputStream);

            } catch (IOException e) {
                MainApp.makeAlertDialog(e.getMessage());
            }

            String list = outputStream.toString();

            if (list.isEmpty() || list.equals(""))
                return FXCollections.emptyObservableList();

            String[] nodes = list.split("\\n");

            ObservableList<RemoteRepoTreeItem> children = FXCollections.observableArrayList();
            for (String node : nodes) {
                RemoteRepoTreeItem treeItemToAdd = ResponseParser.parseMLSD(node);
                treeItemToAdd.setPath(path + "/");
                children.add(treeItemToAdd);
            }
            return children;

        }

        return FXCollections.emptyObservableList();

    }

    public String getItemType() {
        return itemType;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return "RemoteRepoTreeItem{" +
                "itemType='" + itemType + '\'' +
                ", modify=" + modify +
                ", size=" + size +
                ", name='" + name + '\'' +
                ", path='" + path + '\'' +
                ", isFirstTimeLeaf=" + isFirstTimeLeaf +
                ", isFirstTimeChildren=" + isFirstTimeChildren +
                ", isLeaf=" + isLeaf +
                '}';
    }
}
