package controllers.customer;

import controllers.SceneSwitcher;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import model.Account;
import utils.Session;

public class ViewProfileController {

	@FXML
	private Label lblWelcome;
	@FXML
	private Label lblName;
	@FXML
	private Label lblEmail;
	@FXML
	private Label lblPassword;
	@FXML
	private ImageView imgAvatar;

	private final String avatarDefault = "/images/avatar_placeholder.png";

	@FXML
	public void initialize() {
		Account currentUser = Session.getCurrentUser();
		if (currentUser != null) {
			lblWelcome.setText("🐾 Xin chào, " + currentUser.getUserName());
			lblName.setText(currentUser.getUserName());
			lblEmail.setText(currentUser.getEmail());
			lblPassword.setText(maskPassword(currentUser.getPassword()));

			// Set ảnh đại diện mặc định
			imgAvatar.setImage(new Image(avatarDefault));
		} else {
			lblWelcome.setText("Bạn cần đăng nhập để xem thông tin.");
		}
	}

	private String maskPassword(String password) {
		return "*".repeat(12);
	}

	@FXML
	private void handleBack() {
		SceneSwitcher.switchScene("customer/customerDashboard.fxml");
	}

	@FXML
	private void handleLogout() {
		Session.logout();
		SceneSwitcher.switchScene("login.fxml");
	}
}
