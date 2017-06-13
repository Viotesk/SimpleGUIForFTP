package ru.trein.gui.util.loaders;

import com.sun.javafx.collections.ObservableListWrapper;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import ru.trein.gui.Abortable;
import ru.trein.gui.controllers.LoadController;
import ru.trein.gui.nodes.FileTreeItem;
import ru.trein.gui.nodes.Node;
import ru.trein.gui.util.FTP;
import ru.trein.gui.util.WriterGUI;
import ru.trein.gui.util.builders.ChildrenBuilder;

import java.io.*;
import java.util.List;

public class Uploader extends Abortable implements Runnable {
    private ObservableList<Node> nodesForUpload;
    private FileTreeItem selectedRemoteItem;
    private ChildrenBuilder remoteChildrenBuilder;
    private LoadController loadController;


    private int bufferSize = 1024;
    private int uploaded;

    public Uploader(ObservableList<Node> nodesForUpload, FileTreeItem selectedRemoteItem, ChildrenBuilder remoteChildrenBuilder, LoadController loadController) {
        aborted = false;
        this.nodesForUpload = nodesForUpload;
        this.selectedRemoteItem = selectedRemoteItem;
        this.remoteChildrenBuilder = remoteChildrenBuilder;
        this.loadController = loadController;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }


    @Override
    public void run() {
        uploadDir(nodesForUpload, selectedRemoteItem);
        Platform.runLater(() -> loadController.setButtonText("Готово!"));
    }

    private void uploadDir(ObservableList<Node> nodes, FileTreeItem target) {
        if(aborted)
            return;

        ObservableList<Node> dirs = FXCollections.observableArrayList();

        if (!FTP.getInstance().setDirectory(target.getNode().getPath())) {
            if (FTP.getInstance().createDir(target.getNode().getPath())) {
                if (!FTP.getInstance().setDirectory(target.getNode().getPath())) {
                    throw new RuntimeException("something broke");
                }
            }
        }

        for (Node nodeForUpload : nodes) {
            if (nodeForUpload.isDirectory()) {
                dirs.add(nodeForUpload);
                continue;
            }

            if (!FTP.getInstance().openPassiveDataConnection()) {
                WriterGUI.getInstance().writeStatus("Невозможно открыть соединение для передачи данных");
                return;
            }

            if (!FTP.getInstance().startUpload(nodeForUpload.getName())) {
                WriterGUI.getInstance().writeStatus("Нет возможности начать передачу!");
                return;
            }

            try (FileInputStream in = new FileInputStream(new File(nodeForUpload.getPath()))) {

                Platform.runLater(() -> loadController.setFileName(nodeForUpload.getName()));

                uploaded = 0;
                byte[] buffer = new byte[bufferSize];
                long total = in.available();

                try (OutputStream out = FTP.getInstance().writeData()) {
                    int read;
                    while ((read = in.read(buffer)) != -1) {
                        out.write(buffer, 0, read);
                        uploaded += read;
                        Platform.runLater(() -> loadController.updateProgress((uploaded * 100d / total) / 100));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }

                if (!FTP.getInstance().transferIsSuccessfull()) {
                    WriterGUI.getInstance().writeStatus("Передача не удалась!");
                    return;
                }

                Node uploadedNode = new Node(false, nodeForUpload.getModify(),
                        nodeForUpload.getSize(), nodeForUpload.getName(),
                        target.getNode().getPath() + nodeForUpload.getName(), remoteChildrenBuilder);

                target.getNode().addChildren(uploadedNode);
            } catch (FileNotFoundException e) {
                System.err.println(e.getMessage());
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }


        for (Node dir : dirs) {
            Node uploadedNode = new Node(true, dir.getModify(), 0, dir.getName(),
                    target.getNode().getPath() + dir.getName() + "/", remoteChildrenBuilder);

            target.getNode().addChildren(uploadedNode);

            FileTreeItem currItem = new FileTreeItem(dir);
            currItem.getChildren();
            List<Node> childrens = currItem.getNode().getChildren();

            ObservableList<Node> newNodes = new ObservableListWrapper<>(childrens);

            FileTreeItem targetItem = new FileTreeItem(uploadedNode);
            uploadDir(newNodes, targetItem);
        }
    }

}
