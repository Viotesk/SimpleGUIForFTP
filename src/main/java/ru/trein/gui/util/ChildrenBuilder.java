package ru.trein.gui.util;

import javafx.collections.ObservableList;
import ru.trein.gui.nodes.FileTreeItem;

/**
 * Created by Shpien on 10.06.2017.
 */
public interface ChildrenBuilder {
    public ObservableList<FileTreeItem> buildChildren (FileTreeItem fileTreeItem);
}
