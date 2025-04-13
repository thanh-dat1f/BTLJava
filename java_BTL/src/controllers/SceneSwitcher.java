package controllers;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class SceneSwitcher {
    private static Stage mainStage;

    public static void setMainStage(Stage stage) {
        mainStage = stage;
    }

    public static void switchScene(String fxmlFile) {
        try {
        	FXMLLoader loader = new FXMLLoader(SceneSwitcher.class.getResource("/view/" + fxmlFile));
        	Parent root = loader.load();
        	mainStage.setTitle("BESTPETS");
        	mainStage.getIcons().add(new Image(SceneSwitcher.class.getResourceAsStream("/images/logo.png")));            
        	// Set kích thước tối thiểu và tối đa cho cửa sổ
            double minWidth = 400; // Chiều rộng tối thiểu cho cửa sổ
            double minHeight = 500; // Chiều cao tối thiểu cho cửa sổ

            // Thiết lập kích thước tối thiểu và tối đa
            mainStage.setMinWidth(minWidth);
            mainStage.setMinHeight(minHeight);
        	mainStage.setResizable(true);
            mainStage.setScene(new Scene(root));
            mainStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
