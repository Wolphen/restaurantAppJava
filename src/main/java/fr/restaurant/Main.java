package fr.restaurant;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxml = new FXMLLoader(
                getClass().getResource("/fr/restaurant/view/RootView.fxml"));
        Scene scene = new Scene(fxml.load(), 1000, 650);
        scene.getStylesheets().add(
                getClass().getResource("/fr/restaurant/css/style.css").toExternalForm());

        stage.setTitle("Restaurant Manager");
        stage.setScene(scene);
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.show();
    }
    public static void main(String[] args) { launch(args); }
}
