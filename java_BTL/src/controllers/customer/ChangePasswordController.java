package controllers.customer;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class ChangePasswordController {

	@FXML
	private Label lblChangePassword;

	@FXML
	public void initialize() {
		lblChangePassword.setText("Chức năng thay đổi mật khẩu sẽ ở đây.");
	}
}
