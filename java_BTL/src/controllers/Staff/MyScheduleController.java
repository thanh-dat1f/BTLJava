
package controllers.Staff;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.scene.layout.GridPane;
import enums.Shift;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import model.Staff;
import model.WorkSchedule;
import service.ScheduleService;
import utils.Session;

public class MyScheduleController implements Initializable {

    @FXML private Label dateLabel;
    @FXML private Label staffNameLabel;
    @FXML private Label positionLabel;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> viewModeSelector;
    @FXML private ComboBox<String> shiftFilter;
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
    @FXML private ComboBox<Shift> shiftSelector;
    @FXML private ComboBox<String> locationSelector;
    @FXML private TextArea registrationNotes;
    @FXML private ComboBox<String> statisticsMonthSelector;
    @FXML private ComboBox<String> statisticsYearSelector;
    @FXML private ComboBox<String> exportTypeSelector;
    @FXML private Label totalHoursLabel;
    @FXML private Label overtimeHoursLabel;
    @FXML private Label standardWorkdaysLabel;
    @FXML private Label leaveCountLabel;
    @FXML private TableView<MonthlyStats> monthlyStatsTable;
    @FXML private TableColumn<MonthlyStats, String> monthColumn;
    @FXML private TableColumn<MonthlyStats, String> totalHoursColumn;
    @FXML private TableColumn<MonthlyStats, String> overtimeHoursColumn;
    @FXML private TableColumn<MonthlyStats, String> workdaysColumn;
    @FXML private TableColumn<MonthlyStats, String> leaveDaysColumn;
    @FXML private TableColumn<MonthlyStats, String> performanceColumn;
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
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        scheduleService = new ScheduleService();

        // Get current staff information from Session
        Staff currentStaff = Session.getInstance().getCurrentStaff();
        if (currentStaff != null) {
            currentStaffId = currentStaff.getId();
            staffNameLabel.setText("Nhân viên: " + currentStaff.getFullName());
            positionLabel.setText("Vị trí: " + (currentStaff.getPosition() != null ? currentStaff.getPosition() : "Nhân viên"));
        } else {
            showAlert(AlertType.ERROR, "Lỗi", "Không tìm thấy thông tin nhân viên",
                    "Vui lòng đăng nhập lại.");
            return;
        }

        // Initialize table columns
        setupTableColumns();
        
        // Initialize ComboBoxes
        initializeComboBoxes();
        
        // Set default values for date pickers
        initializeDatePickers();
        
        // Initialize monthly stats table
        setupMonthlyStatsTable();
        
        // Load today's schedule by default
        loadScheduleByDate(LocalDate.now());
        
        // Add selection listener for schedule table
        scheduleTable.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> showScheduleDetails(newValue));

        // Add listener for viewModeSelector
        viewModeSelector.setOnAction(event -> handleViewModeChange());
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("scheduleID"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("workDate"));
        shiftColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getShift() != null ? cellData.getValue().getShift().name() : ""));
        startTimeColumn.setCellValueFactory(new PropertyValueFactory<>("startTime"));
        endTimeColumn.setCellValueFactory(new PropertyValueFactory<>("endTime"));
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
        taskColumn.setCellValueFactory(new PropertyValueFactory<>("task"));
        noteColumn.setCellValueFactory(new PropertyValueFactory<>("note"));

        // Format date column
        dateColumn.setCellFactory(column -> new TableCell<WorkSchedule, LocalDate>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : dateFormatter.format(item));
            }
        });

        // Format time columns
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        startTimeColumn.setCellFactory(column -> new TableCell<WorkSchedule, LocalTime>() {
            @Override
            protected void updateItem(LocalTime item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : timeFormatter.format(item));
            }
        });
        endTimeColumn.setCellFactory(column -> new TableCell<WorkSchedule, LocalTime>() {
            @Override
            protected void updateItem(LocalTime item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : timeFormatter.format(item));
            }
        });
    }
    
    private void initializeComboBoxes() {
        // Shift filter ComboBox
        shiftFilter.getItems().addAll("Tất cả", Shift.MORNING.name(), Shift.AFTERNOON.name(), Shift.EVENING.name());
        shiftFilter.setValue("Tất cả");
        
        // Shift selector for registration
        shiftSelector.getItems().addAll(Shift.MORNING, Shift.AFTERNOON, Shift.EVENING);
        shiftSelector.setConverter(new StringConverter<Shift>() {
            @Override
            public String toString(Shift shift) {
                if (shift == null) return null;
                switch(shift) {
                    case MORNING: return "Ca sáng (8:00 - 12:00)";
                    case AFTERNOON: return "Ca chiều (13:00 - 17:00)";
                    case EVENING: return "Ca tối (18:00 - 22:00)";
                    default: return shift.name();
                }
            }
            
            @Override
            public Shift fromString(String string) {
                return null; // Not needed for ComboBox
            }
        });
        
        // Location selector
        locationSelector.getItems().addAll("Chi nhánh 1", "Chi nhánh 2", "Chi nhánh 3");
        
        // Statistics month selector
        for (int i = 1; i <= 12; i++) {
            statisticsMonthSelector.getItems().add(String.format("%02d", i));
        }
        
        // Statistics year selector
        int currentYear = LocalDate.now().getYear();
        for (int i = currentYear - 2; i <= currentYear + 1; i++) {
            statisticsYearSelector.getItems().add(String.valueOf(i));
        }
        
        // Set default values
        statisticsMonthSelector.setValue(String.format("%02d", LocalDate.now().getMonthValue()));
        statisticsYearSelector.setValue(String.valueOf(LocalDate.now().getYear()));
        
        // Export type selector
        exportTypeSelector.getItems().addAll("Báo cáo thống kê", "Lịch làm việc");
        exportTypeSelector.setValue("Báo cáo thống kê");
        
        // View mode selector
        viewModeSelector.getItems().addAll("Hôm nay", "Tuần", "Tháng");
        viewModeSelector.setValue("Hôm nay");
    }
    
    private void initializeDatePickers() {
        // Set date picker format
        StringConverter<LocalDate> converter = new StringConverter<LocalDate>() {
            @Override
            public String toString(LocalDate date) {
                return (date != null) ? dateFormatter.format(date) : "";
            }

            @Override
            public LocalDate fromString(String string) {
                return (string != null && !string.isEmpty()) 
                        ? LocalDate.parse(string, dateFormatter) : null;
            }
        };
        
        datePicker.setConverter(converter);
        registrationDatePicker.setConverter(converter);
        
        // Set default values
        LocalDate today = LocalDate.now();
        datePicker.setValue(today);
        registrationDatePicker.setValue(today);
        
        // Add listener for date picker changes
        datePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                loadScheduleByDate(newValue);
            }
        });
    }
    
    private void setupMonthlyStatsTable() {
        monthlyStatsList = FXCollections.observableArrayList();
        
        if (monthColumn != null) monthColumn.setCellValueFactory(new PropertyValueFactory<>("month"));
        if (totalHoursColumn != null) totalHoursColumn.setCellValueFactory(new PropertyValueFactory<>("totalHours"));
        if (overtimeHoursColumn != null) overtimeHoursColumn.setCellValueFactory(new PropertyValueFactory<>("overtimeHours"));
        if (workdaysColumn != null) workdaysColumn.setCellValueFactory(new PropertyValueFactory<>("workdays"));
        if (leaveDaysColumn != null) leaveDaysColumn.setCellValueFactory(new PropertyValueFactory<>("leaveDays"));
        if (performanceColumn != null) performanceColumn.setCellValueFactory(new PropertyValueFactory<>("performance"));
        
        monthlyStatsTable.setItems(monthlyStatsList);
    }

    private void handleViewModeChange() {
        String mode = viewModeSelector.getValue();
        LocalDate date = datePicker.getValue() != null ? datePicker.getValue() : LocalDate.now();
        
        switch (mode) {
            case "Hôm nay":
                loadScheduleByDate(date);
                break;
            case "Tuần":
                loadWeekSchedule();
                break;
            case "Tháng":
                loadMonthSchedule();
                break;
        }
    }

    private void loadScheduleByDate(LocalDate date) {
        try {
            List<WorkSchedule> schedules = scheduleService.getSchedulesByStaffAndDate(currentStaffId, date);
            scheduleList = FXCollections.observableArrayList(schedules);
            applyShiftFilter();
            dateLabel.setText("Lịch làm việc ngày: " + date.format(dateFormatter));
            updateShiftSummary(schedules);
            showDayView();
            statusLabel.setText("Trạng thái: Đã tải lịch làm việc ngày " + date.format(dateFormatter));
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Lỗi", "Không thể tải lịch làm việc", e.getMessage());
        }
    }

    private void loadWeekSchedule() {
        try {
            LocalDate today = datePicker.getValue() != null ? datePicker.getValue() : LocalDate.now();
            LocalDate startOfWeek = today.minusDays(today.getDayOfWeek().getValue() - 1);
            LocalDate endOfWeek = startOfWeek.plusDays(6);
            
            List<WorkSchedule> schedules = scheduleService.getSchedulesByStaffAndDateRange(
                    currentStaffId, startOfWeek, endOfWeek);
            
            scheduleList = FXCollections.observableArrayList(schedules);
            applyShiftFilter();
            
            dateLabel.setText("Lịch làm việc từ: " +
                    startOfWeek.format(dateFormatter) + " đến " +
                    endOfWeek.format(dateFormatter));
            
            updateShiftSummary(schedules);
            populateWeekView(schedules);
            showWeekView();
            
            statusLabel.setText("Trạng thái: Đã tải lịch làm việc tuần từ " + 
                    startOfWeek.format(dateFormatter) + " đến " + 
                    endOfWeek.format(dateFormatter));
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Lỗi", "Không thể tải lịch làm việc theo tuần", 
                    e.getMessage());
        }
    }

    private void loadMonthSchedule() {
        try {
            LocalDate date = datePicker.getValue() != null ? datePicker.getValue() : LocalDate.now();
            LocalDate startOfMonth = date.withDayOfMonth(1);
            LocalDate endOfMonth = date.withDayOfMonth(date.getMonth().length(date.isLeapYear()));
            
            List<WorkSchedule> schedules = scheduleService.getSchedulesByStaffAndDateRange(
                    currentStaffId, startOfMonth, endOfMonth);
            
            scheduleList = FXCollections.observableArrayList(schedules);
            applyShiftFilter();
            
            dateLabel.setText("Lịch làm việc tháng: " + date.getMonthValue() + "/" + date.getYear());
            updateShiftSummary(schedules);
            showDayView();
            
            statusLabel.setText("Trạng thái: Đã tải lịch làm việc tháng " + 
                    date.getMonthValue() + "/" + date.getYear());
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Lỗi", "Không thể tải lịch làm việc theo tháng", 
                    e.getMessage());
        }
    }

    private void populateWeekView(List<WorkSchedule> schedules) {
        clearWeekViewContainers();
        
        for (WorkSchedule schedule : schedules) {
            LocalDate date = schedule.getWorkDate();
            Shift shift = schedule.getShift();
            
            if (date == null || shift == null) continue;
            
            String task = schedule.getTask() != null ? schedule.getTask() : "Ca làm việc";
            String time = schedule.getStartTime() != null && schedule.getEndTime() != null ?
                    schedule.getStartTime() + " - " + schedule.getEndTime() : "";
            Label label = new Label(task + "\n" + time);
            label.setStyle("-fx-padding: 5; -fx-background-color: #f8f9fa; -fx-background-radius: 3; -fx-text-alignment: center;");
            
            int dayOfWeek = date.getDayOfWeek().getValue();
            addToWeekViewContainer(dayOfWeek, shift, label);
        }
    }
    
    private void clearWeekViewContainers() {
        monMorning.getChildren().clear(); 
        tueMorning.getChildren().clear(); 
        wedMorning.getChildren().clear();
        thuMorning.getChildren().clear(); 
        friMorning.getChildren().clear(); 
        satMorning.getChildren().clear();
        sunMorning.getChildren().clear();
        
        monAfternoon.getChildren().clear(); 
        tueAfternoon.getChildren().clear(); 
        wedAfternoon.getChildren().clear();
        thuAfternoon.getChildren().clear(); 
        friAfternoon.getChildren().clear(); 
        satAfternoon.getChildren().clear();
        sunAfternoon.getChildren().clear();
        
        monEvening.getChildren().clear(); 
        tueEvening.getChildren().clear(); 
        wedEvening.getChildren().clear();
        thuEvening.getChildren().clear(); 
        friEvening.getChildren().clear(); 
        satEvening.getChildren().clear();
        sunEvening.getChildren().clear();
    }
    
    private void addToWeekViewContainer(int dayOfWeek, Shift shift, Label label) {
        if (shift == Shift.MORNING) {
            switch (dayOfWeek) {
                case 1: monMorning.getChildren().add(label); break;
                case 2: tueMorning.getChildren().add(label); break;
                case 3: wedMorning.getChildren().add(label); break;
                case 4: thuMorning.getChildren().add(label); break;
                case 5: friMorning.getChildren().add(label); break;
                case 6: satMorning.getChildren().add(label); break;
                case 7: sunMorning.getChildren().add(label); break;
            }
        } else if (shift == Shift.AFTERNOON) {
            switch (dayOfWeek) {
                case 1: monAfternoon.getChildren().add(label); break;
                case 2: tueAfternoon.getChildren().add(label); break;
                case 3: wedAfternoon.getChildren().add(label); break;
                case 4: thuAfternoon.getChildren().add(label); break;
                case 5: friAfternoon.getChildren().add(label); break;
                case 6: satAfternoon.getChildren().add(label); break;
                case 7: sunAfternoon.getChildren().add(label); break;
            }
        } else if (shift == Shift.EVENING) {
            switch (dayOfWeek) {
                case 1: monEvening.getChildren().add(label); break;
                case 2: tueEvening.getChildren().add(label); break;
                case 3: wedEvening.getChildren().add(label); break;
                case 4: thuEvening.getChildren().add(label); break;
                case 5: friEvening.getChildren().add(label); break;
                case 6: satEvening.getChildren().add(label); break;
                case 7: sunEvening.getChildren().add(label); break;
            }
        }
    }

    private void showScheduleDetails(WorkSchedule schedule) {
        if (schedule == null) {
            additionalInfoArea.clear();
            return;
        }
        
        StringBuilder details = new StringBuilder();
        details.append("Mã lịch: ").append(schedule.getScheduleID()).append("\n");
        details.append("Ngày: ").append(schedule.getWorkDate().format(dateFormatter)).append("\n");
        details.append("Ca: ").append(schedule.getShift() != null ? schedule.getShift().name() : "N/A").append("\n");
        
        if (schedule.getStartTime() != null) {
            details.append("Giờ bắt đầu: ").append(schedule.getStartTime()).append("\n");
        }
        
        if (schedule.getEndTime() != null) {
            details.append("Giờ kết thúc: ").append(schedule.getEndTime()).append("\n");
        }
        
        if (schedule.getLocation() != null && !schedule.getLocation().isEmpty()) {
            details.append("Địa điểm: ").append(schedule.getLocation()).append("\n");
        }
        
        if (schedule.getTask() != null && !schedule.getTask().isEmpty()) {
            details.append("Công việc: ").append(schedule.getTask()).append("\n");
        }
        
        if (schedule.getNote() != null && !schedule.getNote().isEmpty()) {
            details.append("Ghi chú: ").append(schedule.getNote());
        }
        
        additionalInfoArea.setText(details.toString());
    }

    @FXML
    private void applyFilter() {
        applyShiftFilter();
        statusLabel.setText("Trạng thái: Đã lọc danh sách theo ca làm việc");
    }

    private void applyShiftFilter() {
        if (scheduleList == null) return;
        
        ObservableList<WorkSchedule> filteredList = FXCollections.observableArrayList(scheduleList);
        String selectedShift = shiftFilter.getValue();
        
        if (selectedShift != null && !selectedShift.equals("Tất cả")) {
            filteredList.removeIf(schedule -> 
                schedule.getShift() == null || 
                !schedule.getShift().name().equalsIgnoreCase(selectedShift));
        }
        
        scheduleTable.setItems(filteredList);
    }

    private void updateShiftSummary(List<WorkSchedule> schedules) {
        int totalShifts = schedules.size();
        int morningShifts = 0;
        int afternoonShifts = 0;
        int eveningShifts = 0;

        for (WorkSchedule schedule : schedules) {
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

    private void showDayView() {
        dayView.setVisible(true);
        dayView.setManaged(true);
        weekView.setVisible(false);
        weekView.setManaged(false);
    }
    
    private void showWeekView() {
        dayView.setVisible(false);
        dayView.setManaged(false);
        weekView.setVisible(true);
        weekView.setManaged(true);
    }

    @FXML
    private void requestLeave() {
        Dialog<Map<String, Object>> dialog = new Dialog<>();
        dialog.setTitle("Yêu cầu nghỉ phép");
        dialog.setHeaderText("Đăng ký nghỉ phép");
        
        ButtonType requestButtonType = new ButtonType("Gửi yêu cầu", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(requestButtonType, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));
        
        DatePicker leaveDatePicker = new DatePicker(LocalDate.now());
        TextArea reasonTextArea = new TextArea();
        reasonTextArea.setPromptText("Nhập lý do nghỉ phép");
        
        grid.add(new Label("Ngày nghỉ:"), 0, 0);
        grid.add(leaveDatePicker, 1, 0);
        grid.add(new Label("Lý do:"), 0, 1);
        grid.add(reasonTextArea, 1, 1);
        
        dialog.getDialogPane().setContent(grid);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == requestButtonType) {
                Map<String, Object> result = new java.util.HashMap<>();
                result.put("date", leaveDatePicker.getValue());
                result.put("reason", reasonTextArea.getText());
                return result;
            }
            return null;
        });
        
        Optional<Map<String, Object>> result = dialog.showAndWait();
        
        result.ifPresent(data -> {
            LocalDate leaveDate = (LocalDate) data.get("date");
            String reason = (String) data.get("reason");
            
            if (leaveDate == null) {
                showAlert(AlertType.WARNING, "Cảnh báo", "Thiếu thông tin", 
                          "Vui lòng chọn ngày nghỉ phép.");
                return;
            }
            
            if (reason == null || reason.trim().isEmpty()) {
                showAlert(AlertType.WARNING, "Cảnh báo", "Thiếu thông tin", 
                          "Vui lòng nhập lý do nghỉ phép.");
                return;
            }
            
            boolean success = scheduleService.requestLeave(currentStaffId, leaveDate, reason);
            
            if (success) {
                showAlert(AlertType.INFORMATION, "Thành công", "Đã gửi yêu cầu nghỉ phép", 
                          "Yêu cầu nghỉ phép của bạn đã được gửi và đang chờ xét duyệt.");
                statusLabel.setText("Trạng thái: Đã gửi yêu cầu nghỉ phép");
            } else {
                showAlert(AlertType.ERROR, "Lỗi", "Không thể gửi yêu cầu", 
                          "Đã xảy ra lỗi khi gửi yêu cầu nghỉ phép. Vui lòng thử lại sau.");
            }
        });
    }

    @FXML
    private void requestShiftChange() {
        Dialog<Map<String, Object>> dialog = new Dialog<>();
        dialog.setTitle("Yêu cầu đổi ca");
        dialog.setHeaderText("Đăng ký đổi ca làm việc");
        
        ButtonType requestButtonType = new ButtonType("Gửi yêu cầu", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(requestButtonType, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));
        
        DatePicker currentDatePicker = new DatePicker(LocalDate.now());
        ComboBox<Shift> currentShiftComboBox = new ComboBox<>();
        currentShiftComboBox.getItems().addAll(Shift.MORNING, Shift.AFTERNOON, Shift.EVENING);
        currentShiftComboBox.setConverter(new StringConverter<Shift>() {
            @Override
            public String toString(Shift shift) {
                if (shift == null) return null;
                switch(shift) {
                    case MORNING: return "Ca sáng (8:00 - 12:00)";
                    case AFTERNOON: return "Ca chiều (13:00 - 17:00)";
                    case EVENING: return "Ca tối (18:00 - 22:00)";
                    default: return shift.name();
                }
            }
            
            @Override
            public Shift fromString(String string) {
                return null;
            }
        });
        
        DatePicker desiredDatePicker = new DatePicker(LocalDate.now().plusDays(1));
        ComboBox<Shift> desiredShiftComboBox = new ComboBox<>();
        desiredShiftComboBox.getItems().addAll(Shift.MORNING, Shift.AFTERNOON, Shift.EVENING);
        desiredShiftComboBox.setConverter(currentShiftComboBox.getConverter());
        
        TextArea reasonTextArea = new TextArea();
        reasonTextArea.setPromptText("Nhập lý do đổi ca");
        
        grid.add(new Label("Ngày hiện tại:"), 0, 0);
        grid.add(currentDatePicker, 1, 0);
        grid.add(new Label("Ca hiện tại:"), 0, 1);
        grid.add(currentShiftComboBox, 1, 1);
        grid.add(new Label("Ngày muốn đổi:"), 0, 2);
        grid.add(desiredDatePicker, 1, 2);
        grid.add(new Label("Ca muốn đổi:"), 0, 3);
        grid.add(desiredShiftComboBox, 1, 3);
        grid.add(new Label("Lý do:"), 0, 4);
        grid.add(reasonTextArea, 1, 4);
        
        dialog.getDialogPane().setContent(grid);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == requestButtonType) {
                Map<String, Object> result = new java.util.HashMap<>();
                result.put("currentDate", currentDatePicker.getValue());
                result.put("currentShift", currentShiftComboBox.getValue());
                result.put("desiredDate", desiredDatePicker.getValue());
                result.put("desiredShift", desiredShiftComboBox.getValue());
                result.put("reason", reasonTextArea.getText());
                return result;
            }
            return null;
        });
        
        Optional<Map<String, Object>> result = dialog.showAndWait();
        
        result.ifPresent(data -> {
            LocalDate currentDate = (LocalDate) data.get("currentDate");
            Shift currentShift = (Shift) data.get("currentShift");
            LocalDate desiredDate = (LocalDate) data.get("desiredDate");
            Shift desiredShift = (Shift) data.get("desiredShift");
            String reason = (String) data.get("reason");
            
            if (currentDate == null || currentShift == null || 
                desiredDate == null || desiredShift == null) {
                showAlert(AlertType.WARNING, "Cảnh báo", "Thiếu thông tin", 
                          "Vui lòng điền đầy đủ thông tin ca làm việc.");
                return;
            }
            
            if (reason == null || reason.trim().isEmpty()) {
                showAlert(AlertType.WARNING, "Cảnh báo", "Thiếu thông tin", 
                          "Vui lòng nhập lý do đổi ca.");
                return;
            }
            
            boolean success = scheduleService.requestShiftChange(
                    currentStaffId, currentDate, currentShift, desiredDate, desiredShift, reason);
            
            if (success) {
                showAlert(AlertType.INFORMATION, "Thành công", "Đã gửi yêu cầu đổi ca",
                        "Yêu cầu đổi ca đã được gửi và đang chờ xét duyệt.");
                statusLabel.setText("Trạng thái: Đã gửi yêu cầu đổi ca");
            } else {
                showAlert(AlertType.ERROR, "Lỗi", "Không thể gửi yêu cầu đổi ca",
                        "Ca hiện tại không tồn tại hoặc ca mong muốn đã được đăng ký.");
            }
        });
    }

    @FXML
    private void refreshSchedule() {
        String mode = viewModeSelector.getValue();
        switch (mode) {
            case "Hôm nay":
                loadScheduleByDate(datePicker.getValue());
                break;
            case "Tuần":
                loadWeekSchedule();
                break;
            case "Tháng":
                loadMonthSchedule();
                break;
        }
        statusLabel.setText("Trạng thái: Đã làm mới lịch làm việc");
    }

    @FXML
    private void registerShift() {
        LocalDate date = registrationDatePicker.getValue();
        Shift shift = shiftSelector.getValue();
        String location = locationSelector.getValue();
        String notes = registrationNotes.getText().trim();

        if (date == null || shift == null || location == null) {
            showAlert(AlertType.WARNING, "Cảnh báo", "Chưa đủ thông tin",
                    "Vui lòng chọn ngày, ca làm và vị trí làm việc.");
            return;
        }

        try {
            boolean success = scheduleService.registerShift(
                    currentStaffId, date, shift, location, notes);
            
            if (success) {
                showAlert(AlertType.INFORMATION, "Thành công", "Đã đăng ký ca làm",
                        "Ca làm đã được đăng ký: " + shift.name() + " ngày " + 
                        date.format(dateFormatter));
                
                registrationDatePicker.setValue(LocalDate.now());
                shiftSelector.setValue(null);
                locationSelector.setValue(null);
                registrationNotes.clear();
                
                statusLabel.setText("Trạng thái: Đã đăng ký ca làm thành công");
                
                if (date.equals(datePicker.getValue())) {
                    loadScheduleByDate(date);
                }
            } else {
                showAlert(AlertType.ERROR, "Lỗi", "Không thể đăng ký ca làm",
                        "Có thể ca làm này đã được đăng ký hoặc có xung đột lịch. Vui lòng kiểm tra lại.");
            }
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Lỗi", "Không thể đăng ký ca làm", e.getMessage());
        }
    }

    @FXML
    private void cancelRegistration() {
        registrationDatePicker.setValue(LocalDate.now());
        shiftSelector.setValue(null);
        locationSelector.setValue(null);
        registrationNotes.clear();
        statusLabel.setText("Trạng thái: Đã hủy đăng ký ca làm");
    }

    @FXML
    private void viewWorkStatistics() {
        String monthStr = statisticsMonthSelector.getValue();
        String yearStr = statisticsYearSelector.getValue();
        
        if (monthStr == null || yearStr == null) {
            showAlert(AlertType.WARNING, "Cảnh báo", "Chưa chọn thời gian",
                    "Vui lòng chọn tháng và năm để xem thống kê.");
            return;
        }
        
        try {
            int month = Integer.parseInt(monthStr);
            int year = Integer.parseInt(yearStr);
            
            Map<String, Object> stats = scheduleService.getMonthlyStatistics(currentStaffId, month, year);
            
            totalHoursLabel.setText(stats.get("totalHours") + " giờ");
            overtimeHoursLabel.setText(stats.get("overtimeHours") + " giờ");
            standardWorkdaysLabel.setText(stats.get("standardWorkdays") + " ngày");
            leaveCountLabel.setText(stats.get("leaveCount") + " ngày");
            
            int expectedHours = (int) stats.get("standardWorkdays") * 8;
            int totalHours = (int) stats.get("totalHours");
            String performance = expectedHours > 0 ? 
                    String.format("%.0f%%", (totalHours * 100.0 / expectedHours)) : "N/A";
            
            monthlyStatsList.clear();
            monthlyStatsList.add(new MonthlyStats(
                    month, 
                    year, 
                    (int)stats.get("totalHours"), 
                    (int)stats.get("overtimeHours"), 
                    (int)stats.get("standardWorkdays"), 
                    (int)stats.get("leaveCount"), 
                    performance));
            
            statusLabel.setText("Trạng thái: Đã tải thống kê giờ làm tháng " + month + "/" + year);
        } catch (NumberFormatException e) {
            showAlert(AlertType.ERROR, "Lỗi", "Dữ liệu không hợp lệ",
                    "Tháng và năm phải là số nguyên.");
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Lỗi", "Không thể tải thống kê", e.getMessage());
        }
    }

    @FXML
    private void exportWorkReport() {
        String exportType = exportTypeSelector.getValue();
        String monthStr = statisticsMonthSelector.getValue();
        String yearStr = statisticsYearSelector.getValue();
        
        if (monthStr == null || yearStr == null) {
            showAlert(AlertType.WARNING, "Cảnh báo", "Chưa chọn thời gian",
                    "Vui lòng chọn tháng và năm để xuất báo cáo.");
            return;
        }
        
        try {
            int month = Integer.parseInt(monthStr);
            int year = Integer.parseInt(yearStr);
            String fileName = (exportType.equals("Báo cáo thống kê") ? 
                    "work_statistics_" : "work_schedule_") + year + "_" + month + ".csv";
            
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Lưu báo cáo");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
            fileChooser.setInitialFileName(fileName);
            
            File file = fileChooser.showSaveDialog(null);
            if (file != null) {
                try (FileWriter writer = new FileWriter(file)) {
                    if (exportType.equals("Báo cáo thống kê")) {
                        Map<String, Object> stats = scheduleService.getMonthlyStatistics(currentStaffId, month, year);
                        writer.write("Thống kê,Giá trị\n");
                        writer.append("Tổng giờ làm việc,").append(stats.get("totalHours") + " giờ\n");
                        writer.append("Tăng ca,").append(stats.get("overtimeHours") + " giờ\n");
                        writer.append("Ngày công chuẩn,").append(stats.get("standardWorkdays") + " ngày\n");
                        writer.append("Ngày nghỉ phép,").append(stats.get("leaveCount") + " ngày\n");
                    } else {
                        LocalDate startDate = LocalDate.of(year, month, 1);
                        LocalDate endDate = startDate.withDayOfMonth(startDate.getMonth().length(startDate.isLeapYear()));
                        List<WorkSchedule> schedules = scheduleService.getSchedulesByStaffAndDateRange(
                                currentStaffId, startDate, endDate);
                        
                        writer.append("Mã lịch,Ngày,Ca,Giờ bắt đầu,Giờ kết thúc,Địa điểm,Công việc,Ghi chú\n");
                        for (WorkSchedule schedule : schedules) {
                            writer.append(String.format("%d,%s,%s,%s,%s,%s,%s,%s\n",
                                    schedule.getScheduleID(),
                                    schedule.getWorkDate().format(dateFormatter),
                                    schedule.getShift() != null ? schedule.getShift().name() : "",
                                    schedule.getStartTime() != null ? schedule.getStartTime().toString() : "",
                                    schedule.getEndTime() != null ? schedule.getEndTime().toString() : "",
                                    schedule.getLocation() != null ? schedule.getLocation() : "",
                                    schedule.getTask() != null ? schedule.getTask() : "",
                                    schedule.getNote() != null ? schedule.getNote() : ""));
                        }
                    }
                    showAlert(AlertType.INFORMATION, "Thành công", "Đã xuất báo cáo",
                            "Báo cáo đã được lưu tại: " + file.getAbsolutePath());
                    statusLabel.setText("Trạng thái: Đã xuất " + exportType.toLowerCase());
                }
            }
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Lỗi", "Không thể xuất báo cáo", e.getMessage());
        }
    }

    @FXML
    private void showHelp() {
        showAlert(AlertType.INFORMATION, "Trợ giúp", "Hướng dẫn sử dụng",
                "Phần quản lý lịch làm việc cho phép bạn:\n\n" +
                "- Xem lịch làm việc theo ngày, tuần, tháng\n" +
                "- Đăng ký ca làm việc mới\n" +
                "- Xem thống kê giờ làm\n" +
                "- Yêu cầu nghỉ phép hoặc đổi ca\n\n" +
                "Liên hệ quản trị viên để được hỗ trợ thêm.");
    }

    @FXML
    private void exitApplication() {
        Stage stage = (Stage) scheduleTable.getScene().getWindow();
        stage.close();
    }

    private void showAlert(AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static class MonthlyStats {
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

        public String getMonth() { return String.format("%02d/%d", month, year); }
        public String getTotalHours() { return totalHours + " giờ"; }
        public String getOvertimeHours() { return overtimeHours + " giờ"; }
        public String getWorkdays() { return workdays + " ngày"; }
        public String getLeaveDays() { return leaveDays + " ngày"; }
        public String getPerformance() { return performance; }
    }
}
