package controllers.Staff;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import model.Booking;
import model.BookingDetail;
import model.Customer;
import model.Pet;
import model.Service;
import model.Staff;
import service.BookingService;
import controllers.SceneSwitcher;
import utils.Session;
import utils.RoleChecker;
import enums.StatusEnum;

public class BookingViewController implements Initializable {

    @FXML private Label currentDateLabel;
    @FXML private Label staffNameLabel;
    @FXML private DatePicker datePicker;
    @FXML private Button todayButton;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilter;
    @FXML private Button confirmArrivalButton;
    @FXML private Button startButton;
    @FXML private Button completeButton;
    @FXML private Button printInvoiceButton;
    @FXML private TableView<Booking> bookingTable;
    @FXML private TableColumn<Booking, Integer> idColumn;
    @FXML private TableColumn<Booking, LocalDateTime> timeColumn;
    @FXML private TableColumn<Booking, String> customerColumn;
    @FXML private TableColumn<Booking, String> phoneColumn;
    @FXML private TableColumn<Booking, String> petColumn;
    @FXML private TableColumn<Booking, String> serviceColumn;
    @FXML private TableColumn<Booking, String> statusColumn;
    @FXML private TableColumn<Booking, String> assignedStaffColumn;
    @FXML private TextArea notesArea;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private ComboBox<String> upcomingStatusFilter;
    @FXML private TableView<Booking> upcomingBookingTable;
    @FXML private TableColumn<Booking, Integer> upcomingIdColumn;
    @FXML private TableColumn<Booking, LocalDate> upcomingDateColumn;
    @FXML private TableColumn<Booking, String> upcomingTimeColumn;
    @FXML private TableColumn<Booking, String> upcomingCustomerColumn;
    @FXML private TableColumn<Booking, String> upcomingPhoneColumn;
    @FXML private TableColumn<Booking, String> upcomingPetColumn;
    @FXML private TableColumn<Booking, String> upcomingServiceColumn;
    @FXML private TableColumn<Booking, String> upcomingStatusColumn;
    @FXML private TableColumn<Booking, String> upcomingStaffColumn;
    @FXML private ComboBox<String> monthSelector;
    @FXML private ComboBox<String> yearSelector;
    @FXML private Label totalBookingsLabel;
    @FXML private Label bookingTrendLabel;
    @FXML private Label completionRateLabel;
    @FXML private Label completionTrendLabel;
    @FXML private Label popularServiceLabel;
    @FXML private Label servicePercentLabel;
    @FXML private Label loyalCustomerLabel;
    @FXML private Label statusMessageLabel;

    private BookingService bookingService;
    private ObservableList<Booking> bookingList;
    private ObservableList<Booking> upcomingBookingList;
    private Booking selectedBooking;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        bookingService = new BookingService();

        // Initialize current date and staff name
        currentDateLabel.setText("Ngày hiện tại: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        Staff currentStaff = Session.getInstance().getCurrentStaff();
        staffNameLabel.setText("Nhân viên: " + (currentStaff != null ? currentStaff.getFullName() : "N/A"));

        // Initialize today's bookings table
        idColumn.setCellValueFactory(new PropertyValueFactory<>("bookingId"));
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("bookingTime"));
        timeColumn.setCellFactory(column -> new TableCell<Booking, LocalDateTime>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : formatter.format(item));
            }
        });
        customerColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getCustomer() != null ? cellData.getValue().getCustomer().getFullName() : ""));
        phoneColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getCustomer() != null ? cellData.getValue().getCustomer().getPhone() : ""));
        petColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getPet() != null ? cellData.getValue().getPet().getName() : ""));
        serviceColumn.setCellValueFactory(cellData -> new SimpleStringProperty(getServiceNameFromBooking(cellData.getValue())));
        statusColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getStatus() != null ? cellData.getValue().getStatus().name() : ""));
        assignedStaffColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getAssignedStaff() != null ? cellData.getValue().getAssignedStaff().getFullName() : ""));

        // Initialize upcoming bookings table
        upcomingIdColumn.setCellValueFactory(new PropertyValueFactory<>("bookingId"));
        upcomingDateColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getBookingTime().toLocalDate().toString()));
        upcomingTimeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getBookingTime().format(DateTimeFormatter.ofPattern("HH:mm"))));
        upcomingCustomerColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getCustomer() != null ? cellData.getValue().getCustomer().getFullName() : ""));
        upcomingPhoneColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getCustomer() != null ? cellData.getValue().getCustomer().getPhone() : ""));
        upcomingPetColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getPet() != null ? cellData.getValue().getPet().getName() : ""));
        upcomingServiceColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                getServiceNameFromBooking(cellData.getValue())));
        upcomingStatusColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getStatus() != null ? cellData.getValue().getStatus().name() : ""));
        upcomingStaffColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getAssignedStaff() != null ? cellData.getValue().getAssignedStaff().getFullName() : ""));

        // Initialize status filters
        ObservableList<String> statusOptions = FXCollections.observableArrayList(
                "", "PENDING", "CONFIRMED", "COMPLETED", "CANCELLED");
        statusFilter.setItems(statusOptions);
        upcomingStatusFilter.setItems(statusOptions);

        // Initialize month and year selectors
        ObservableList<String> months = FXCollections.observableArrayList(
                "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12");
        monthSelector.setItems(months);
        ObservableList<String> years = FXCollections.observableArrayList(
                "2023", "2024", "2025", "2026");
        yearSelector.setItems(years);

        // Load initial data
        loadTodaySchedule(null);
        loadUpcomingBookings();
        loadStatistics();

        // Setup selection listeners
        bookingTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, newValue) -> handleBookingSelection(newValue));
        upcomingBookingTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, newValue) -> handleBookingSelection(newValue));

        // Setup button visibility based on roles
        setupButtonVisibility();
    }

    private String getServiceNameFromBooking(Booking booking) {
        if (booking == null) return "";
        try {
            List<BookingDetail> details = bookingService.getBookingDetails(booking.getBookingId());
            if (details != null && !details.isEmpty()) {
                return details.get(0).getService().getName();
            }
        } catch (Exception e) {
            // Log error if necessary
        }
        return "Không có thông tin";
    }

    private void setupButtonVisibility() {
        boolean canMarkServiceDone = RoleChecker.hasPermission("MARK_SERVICE_DONE");
        boolean canCreateBooking = RoleChecker.hasPermission("CREATE_BOOKING");
        boolean canPrintReceipt = RoleChecker.hasPermission("PRINT_RECEIPT");
        boolean canManagePayment = RoleChecker.hasPermission("MANAGE_PAYMENT");

        startButton.setVisible(canMarkServiceDone);
        completeButton.setVisible(canMarkServiceDone);
        printInvoiceButton.setVisible(canPrintReceipt || canManagePayment);
        confirmArrivalButton.setVisible(canCreateBooking);
    }

    @FXML
    private void loadTodaySchedule(ActionEvent event) {
        try {
            LocalDate date = datePicker.getValue() != null ? datePicker.getValue() : LocalDate.now();
            Staff currentStaff = Session.getInstance().getCurrentStaff();
            List<Booking> bookings;
            if (currentStaff == null) {
                bookings = bookingService.getBookingsByDate(date);
            } else if (RoleChecker.hasPermission("VIEW_ALL_BOOKINGS")) {
                bookings = bookingService.getBookingsByDate(date);
            } else {
                bookings = bookingService.getBookingsByStaffIdAndDate(currentStaff.getId(), date);
            }
            bookingList = FXCollections.observableArrayList(bookings);
            bookingTable.setItems(bookingList);
            statusMessageLabel.setText("Đã tải danh sách lịch hẹn hôm nay");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải danh sách đặt lịch", e.getMessage());
        }
    }

    @FXML
    private void searchBookings(ActionEvent event) {
        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            loadTodaySchedule(null);
            return;
        }
        try {
            List<Booking> bookings = bookingService.searchBookings(query);
            bookingList = FXCollections.observableArrayList(bookings);
            bookingTable.setItems(bookingList);
            statusMessageLabel.setText("Đã tìm thấy " + bookings.size() + " kết quả");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tìm kiếm", e.getMessage());
        }
    }

    @FXML
    private void applyFilters(ActionEvent event) {
        String status = statusFilter.getValue();
        try {
            LocalDate date = datePicker.getValue() != null ? datePicker.getValue() : LocalDate.now();
            Staff currentStaff = Session.getInstance().getCurrentStaff();
            List<Booking> bookings;
            if (currentStaff == null || RoleChecker.hasPermission("VIEW_ALL_BOOKINGS")) {
                bookings = bookingService.getBookingsByDateAndStatus(date, status);
            } else {
                bookings = bookingService.getBookingsByStaffIdDateAndStatus(currentStaff.getId(), date, status);
            }
            bookingList = FXCollections.observableArrayList(bookings);
            bookingTable.setItems(bookingList);
            statusMessageLabel.setText("Đã áp dụng bộ lọc");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể áp dụng bộ lọc", e.getMessage());
        }
    }

    @FXML
    private void handleNewBooking(ActionEvent event) {
        try {
            Stage currentStage = (Stage) bookingTable.getScene().getWindow();
            SceneSwitcher.switchToNewBookingScene(currentStage);
            loadTodaySchedule(null); // Refresh after adding new booking
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể mở màn hình đặt lịch mới", e.getMessage());
        }
    }

    @FXML
    private void refreshBookings(ActionEvent event) {
        loadTodaySchedule(null);
        statusMessageLabel.setText("Đã làm mới danh sách");
    }

    @FXML
    private void confirmArrival(ActionEvent event) {
        if (selectedBooking == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Chưa chọn lịch đặt",
                    "Vui lòng chọn một lịch đặt để xác nhận khách đến.");
            return;
        }
        try {
            bookingService.updateBookingStatus(selectedBooking.getBookingId(), "CONFIRMED");
            loadTodaySchedule(null);
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã xác nhận khách đến",
                    "Đã xác nhận khách đến cho lịch đặt #" + selectedBooking.getBookingId());
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể xác nhận khách đến", e.getMessage());
        }
    }

    @FXML
    private void startService(ActionEvent event) {
        if (selectedBooking == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Chưa chọn lịch đặt",
                    "Vui lòng chọn một lịch đặt để bắt đầu dịch vụ.");
            return;
        }
        try {
            bookingService.updateBookingStatus(selectedBooking.getBookingId(), "CONFIRMED");
            loadTodaySchedule(null);
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã bắt đầu dịch vụ",
                    "Dịch vụ đã được bắt đầu cho lịch đặt #" + selectedBooking.getBookingId());
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể bắt đầu dịch vụ", e.getMessage());
        }
    }

    @FXML
    private void completeService(ActionEvent event) {
        if (selectedBooking == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Chưa chọn lịch đặt",
                    "Vui lòng chọn một lịch đặt để hoàn thành dịch vụ.");
            return;
        }
        try {
            bookingService.updateBookingStatus(selectedBooking.getBookingId(), "COMPLETED");
            loadTodaySchedule(null);
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã hoàn thành dịch vụ",
                    "Dịch vụ đã được hoàn thành cho lịch đặt #" + selectedBooking.getBookingId());
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể hoàn thành dịch vụ", e.getMessage());
        }
    }

    @FXML
    private void printInvoice(ActionEvent event) {
        if (selectedBooking == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Chưa chọn lịch đặt",
                    "Vui lòng chọn một lịch đặt để in hóa đơn.");
            return;
        }
        try {
            Stage currentStage = (Stage) printInvoiceButton.getScene().getWindow();
            SceneSwitcher.switchToInvoiceScene(currentStage, selectedBooking.getBookingId());
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể mở màn hình hóa đơn", e.getMessage());
        }
    }

    private void handleBookingSelection(Booking booking) {
        selectedBooking = booking;
        boolean hasSelection = (booking != null);
        if (!hasSelection) {
            confirmArrivalButton.setDisable(true);
            startButton.setDisable(true);
            completeButton.setDisable(true);
            printInvoiceButton.setDisable(true);
            notesArea.clear();
            return;
        }
        StatusEnum status = booking.getStatus();
        boolean isPending = status == StatusEnum.PENDING;
        boolean isConfirmed = status == StatusEnum.CONFIRMED;
        boolean canComplete = isConfirmed;

        confirmArrivalButton.setDisable(!isPending);
        startButton.setDisable(!(isPending || isConfirmed));
        completeButton.setDisable(!canComplete);
        printInvoiceButton.setDisable(false);

        String notes = booking.getNote();
        notesArea.setText(notes != null ? notes : "Không có ghi chú");
    }

    @FXML
    private void viewDateRange(ActionEvent event) {
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        if (startDate == null || endDate == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Chưa chọn ngày",
                    "Vui lòng chọn ngày bắt đầu và kết thúc.");
            return;
        }
        if (startDate.isAfter(endDate)) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Ngày không hợp lệ",
                    "Ngày bắt đầu phải trước ngày kết thúc.");
            return;
        }
        loadUpcomingBookings();
    }

    private void loadUpcomingBookings() {
        try {
            LocalDate startDate = startDatePicker.getValue() != null ? startDatePicker.getValue() : LocalDate.now();
            LocalDate endDate = endDatePicker.getValue() != null ? endDatePicker.getValue() : startDate.plusDays(7);
            Staff currentStaff = Session.getInstance().getCurrentStaff();
            List<Booking> bookings;
            if (currentStaff == null || RoleChecker.hasPermission("VIEW_ALL_BOOKINGS")) {
                bookings = bookingService.getBookingsByDateRange(startDate, endDate);
            } else {
                bookings = bookingService.getBookingsByStaffIdAndDateRange(currentStaff.getId(), startDate, endDate);
            }
            String status = upcomingStatusFilter.getValue();
            if (status != null && !status.isEmpty()) {
                bookings = bookings.stream()
                        .filter(b -> b.getStatus().name().equals(status))
                        .collect(Collectors.toList());
            }
            upcomingBookingList = FXCollections.observableArrayList(bookings);
            upcomingBookingTable.setItems(upcomingBookingList);
            statusMessageLabel.setText("Đã tải danh sách lịch hẹn sắp tới");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải danh sách lịch hẹn", e.getMessage());
        }
    }

    @FXML
    private void applyUpcomingFilters(ActionEvent event) {
        loadUpcomingBookings();
    }

    @FXML
    private void sendReminders(ActionEvent event) {
        try {
            List<Booking> selectedBookings = upcomingBookingTable.getSelectionModel().getSelectedItems();
            if (selectedBookings.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Chưa chọn lịch hẹn",
                        "Vui lòng chọn ít nhất một lịch hẹn để gửi nhắc nhở.");
                return;
            }
            for (Booking booking : selectedBookings) {
                bookingService.sendReminder(booking.getBookingId());
            }
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã gửi nhắc nhở",
                    "Nhắc nhở đã được gửi cho " + selectedBookings.size() + " lịch hẹn.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể gửi nhắc nhở", e.getMessage());
        }
        statusMessageLabel.setText("Đã gửi nhắc nhở");
    }

    @FXML
    private void exportBookingList(ActionEvent event) {
        try {
            bookingService.exportBookings(upcomingBookingList);
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã xuất danh sách",
                    "Danh sách lịch hẹn đã được xuất.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể xuất danh sách", e.getMessage());
        }
        statusMessageLabel.setText("Đã xuất danh sách lịch hẹn");
    }

    @FXML
    private void viewStatistics(ActionEvent event) {
        loadStatistics();
    }

    private void loadStatistics() {
        try {
            String month = monthSelector.getValue();
            String year = yearSelector.getValue();
            if (month == null || year == null) {
                month = String.valueOf(LocalDate.now().getMonthValue());
                year = String.valueOf(LocalDate.now().getYear());
            }
            int monthInt = Integer.parseInt(month);
            int yearInt = Integer.parseInt(year);

            int totalBookings = bookingService.getTotalBookings(monthInt, yearInt);
            double bookingTrend = bookingService.getBookingTrend(monthInt, yearInt);
            double completionRate = bookingService.getCompletionRate(monthInt, yearInt);
            double completionTrend = bookingService.getCompletionTrend(monthInt, yearInt);
            Service popularService = bookingService.getPopularService(monthInt, yearInt);
            double servicePercent = bookingService.getServicePercentage(monthInt, yearInt);
            int loyalCustomers = bookingService.getLoyalCustomers(monthInt, yearInt);

            totalBookingsLabel.setText(String.valueOf(totalBookings));
            bookingTrendLabel.setText(String.format("%.1f%% %s", Math.abs(bookingTrend), bookingTrend >= 0 ? "↑" : "↓"));
            completionRateLabel.setText(String.format("%.1f%%", completionRate));
            completionTrendLabel.setText(String.format("%.1f%% %s", Math.abs(completionTrend), completionTrend >= 0 ? "↑" : "↓"));
            popularServiceLabel.setText(popularService != null ? popularService.getName() : "N/A");
            servicePercentLabel.setText(String.format("%.1f%% tổng số lịch hẹn", servicePercent));
            loyalCustomerLabel.setText(String.valueOf(loyalCustomers) + " khách hàng");

            statusMessageLabel.setText("Đã tải thống kê");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải thống kê", e.getMessage());
        }
    }

    @FXML
    private void exportReport(ActionEvent event) {
        try {
            String month = monthSelector.getValue();
            String year = yearSelector.getValue();
            if (month == null || year == null) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Chưa chọn thời gian",
                        "Vui lòng chọn tháng và năm để xuất báo cáo.");
                return;
            }
            bookingService.exportStatisticsReport(Integer.parseInt(month), Integer.parseInt(year));
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã xuất báo cáo",
                    "Báo cáo thống kê đã được xuất.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể xuất báo cáo", e.getMessage());
        }
        statusMessageLabel.setText("Đã xuất báo cáo thống kê");
    }

    @FXML
    private void showHelp(ActionEvent event) {
        showAlert(Alert.AlertType.INFORMATION, "Trợ giúp", "Hướng dẫn sử dụng",
                "Liên hệ quản trị viên để được hỗ trợ thêm.");
    }

    @FXML
    private void exitApplication(ActionEvent event) {
        Stage stage = (Stage) bookingTable.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}