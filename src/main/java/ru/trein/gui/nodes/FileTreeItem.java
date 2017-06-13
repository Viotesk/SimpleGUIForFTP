package ru.trein.gui.nodes;

import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class FileTreeItem extends TreeItem<String> {

    private static Image folderClosedImg = new Image("/img/folder.png");
    private static Image folderOpenImg = new Image("/img/folder-open.png");

    Node node;

    private boolean isFirstTimeChildren = true;

    @SuppressWarnings("unchecked")
    public FileTreeItem(Node node) {
        super(node.getName());

        this.node = node;

        if(!node.isDirectory())
            throw new RuntimeException("Node can't be file");

        this.setGraphic(new ImageView(folderClosedImg));

        //event handlers for change img if folder opened/closed
        this.addEventHandler(TreeItem.branchCollapsedEvent(), new EventHandler() {
            public void handle(Event event) {
                FileTreeItem source = (FileTreeItem) event.getSource();
                if (!source.isExpanded()) {
                    ImageView iv = (ImageView) source.getGraphic();
                    iv.setImage(folderClosedImg);
                }
            }
        });

        this.addEventHandler(TreeItem.branchExpandedEvent(), new EventHandler() {
            public void handle(Event e) {
                FileTreeItem source = (FileTreeItem) e.getSource();
                if (source.isExpanded()) {
                    ImageView iv = (ImageView) source.getGraphic();
                    iv.setImage(folderOpenImg);
                }
            }
        });
    }

    @Override
    public boolean isLeaf() {
        return false;
    }

    @Override
    public ObservableList<TreeItem<String>> getChildren() {
        if (isFirstTimeChildren) {
            isFirstTimeChildren = false;
            super.getChildren().setAll(buildChildren(this));
        }
        return (super.getChildren());
    }

    private ObservableList<FileTreeItem> buildChildren(FileTreeItem treeItem) {
        return node.getChildrenBuilder().buildChildren(treeItem);
    }

    public void reloadChildrens() {
        super.getChildren().setAll(buildChildren(this));
    }

    public Node getNode() {
        return node;
    }
}
