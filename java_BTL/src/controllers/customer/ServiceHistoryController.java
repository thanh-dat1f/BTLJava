package controllers.customer;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class ServiceHistoryController {

	@FXML
	private Label lblServiceHistory;

	@FXML
	public void initialize() {
		lblServiceHistory.setText("Danh sách các dịch vụ bạn đã sử dụng.");
	}
}
