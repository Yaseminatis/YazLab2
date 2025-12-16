package edu.kou.yazLab2;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/edu/kou/yazLab2/main.fxml")
        );

        Parent root = loader.load();

        // UI genişliğine uygun sahne
        Scene scene = new Scene(root, 1400, 700);

        stage.setTitle("YazLab2 - Graph Editor");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}