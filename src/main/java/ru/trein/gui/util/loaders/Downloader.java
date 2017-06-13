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

public class Downloader extends Abortable implements Runnable {
    private ObservableList<Node> nodesForDownload;
    private FileTreeItem selectedLocalItem;
    private ChildrenBuilder localChildrenBuilder;
    private LoadController loadController;

    private int bufferSize = 1024;
    private int downloaded;

    public Downloader(ObservableList<Node> nodesForDownload, FileTreeItem selectedLocalItem, ChildrenBuilder localChildrenBuilder, LoadController loadController) {
        aborted = false;
        this.nodesForDownload = nodesForDownload;
        this.selectedLocalItem = selectedLocalItem;
        this.localChildrenBuilder = localChildrenBuilder;
        this.loadController = loadController;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    @Override
    public void run() {
        downloadDir(nodesForDownload, selectedLocalItem);
        Platform.runLater(() -> loadController.setButtonText("Готово!"));
    }

    private void downloadDir(ObservableList<Node> nodes, FileTreeItem target) {
        if(aborted)
            return;

        ObservableList<Node> dirs = FXCollections.observableArrayList();

        String path = target.getNode().getPath();

        File currDir = new File(path);

        if (!(currDir.exists() && currDir.isDirectory()))
            if (!currDir.mkdir()) {
                WriterGUI.getInstance().writeStatus("Не удалось создать директорию");
                return;
            }


        for (Node nodesForDownload : nodes) {
            if (nodesForDownload.isDirectory()) {
                dirs.add(nodesForDownload);
                continue;
            }

            if (!FTP.getInstance().openPassiveDataConnection()) {
                WriterGUI.getInstance().writeStatus("Невозможно открыть соединение для передачи данных");
                return;
            }

            if (!FTP.getInstance().startDownload(nodesForDownload.getName())) {
                WriterGUI.getInstance().writeStatus("Нет возможности начать передачу!");
                return;
            }

            try (FileOutputStream out = new FileOutputStream(new File(path + File.separator + nodesForDownload.getName()))) {

                Platform.runLater(() -> loadController.setFileName(nodesForDownload.getName()));

                downloaded = 0;
                byte[] buffer = new byte[bufferSize];
                long total = nodesForDownload.getSize();

                try (InputStream in = FTP.getInstance().readData()) {
                    int read;
                    while ((read = in.read(buffer)) != -1) {
                        out.write(buffer, 0, read);
                        downloaded += read;
                        Platform.runLater(() -> loadController.updateProgress((downloaded * 100d / total) / 100));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }

                if (!FTP.getInstance().transferIsSuccessfull()) {
                    WriterGUI.getInstance().writeStatus("Передача не удалась!");
                    return;
                }

                Node uploadedNode = new Node(false, nodesForDownload.getModify(),
                        nodesForDownload.getSize(), nodesForDownload.getName(),
                        path + nodesForDownload.getName(), localChildrenBuilder);

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
                    path + File.separator + dir.getName(), localChildrenBuilder);

            target.getNode().addChildren(uploadedNode);

            FileTreeItem currItem = new FileTreeItem(dir);
            currItem.getChildren();
            List<Node> childrens = currItem.getNode().getChildren();

            ObservableList<Node> newNodes = new ObservableListWrapper<>(childrens);

            FileTreeItem targetItem = new FileTreeItem(uploadedNode);
            downloadDir(newNodes, targetItem);
        }
    }

}
