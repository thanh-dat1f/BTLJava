package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.application.Platform;
import javafx.event.ActionEvent;

public class HomeController {

    @FXML private Label title;
    @FXML private Button btnLogin, btnLanguage;
    private boolean isEnglish = false;

    @FXML
    public void initialize() {
        updateLanguage();
        btnLogin.setOnAction(this::handleLogin);
        btnLanguage.setOnAction(this::toggleLanguage);
    }

    private void handleLogin(ActionEvent event) {
        SceneSwitcher.switchScene("login.fxml");
    }


    private void toggleLanguage(ActionEvent event) {
        isEnglish = !isEnglish;
        updateLanguage();
    }

    private void updateLanguage() {
        if (isEnglish) {
            title.setText("PET CARE");
            btnLogin.setText("Login");
            btnLanguage.setText("EN|VN");
        } else {
        	title.setText("CHĂM SÓC THÚ CƯNG CỦA BẠN");
            btnLogin.setText("Đăng nhập");
            btnLanguage.setText("VN|GB");
        }
    }
}
