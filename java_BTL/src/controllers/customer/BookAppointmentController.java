package controllers.customer;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import controllers.SceneSwitcher;
import enums.StatusEnum;
import enums.TypeOrder;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import model.Account;
import model.Customer;
import model.HappenStatus;
import model.Order;
import repository.HappenStatusRepository;
import repository.OrderRepository;
import service.CustomerService;
import utils.Session;

public class BookAppointmentController {

	@FXML
	private Label lblWelcome;

	@FXML
	private Label lblCustomerInfo;

	@FXML
	private Label lblEmail;

	@FXML
	private ComboBox<String> cbServices;

	@FXML
	private ComboBox<String> cbTime;

	@FXML
	private DatePicker dpDate;

	@FXML
	private Label lblBookingInfo;

	@FXML
	public void initialize() {
		Account currentUser = Session.getCurrentUser();
		if (currentUser != null) {
			lblWelcome.setText("🐾 Xin chào, " + currentUser.getUserName());
			lblCustomerInfo.setText("Tên khách hàng: " + currentUser.getUserName());
			lblEmail.setText("Email: " + currentUser.getEmail());

			// Tạo danh sách dịch vụ cho ComboBox
			cbServices.setItems(FXCollections.observableArrayList("Dịch vụ 1", "Dịch vụ 2", "Dịch vụ 3"));

			// Tạo danh sách giờ cho ComboBox
			cbTime.setItems(FXCollections.observableArrayList("08:00 AM", "09:00 AM", "10:00 AM"));
		} else {
			lblWelcome.setText("Bạn cần đăng nhập để xem thông tin.");
		}
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

	@FXML
	private void handleBookAppointment() {
		// Kiểm tra các trường
		if (cbServices.getValue() == null || cbTime.getValue() == null || dpDate.getValue() == null) {
			Alert alert = new Alert(AlertType.WARNING);
			alert.setTitle("Thông báo");
			alert.setHeaderText("Chưa chọn đủ thông tin");
			alert.setContentText("Vui lòng chọn dịch vụ, thời gian và ngày để đặt lịch.");
			alert.showAndWait();
			return;
		}

		// Lấy thông tin từ UI
		String service = cbServices.getValue();
		String timeStr = cbTime.getValue(); // Ví dụ "08:00"
		DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");
		LocalTime localTime = LocalTime.parse(timeStr, timeFormatter);
		LocalDate localDate = dpDate.getValue(); // LocalDate từ DatePicker
		LocalDateTime localDateTime = localDate.atTime(localTime);
		Timestamp appointmentTimestamp = Timestamp.valueOf(localDateTime);

		// Lấy Account và chuyển sang Customer
		Account acc = Session.getCurrentUser();
		Customer currentCustomer = null;
		try {
			currentCustomer = CustomerService.getInstance().getCustomerById(acc.getAccountID());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // Đúng kiểu Customer

		// Tạo Order
		Order appointmentOrder = new Order();
		appointmentOrder.setOrderDate(new Timestamp(System.currentTimeMillis()));
		appointmentOrder.setAppointmentDate(appointmentTimestamp);
		appointmentOrder.setOrderType(TypeOrder.APPOINTMENT);
		appointmentOrder.setCustomer(currentCustomer); // Giờ đúng kiểu
		HappenStatus status = HappenStatusRepository.getInstance().findByStatusCode(StatusEnum.PENDING);
		appointmentOrder.setHappenStatus(status);

		int result = OrderRepository.getInstance().insert(appointmentOrder);
		if (result > 0) {
			lblBookingInfo.setText("🐾 Lịch hẹn đã được đặt thành công!");
		} else {
			lblBookingInfo.setText("❌ Đặt lịch thất bại. Vui lòng thử lại.");
		}

	}

}
