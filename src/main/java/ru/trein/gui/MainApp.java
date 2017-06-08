package ru.trein.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Created by Shpien on 08.06.2017.
 */
public class MainApp extends Application {

    private Stage primaryStage;

    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/view/RootWindow.fxml"));

        Parent root = loader.load();
        Scene scene = new Scene(root);

        primaryStage.setTitle("Simple FTP Client");
        primaryStage.setScene(scene);
        primaryStage.show();

    }
}
