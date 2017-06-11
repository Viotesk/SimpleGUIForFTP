package ru.trein.gui.nodes;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import ru.trein.gui.util.ChildrenBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Shpien on 10.06.2017.
 */
public class Node {

    private ImageView img;
    private boolean isDirectory;
    private long modify;
    private long size;
    private String name;
    private String path;
    private List<Node> children = new ArrayList<>();
    private ChildrenBuilder childrenBuilder;

    public Node(boolean isDirectory, long modify, long size, String name, String path, ChildrenBuilder childrenBuilder) {
        this.isDirectory = isDirectory;
        this.modify = modify;
        this.size = size;
        this.name = name;
        this.path = path;
        this.childrenBuilder = childrenBuilder;

        if(isDirectory)
            img = new ImageView(new Image("/img/folder.png"));
        else
            img = new ImageView(new Image("/img/text-x-generic.png"));
    }

    public void setSize(long size) {
        this.size = size;
    }

    public void addChildren(Node node) {
        children.add(node);
    }

    public List<Node> getChildren() {
        return children;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public long getModify() {
        return modify;
    }

    public long getSize() {
        return size;
    }

    public ImageView getImg() {
        return img;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public ChildrenBuilder getChildrenBuilder() {
        return childrenBuilder;
    }
}
