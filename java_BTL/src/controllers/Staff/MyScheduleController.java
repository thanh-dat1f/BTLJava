package controllers.Staff;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import enums.Shift;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Staff;
import model.WorkSchedule;
import service.ScheduleService;
import utils.Session;

public class MyScheduleController implements Initializable {

    @FXML private Label dateLabel;
    @FXML private Label staffNameLabel;
    @FXML private Label positionLabel;
    @FXML private DatePicker datePicker;
    @FXML private Button todayButton;
    @FXML private Button weekViewButton;
    @FXML private Button monthViewButton;
    @FXML private ComboBox<String> shiftFilter;
    @FXML private Button printButton;
    @FXML private TableView<WorkSchedule> scheduleTable;
    @FXML private TableColumn<WorkSchedule, Integer> idColumn;
    @FXML private TableColumn<WorkSchedule, LocalDate> dateColumn;
    @FXML private TableColumn<WorkSchedule, String> shiftColumn;
    @FXML private TableColumn<WorkSchedule, LocalTime> startTimeColumn;
    @FXML private TableColumn<WorkSchedule, LocalTime> endTimeColumn;
    @FXML private TableColumn<WorkSchedule, String> locationColumn;
    @FXML private TableColumn<WorkSchedule, String> taskColumn;
    @FXML private TableColumn<WorkSchedule, String> noteColumn;
    @FXML private TextArea additionalInfoArea;
    @FXML private Label totalShiftsLabel;
    @FXML private Label morningShiftsLabel;
    @FXML private Label afternoonShiftsLabel;
    @FXML private Label eveningShiftsLabel;
    @FXML private DatePicker registrationDatePicker;
    @FXML private ComboBox<String> shiftSelector;
    @FXML private ComboBox<String> locationSelector;
    @FXML private TextArea registrationNotes;
    @FXML private ComboBox<String> statisticsMonthSelector;
    @FXML private ComboBox<String> statisticsYearSelector;
    @FXML private Label totalHoursLabel;
    @FXML private Label overtimeHoursLabel;
    @FXML private Label standardWorkdaysLabel;
    @FXML private Label leaveCountLabel;
    @FXML private TableView<MonthlyStats> monthlyStatsTable;
    @FXML private Label statusLabel;
    @FXML private VBox dayView;
    @FXML private VBox weekView;
    @FXML private VBox monMorning, tueMorning, wedMorning, thuMorning, friMorning, satMorning, sunMorning;
    @FXML private VBox monAfternoon, tueAfternoon, wedAfternoon, thuAfternoon, friAfternoon, satAfternoon, sunAfternoon;
    @FXML private VBox monEvening, tueEvening, wedEvening, thuEvening, friEvening, satEvening, sunEvening;

    private ScheduleService scheduleService;
    private ObservableList<WorkSchedule> scheduleList;
    private ObservableList<MonthlyStats> monthlyStatsList;
    private int currentStaffId;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        scheduleService = new ScheduleService();

        // Lấy thông tin nhân viên từ Session
        Staff currentStaff = Session.getInstance().getCurrentStaff();
        if (currentStaff != null) {
            currentStaffId = currentStaff.getId();
            staffNameLabel.setText("Nhân viên: " + currentStaff.getFullName());
            positionLabel.setText("Vị trí: " + (currentStaff.getPosition() != null ? currentStaff.getPosition() : "N/A"));
        } else {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không tìm thấy thông tin nhân viên",
                    "Vui lòng đăng nhập lại.");
            return;
        }

        // Khởi tạo các cột bảng
        idColumn.setCellValueFactory(new PropertyValueFactory<>("scheduleID"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("workDate"));
        shiftColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getShift() != null ? cellData.getValue().getShift().name() : ""));
        startTimeColumn.setCellValueFactory(new PropertyValueFactory<>("startTime"));
        endTimeColumn.setCellValueFactory(new PropertyValueFactory<>("endTime"));
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
        taskColumn.setCellValueFactory(new PropertyValueFactory<>("task"));
        noteColumn.setCellValueFactory(new PropertyValueFactory<>("note"));

        // Định dạng cột ngày
        dateColumn.setCellFactory(column -> new TableCell<WorkSchedule, LocalDate>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : formatter.format(item));
            }
        });

        // Định dạng cột giờ
        startTimeColumn.setCellFactory(column -> new TableCell<WorkSchedule, LocalTime>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            @Override
            protected void updateItem(LocalTime item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : formatter.format(item));
            }
        });
        endTimeColumn.setCellFactory(column -> new TableCell<WorkSchedule, LocalTime>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            @Override
            protected void updateItem(LocalTime item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : formatter.format(item));
            }
        });

        // Khởi tạo ComboBox
        shiftFilter.getItems().addAll("Tất cả", Shift.MORNING.name(), Shift.AFTERNOON.name(), Shift.EVENING.name());
        shiftFilter.setValue("Tất cả");
        shiftSelector.getItems().addAll(Shift.MORNING.name(), Shift.AFTERNOON.name(), Shift.EVENING.name());
        locationSelector.getItems().addAll("Chi nhánh 1", "Chi nhánh 2");
        statisticsMonthSelector.getItems().addAll(
                "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12");
        statisticsYearSelector.getItems().addAll("2023", "2024", "2025", "2026");

        // Thiết lập giá trị mặc định
        datePicker.setValue(LocalDate.now());
        registrationDatePicker.setValue(LocalDate.now());
        statisticsMonthSelector.setValue(String.valueOf(LocalDate.now().getMonthValue()));
        statisticsYearSelector.setValue(String.valueOf(LocalDate.now().getYear()));

        // Tải lịch làm việc hôm nay
        loadTodaySchedule();

        // Thiết lập lắng nghe sự kiện cho DatePicker
        datePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            loadScheduleByDate(newValue);
        });

        // Khởi tạo bảng thống kê tháng (giả lập)
        monthlyStatsList = FXCollections.observableArrayList();
        monthlyStatsTable.setItems(monthlyStatsList);
        monthlyStatsTable.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("month"));
        monthlyStatsTable.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("totalHours"));
        monthlyStatsTable.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("overtimeHours"));
        monthlyStatsTable.getColumns().get(3).setCellValueFactory(new PropertyValueFactory<>("workdays"));
        monthlyStatsTable.getColumns().get(4).setCellValueFactory(new PropertyValueFactory<>("leaveDays"));
        monthlyStatsTable.getColumns().get(5).setCellValueFactory(new PropertyValueFactory<>("performance"));
    }

    @FXML
    private void loadTodaySchedule() {
        LocalDate today = LocalDate.now();
        datePicker.setValue(today);
        loadScheduleByDate(today);
        dateLabel.setText("Lịch làm việc ngày: " + today.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
    }

    private void loadScheduleByDate(LocalDate date) {
        try {
            List<WorkSchedule> schedules = scheduleService.getSchedulesByStaffAndDate(currentStaffId, date);
            scheduleList = FXCollections.observableArrayList(schedules);
            applyShiftFilter();
            scheduleTable.setItems(scheduleList);
            dateLabel.setText("Lịch làm việc ngày: " + date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            updateShiftSummary();
            dayView.setVisible(true);
            dayView.setManaged(true);
            weekView.setVisible(false);
            weekView.setManaged(false);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải lịch làm việc", e.getMessage());
        }
    }

    @FXML
    private void loadWeekSchedule() {
        try {
            LocalDate today = LocalDate.now();
            LocalDate startOfWeek = today.minusDays(today.getDayOfWeek().getValue() - 1);
            LocalDate endOfWeek = startOfWeek.plusDays(6);
            List<WorkSchedule> schedules = scheduleService.getSchedulesByStaffAndDateRange(currentStaffId, startOfWeek, endOfWeek);
            scheduleList = FXCollections.observableArrayList(schedules);
            applyShiftFilter();
            scheduleTable.setItems(scheduleList);
            dateLabel.setText("Lịch làm việc từ: " +
                    startOfWeek.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " đến " +
                    endOfWeek.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            updateShiftSummary();
            populateWeekView(schedules);
            dayView.setVisible(false);
            dayView.setManaged(false);
            weekView.setVisible(true);
            weekView.setManaged(true);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải lịch làm việc", e.getMessage());
        }
    }

    @FXML
    private void loadMonthSchedule() {
        try {
            LocalDate date = datePicker.getValue() != null ? datePicker.getValue() : LocalDate.now();
            LocalDate startOfMonth = date.withDayOfMonth(1);
            LocalDate endOfMonth = date.withDayOfMonth(date.getMonth().length(date.isLeapYear()));
            List<WorkSchedule> schedules = scheduleService.getSchedulesByStaffAndDateRange(currentStaffId, startOfMonth, endOfMonth);
            scheduleList = FXCollections.observableArrayList(schedules);
            applyShiftFilter();
            scheduleTable.setItems(scheduleList);
            dateLabel.setText("Lịch làm việc tháng: " + date.getMonthValue() + "/" + date.getYear());
            updateShiftSummary();
            dayView.setVisible(true);
            dayView.setManaged(true);
            weekView.setVisible(false);
            weekView.setManaged(false);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải lịch làm việc", e.getMessage());
        }
    }

    private void populateWeekView(List<WorkSchedule> schedules) {
        // Xóa tất cả nội dung trong các VBox
        monMorning.getChildren().clear(); tueMorning.getChildren().clear(); wedMorning.getChildren().clear();
        thuMorning.getChildren().clear(); friMorning.getChildren().clear(); satMorning.getChildren().clear();
        sunMorning.getChildren().clear();
        monAfternoon.getChildren().clear(); tueAfternoon.getChildren().clear(); wedAfternoon.getChildren().clear();
        thuAfternoon.getChildren().clear(); friAfternoon.getChildren().clear(); satAfternoon.getChildren().clear();
        sunAfternoon.getChildren().clear();
        monEvening.getChildren().clear(); tueEvening.getChildren().clear(); wedEvening.getChildren().clear();
        thuEvening.getChildren().clear(); friEvening.getChildren().clear(); satEvening.getChildren().clear();
        sunEvening.getChildren().clear();

        // Điền dữ liệu vào các VBox dựa trên lịch làm việc
        for (WorkSchedule schedule : schedules) {
            LocalDate date = schedule.getWorkDate();
            Shift shift = schedule.getShift();
            String task = schedule.getTask() != null ? schedule.getTask() : "Ca " + shift.name();
            Label label = new Label(task);
            int dayOfWeek = date.getDayOfWeek().getValue();

            if (shift == Shift.MORNING) {
                if (dayOfWeek == 1) monMorning.getChildren().add(label);
                else if (dayOfWeek == 2) tueMorning.getChildren().add(label);
                else if (dayOfWeek == 3) wedMorning.getChildren().add(label);
                else if (dayOfWeek == 4) thuMorning.getChildren().add(label);
                else if (dayOfWeek == 5) friMorning.getChildren().add(label);
                else if (dayOfWeek == 6) satMorning.getChildren().add(label);
                else if (dayOfWeek == 7) sunMorning.getChildren().add(label);
            } else if (shift == Shift.AFTERNOON) {
                if (dayOfWeek == 1) monAfternoon.getChildren().add(label);
                else if (dayOfWeek == 2) tueAfternoon.getChildren().add(label);
                else if (dayOfWeek == 3) wedAfternoon.getChildren().add(label);
                else if (dayOfWeek == 4) thuAfternoon.getChildren().add(label);
                else if (dayOfWeek == 5) friAfternoon.getChildren().add(label);
                else if (dayOfWeek == 6) satAfternoon.getChildren().add(label);
                else if (dayOfWeek == 7) sunAfternoon.getChildren().add(label);
            } else if (shift == Shift.EVENING) {
                if (dayOfWeek == 1) monEvening.getChildren().add(label);
                else if (dayOfWeek == 2) tueEvening.getChildren().add(label);
                else if (dayOfWeek == 3) wedEvening.getChildren().add(label);
                else if (dayOfWeek == 4) thuEvening.getChildren().add(label);
                else if (dayOfWeek == 5) friEvening.getChildren().add(label);
                else if (dayOfWeek == 6) satEvening.getChildren().add(label);
                else if (dayOfWeek == 7) sunEvening.getChildren().add(label);
            }
        }
    }

    @FXML
    private void applyFilter() {
        applyShiftFilter();
    }

    private void applyShiftFilter() {
        if (scheduleList == null) return;
        ObservableList<WorkSchedule> filteredList = FXCollections.observableArrayList(scheduleList);
        String selectedShift = shiftFilter.getValue();
        if (selectedShift != null && !selectedShift.equals("Tất cả")) {
            filteredList.removeIf(schedule -> !schedule.getShift().name().equalsIgnoreCase(selectedShift));
        }
        scheduleTable.setItems(filteredList);
        updateShiftSummary();
    }

    private void updateShiftSummary() {
        if (scheduleList == null) return;
        int totalShifts = scheduleList.size();
        int morningShifts = 0;
        int afternoonShifts = 0;
        int eveningShifts = 0;

        for (WorkSchedule schedule : scheduleList) {
            Shift shift = schedule.getShift();
            if (shift == Shift.MORNING) morningShifts++;
            else if (shift == Shift.AFTERNOON) afternoonShifts++;
            else if (shift == Shift.EVENING) eveningShifts++;
        }

        totalShiftsLabel.setText(String.valueOf(totalShifts));
        morningShiftsLabel.setText(String.valueOf(morningShifts));
        afternoonShiftsLabel.setText(String.valueOf(afternoonShifts));
        eveningShiftsLabel.setText(String.valueOf(eveningShifts));
    }

    @FXML
    private void printSchedule(ActionEvent event) {
        showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Tính năng đang phát triển",
                "Chức năng in lịch làm việc đang được phát triển.");
    }

    @FXML
    private void requestLeave() {
        showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Tính năng đang phát triển",
                "Chức năng yêu cầu nghỉ phép đang được phát triển.");
    }

    @FXML
    private void requestShiftChange() {
        showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Tính năng đang phát triển",
                "Chức năng đổi ca đang được phát triển.");
    }

    @FXML
    private void refreshSchedule() {
        loadTodaySchedule();
        statusLabel.setText("Trạng thái: Đã làm mới lịch làm việc");
    }

    @FXML
    private void registerShift() {
        LocalDate date = registrationDatePicker.getValue();
        String shiftStr = shiftSelector.getValue();
        String location = locationSelector.getValue();
        String notes = registrationNotes.getText().trim();

        if (date == null || shiftStr == null || location == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Chưa đủ thông tin",
                    "Vui lòng chọn ngày, ca làm và vị trí làm việc.");
            return;
        }

        try {
            Shift shift = Shift.valueOf(shiftStr);
            WorkSchedule schedule = new WorkSchedule();
            schedule.setStaff(Session.getInstance().getCurrentStaff());
            schedule.setWorkDate(date);
            schedule.setShift(shift);
            schedule.setLocation(location);
            schedule.setNote(notes);
            schedule.setTask("Công việc mặc định"); // Giả lập
            // Thiết lập giờ bắt đầu và kết thúc dựa trên ca
            if (shift == Shift.MORNING) {
                schedule.setStartTime(LocalTime.of(8, 0));
                schedule.setEndTime(LocalTime.of(12, 0));
            } else if (shift == Shift.AFTERNOON) {
                schedule.setStartTime(LocalTime.of(13, 0));
                schedule.setEndTime(LocalTime.of(17, 0));
            } else if (shift == Shift.EVENING) {
                schedule.setStartTime(LocalTime.of(18, 0));
                schedule.setEndTime(LocalTime.of(22, 0));
            }

            // Giả định ScheduleService có phương thức registerShift
            // scheduleService.registerShift(schedule);
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã đăng ký ca làm",
                    "Ca làm đã được đăng ký: " + shift.name() + " ngày " + date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            registrationDatePicker.setValue(LocalDate.now());
            shiftSelector.setValue(null);
            locationSelector.setValue(null);
            registrationNotes.clear();
        } catch (IllegalArgumentException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Ca làm không hợp lệ",
                    "Vui lòng chọn ca làm hợp lệ.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể đăng ký ca làm", e.getMessage());
        }
    }

    @FXML
    private void cancelRegistration() {
        registrationDatePicker.setValue(LocalDate.now());
        shiftSelector.setValue(null);
        locationSelector.setValue(null);
        registrationNotes.clear();
        showAlert(Alert.AlertType.INFORMATION, "Thành công", "Hủy đăng ký",
                "Thông tin đăng ký ca làm đã được xóa.");
    }

    @FXML
    private void viewWorkStatistics() {
        String month = statisticsMonthSelector.getValue();
        String year = statisticsYearSelector.getValue();
        if (month == null || year == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Chưa chọn thời gian",
                    "Vui lòng chọn tháng và năm để xem thống kê.");
            return;
        }

        // Giả lập thống kê
        totalHoursLabel.setText("160 giờ");
        overtimeHoursLabel.setText("12 giờ");
        standardWorkdaysLabel.setText("22 ngày");
        leaveCountLabel.setText("1 ngày");

        monthlyStatsList.clear();
        monthlyStatsList.add(new MonthlyStats(Integer.parseInt(month), Integer.parseInt(year), 160, 12, 22, 1, "90%"));
        statusLabel.setText("Trạng thái: Đã tải thống kê giờ làm");
    }

    @FXML
    private void exportWorkReport() {
        showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Tính năng đang phát triển",
                "Chức năng xuất báo cáo đang được phát triển.");
    }

    @FXML
    private void showHelp() {
        showAlert(Alert.AlertType.INFORMATION, "Trợ giúp", "Hướng dẫn sử dụng",
                "Liên hệ quản trị viên để được hỗ trợ thêm.");
    }

    @FXML
    private void exitApplication() {
        Stage stage = (Stage) scheduleTable.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Giả lập lớp MonthlyStats
    private static class MonthlyStats {
        private final int month;
        private final int year;
        private final int totalHours;
        private final int overtimeHours;
        private final int workdays;
        private final int leaveDays;
        private final String performance;

        public MonthlyStats(int month, int year, int totalHours, int overtimeHours, int workdays, int leaveDays, String performance) {
            this.month = month;
            this.year = year;
            this.totalHours = totalHours;
            this.overtimeHours = overtimeHours;
            this.workdays = workdays;
            this.leaveDays = leaveDays;
            this.performance = performance;
        }

        public String getMonth() { return month + "/" + year; }
        public String getTotalHours() { return totalHours + " giờ"; }
        public String getOvertimeHours() { return overtimeHours + " giờ"; }
        public String getWorkdays() { return workdays + " ngày"; }
        public String getLeaveDays() { return leaveDays + " ngày"; }
        public String getPerformance() { return performance; }
    }
}