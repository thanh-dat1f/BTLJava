package controllers.Staff;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Schedule;
import service.ScheduleService;
import utils.Session;
import model.Staff;
import java.util.ArrayList;

public class MyScheduleController implements Initializable {

    @FXML
    private TableView<Schedule> scheduleTable;
    
    @FXML
    private TableColumn<Schedule, Integer> idColumn;
    
    @FXML
    private TableColumn<Schedule, LocalDate> dateColumn;
    
    @FXML
    private TableColumn<Schedule, String> shiftColumn;
    
    @FXML
    private TableColumn<Schedule, String> noteColumn;
    
    @FXML
    private Label dateLabel;
    
    @FXML
    private DatePicker datePicker;
    
    @FXML
    private Button todayButton;
    
    @FXML
    private Button weekViewButton;
    
    @FXML
    private Button printButton;
    
    private ScheduleService scheduleService;
    private ObservableList<Schedule> scheduleList;
    private int currentStaffId;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize service
        scheduleService = new ScheduleService();
        
        // Get current staff ID from Session
        Staff currentStaff = Session.getInstance().getCurrentStaff();
        if (currentStaff != null) {
            currentStaffId = currentStaff.getId(); // Changed from getStaffId() to getId()
        } else {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không tìm thấy thông tin nhân viên", 
                    "Vui lòng đăng nhập lại.");
            return;
        }        
        idColumn.setCellValueFactory(new PropertyValueFactory<>("scheduleId"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("workDate"));
        shiftColumn.setCellValueFactory(new PropertyValueFactory<>("shift"));
        noteColumn.setCellValueFactory(new PropertyValueFactory<>("note"));
        
        // Set default value for DatePicker
        datePicker.setValue(LocalDate.now());
        
        // Load today's schedule
        loadTodaySchedule();
        
        // Setup event listener for DatePicker
        datePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            loadScheduleByDate(newValue);
        });
    }
    /**
     * Tải lịch làm việc của ngày hiện tại
     */
    @FXML
    private void loadTodaySchedule() {
        LocalDate today = LocalDate.now();
        datePicker.setValue(today);
        loadScheduleByDate(today);
        dateLabel.setText("Lịch làm việc ngày: " + today.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
    }
    
    /**
     * Tải lịch làm việc theo ngày được chọn
     * @param date Ngày cần xem lịch
     */
    private void loadScheduleByDate(LocalDate date) {
        try {
            List<Schedule> schedules = scheduleService.getSchedulesByStaffAndDate(currentStaffId, date);
            scheduleList = FXCollections.observableArrayList(schedules);
            scheduleTable.setItems(scheduleList);
            dateLabel.setText("Lịch làm việc ngày: " + date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải lịch làm việc", e.getMessage());
        }
    }
    
    /**
     * Tải lịch làm việc của tuần hiện tại
     */
    @FXML
    private void loadWeekSchedule() {
        try {
            LocalDate today = LocalDate.now();
            LocalDate startOfWeek = today.minusDays(today.getDayOfWeek().getValue() - 1);
            LocalDate endOfWeek = startOfWeek.plusDays(6);
            
            List<Schedule> schedules = scheduleService.getSchedulesByStaffAndDateRange(currentStaffId, startOfWeek, endOfWeek);
            scheduleList = FXCollections.observableArrayList(schedules);
            scheduleTable.setItems(scheduleList);
            
            dateLabel.setText("Lịch làm việc từ: " + 
                startOfWeek.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + 
                " đến " + 
                endOfWeek.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải lịch làm việc", e.getMessage());
        }
    }
    
    /**
     * In lịch làm việc
     */
    @FXML
    private void printSchedule(ActionEvent event) {
        // Xử lý in lịch làm việc
        showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Tính năng đang phát triển", 
                "Chức năng in lịch làm việc đang được phát triển.");
    }
    
    /**
     * Hiển thị thông báo
     */
    private void showAlert(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}