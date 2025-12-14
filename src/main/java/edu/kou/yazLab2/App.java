package edu.kou.yazLab2;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        Parent root = FXMLLoader.load(
                getClass().getResource("/edu/kou/yazLab2/main.fxml")
        );

        Scene scene = new Scene(root, 600, 400);
        stage.setScene(scene);
        stage.setTitle("YazLab2");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}