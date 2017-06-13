package ru.trein.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class MainApp extends Application {
    private static Stage primartStage;

    public void start(Stage primaryStage) throws Exception {
        primartStage = primaryStage;

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/view/RootWindow.fxml"));

        Parent root = loader.load();
        Scene scene = new Scene(root);

        primaryStage.getIcons().add(new Image("/img/mainIcon.png"));
        primaryStage.setTitle("Simple FTP Client");
        primaryStage.setScene(scene);
        primaryStage.show();

    }

    public static Stage getPrimartStage() {
        return primartStage;
    }

    public static void makeAlertDialog(String text) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Ошибка!");
        alert.setContentText(text);

        alert.showAndWait();
    }
}
