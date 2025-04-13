package controllers;

import exception.BusinessException;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import model.Account;
import service.AccountService;
import utils.Session;

import java.io.IOException;
import java.util.Optional;

public class LoginController {
	@FXML
	private TextField usernameField;
	@FXML
	private PasswordField passwordField;
	@FXML
	private TextField passwordTextField; // TextField hiển thị mật khẩu
	@FXML
	private Label messageLabel;
	@FXML
	private ImageView togglePasswordVisibilityIcon; // Icon con mắt
	

	private final AccountService accountService = new AccountService();
	private int failedLoginAttempts = 0; // Biến đếm số lần đăng nhập thất bại

	
	@FXML
	private ImageView logoImage;

	@FXML
	public void initialize() {
		// Tải logo vào ImageView
		Image image = new Image(getClass().getResourceAsStream("/images/logo.png"));
		logoImage.setImage(image);

		togglePasswordVisibilityIcon.setImage(new Image(getClass().getResourceAsStream("/images/hide.png")));

	}

	@FXML
	public void handleLogin() {
		String username = usernameField.getText().trim();
		String password = passwordField.isVisible() ? passwordField.getText().trim()
				: passwordTextField.getText().trim();

		try {
			if (failedLoginAttempts >= 5) {
				messageLabel.setText("Tài khoản đã bị khóa tạm thời sau 5 lần sai mật khẩu!");
				messageLabel.setStyle("-fx-text-fill: red;");
				return; // Không cho đăng nhập nếu đã khóa tài khoản
			}

			Optional<Account> user = accountService.login(username, password);
			Account account = user.orElseThrow(() -> new BusinessException("Sai tên đăng nhập hoặc mật khẩu"));
			messageLabel.setText("Đăng nhập thành công!");
			messageLabel.setStyle("-fx-text-fill: green;");
			Session.setCurrentUser(account);

			// Đặt lại số lần thất bại khi đăng nhập thành công
			failedLoginAttempts = 0;

			SceneSwitcher.switchScene("dashboard.fxml");

		} catch (BusinessException e) {
			failedLoginAttempts++;
			messageLabel.setText(e.getMessage());
			messageLabel.setStyle("-fx-text-fill: red;");
		}
	}


	@FXML
	public void togglePasswordVisibility() {
		if (passwordField.isVisible()) {
			// Chuyển sang TextField để hiển thị mật khẩu
			passwordField.setVisible(false);
			passwordTextField.setVisible(true);
			passwordTextField.setText(passwordField.getText());
			// Đổi icon mắt
			togglePasswordVisibilityIcon.setImage(new Image(getClass().getResourceAsStream("/images/show.png")));
		} else {
			// Quay lại PasswordField để ẩn mật khẩu
			passwordField.setVisible(true);
			passwordTextField.setVisible(false);
			passwordField.setText(passwordTextField.getText());
			// Đổi icon mắt bị gạch chéo
			togglePasswordVisibilityIcon.setImage(new Image(getClass().getResourceAsStream("/images/hide.png")));
		}
	}

}
