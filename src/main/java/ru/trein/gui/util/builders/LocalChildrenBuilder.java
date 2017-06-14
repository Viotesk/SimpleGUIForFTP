package ru.trein.gui.util.builders;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import ru.trein.gui.nodes.FileTreeItem;
import ru.trein.gui.nodes.Node;

import java.io.File;
import java.util.HashSet;

/**
 * Created by Shpien on 10.06.2017.
 */
public class LocalChildrenBuilder implements ChildrenBuilder {
    @Override
    public ObservableList<FileTreeItem> buildChildren(FileTreeItem fileTreeItem) {

        File f = new File(fileTreeItem.getNode().getPath());

        if (!f.isDirectory())
            return FXCollections.emptyObservableList();

        File[] files = f.listFiles();

        if (files == null)
            return FXCollections.emptyObservableList();

        ObservableList<FileTreeItem> children = FXCollections.observableArrayList(new HashSet<>());
//        children.clear();
        for (File childFile : files) {
            Node node = new Node(childFile.isDirectory(), childFile.lastModified(), childFile.length(), childFile.getName(), childFile.getAbsolutePath(), this);
            fileTreeItem.getNode().addChildren(node);

            if(!node.isDirectory())
                continue;

            FileTreeItem treeItem = new FileTreeItem(node);
            children.add(treeItem);
        }
        return children;


    }
}
