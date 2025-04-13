package controllers.customer;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class AppointmentHistoryController {

	@FXML
	private Label lblAppointmentHistory;

	@FXML
	public void initialize() {
		lblAppointmentHistory.setText("Danh sách các cuộc hẹn của bạn.");
	}
}
