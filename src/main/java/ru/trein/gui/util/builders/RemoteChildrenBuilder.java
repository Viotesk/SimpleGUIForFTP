package ru.trein.gui.util.builders;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import ru.trein.gui.nodes.FileTreeItem;
import ru.trein.gui.nodes.Node;
import ru.trein.gui.util.FTP;
import ru.trein.gui.util.ResponseParser;
import ru.trein.gui.util.WriterGUI;

import java.util.HashSet;

public class RemoteChildrenBuilder implements ChildrenBuilder {
    @Override
    public ObservableList<FileTreeItem> buildChildren(FileTreeItem fileTreeItem) {
        Node parentNode = fileTreeItem.getNode();

        String parentPath = parentNode.getPath();

        WriterGUI.getInstance().writeStatus("Получение списка каталогов \"" + parentPath + "\"...");

        if (!parentPath.equals(FTP.getInstance().currentDirectory()))
            FTP.getInstance().setDirectory(parentPath);

        String nodesString = FTP.getInstance().commandMLSD();

        if (nodesString == null || nodesString.isEmpty() || nodesString.equals(""))
            return FXCollections.emptyObservableList();

        String[] nodes = nodesString.split("\\n");

        ObservableList<FileTreeItem> children = FXCollections.observableArrayList(new HashSet<>());
//        children.clear();

        for (String nodeStr : nodes) {
            if (nodeStr.isEmpty() || nodeStr.equals(""))
                continue;

            Node node = ResponseParser.parseMLSD(nodeStr, parentPath);
            node.setChildrenBuilder(this);
            parentNode.addChildren(node);

            if (!node.isDirectory())
                continue;

            FileTreeItem treeItem = new FileTreeItem(node);
            children.add(treeItem);
        }
        WriterGUI.getInstance().writeStatus("Список каталогов \"" + parentPath + "\" извлечен.");
        return children;
    }
}
