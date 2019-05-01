package sample;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Properties;

public class Main extends Application {

    public Stage abtWindow;

    @Override
    public void start(Stage primaryStage) throws Exception{

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/sample/Design.fxml"));

        Parent root = loader.load();


        Controller controller =loader.getController();
        primaryStage.setTitle("にやあ　Dictator　[ база не выбрана ]");
        primaryStage.setScene(new Scene(root, 950, 560));
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/sample/icon.png")));
        primaryStage.setOnCloseRequest(e -> controller.exitApplication() );
        primaryStage.setOnHidden(e -> controller.exitApplication() );
        primaryStage.setOnShown(e-> controller.setTitle("123"));
        primaryStage.show();



    }



    public static void main(String[] args) {

        launch(args);
    }
}
