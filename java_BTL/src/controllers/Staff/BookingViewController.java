package controllers.Staff;

import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import controllers.SceneSwitcher;
import enums.StatusEnum;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Booking;
import model.BookingDetail;
import model.Customer;
import model.Pet;
import model.Service;
import model.Staff;
import repository.BookingDetailRepository;
import repository.BookingRepository;
import repository.ServiceRepository;
import service.BookingService;
import utils.DatabaseConnection;
import utils.RoleChecker;
import utils.Session;

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
    private BookingRepository bookingRepository;
    private BookingDetailRepository bookingDetailRepository;
    private ServiceRepository serviceRepository;
    private ObservableList<Booking> bookingList;
    private ObservableList<Booking> upcomingBookingList;
    private Booking selectedBooking;

    public BookingViewController() {
        this.bookingService = new BookingService();
        this.bookingRepository = BookingRepository.getInstance();
        this.bookingDetailRepository = new BookingDetailRepository();
        this.serviceRepository = ServiceRepository.getInstance();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Khởi tạo ngày hiện tại và thông tin nhân viên
        currentDateLabel.setText("Ngày hiện tại: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        Staff currentStaff = Session.getCurrentStaff();
        staffNameLabel.setText("Nhân viên: " + (currentStaff != null ? currentStaff.getFullName() : "N/A"));

        // Khởi tạo bảng lịch hẹn hôm nay
        initializeBookingTable();

        // Khởi tạo bảng lịch hẹn sắp tới
        initializeUpcomingBookingTable();

        // Khởi tạo bộ lọc trạng thái
        ObservableList<String> statusOptions = FXCollections.observableArrayList(
                "Tất cả", StatusEnum.PENDING.name(), StatusEnum.CONFIRMED.name(),
                StatusEnum.COMPLETED.name(), StatusEnum.CANCELLED.name());
        statusFilter.setItems(statusOptions);
        statusFilter.setValue("Tất cả");
        upcomingStatusFilter.setItems(statusOptions);
        upcomingStatusFilter.setValue("Tất cả");

        // Khởi tạo bộ chọn tháng và năm
        ObservableList<String> months = FXCollections.observableArrayList(
                "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12");
        monthSelector.setItems(months);
        monthSelector.setValue(String.valueOf(LocalDate.now().getMonthValue()));
        ObservableList<String> years = FXCollections.observableArrayList(
                "2023", "2024", "2025", "2026");
        yearSelector.setItems(years);
        yearSelector.setValue(String.valueOf(LocalDate.now().getYear()));

        // Thiết lập giá trị mặc định cho DatePicker
        datePicker.setValue(LocalDate.now());
        startDatePicker.setValue(LocalDate.now());
        endDatePicker.setValue(LocalDate.now().plusDays(7));

        // Tải dữ liệu ban đầu
        loadTodaySchedule();
        loadUpcomingBookings();
        loadStatistics();

        // Thiết lập lắng nghe sự kiện chọn hàng
        bookingTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, newValue) -> handleBookingSelection(newValue));
        upcomingBookingTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, newValue) -> handleBookingSelection(newValue));

        // Thiết lập hiển thị nút dựa trên vai trò
        setupButtonVisibility();
    }

    private void initializeBookingTable() {
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
                cellData.getValue().getStaff() != null ? cellData.getValue().getStaff().getFullName() : ""));
    }

    private void initializeUpcomingBookingTable() {
        upcomingIdColumn.setCellValueFactory(new PropertyValueFactory<>("bookingId"));
        upcomingDateColumn.setCellValueFactory(cellData -> Bindings.createObjectBinding(
                () -> cellData.getValue() != null ? cellData.getValue().getBookingTime().toLocalDate() : null));
        upcomingTimeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getBookingTime() != null ?
                cellData.getValue().getBookingTime().format(DateTimeFormatter.ofPattern("HH:mm")) : ""));
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
                cellData.getValue().getStaff() != null ? cellData.getValue().getStaff().getFullName() : ""));
    }

    private String getServiceNameFromBooking(Booking booking) {
        if (booking == null) return "";
        try {
            List<BookingDetail> details = getBookingDetails(booking.getBookingId());
            if (details != null && !details.isEmpty()) {
                return details.get(0).getService().getName();
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy tên dịch vụ: " + e.getMessage());
        }
        return "Không có thông tin";
    }
    
    private List<BookingDetail> getBookingDetails(int bookingId) {
        try {
            String condition = "booking_id = ?";
            return bookingDetailRepository.selectByCondition(condition, bookingId);
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy chi tiết booking: " + e.getMessage());
            return new ArrayList<>();
        }
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

    private void loadTodaySchedule() {
        try {
            LocalDate date = datePicker.getValue() != null ? datePicker.getValue() : LocalDate.now();
            Staff currentStaff = Session.getCurrentStaff();
            List<Booking> bookings;
            
            if (currentStaff == null || RoleChecker.hasPermission("VIEW_ALL_BOOKINGS")) {
                bookings = bookingService.getBookingsByDate(date);
            } else {
                bookings = bookingService.getBookingsByStaffAndDate(currentStaff.getId(), date);
            }
            
            bookingList = FXCollections.observableArrayList(bookings);
            applyFilters();
            statusMessageLabel.setText("Đã tải danh sách lịch hẹn hôm nay");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải danh sách đặt lịch", e.getMessage());
        }
    }

    @FXML
    private void loadTodaySchedule(ActionEvent event) {
        loadTodaySchedule();
    }

    @FXML
    private void searchBookings(ActionEvent event) {
        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            loadTodaySchedule();
            return;
        }
        try {
            List<Booking> bookings = bookingService.searchBookings(query);
            bookingList = FXCollections.observableArrayList(bookings);
            applyFilters();
            statusMessageLabel.setText("Đã tìm thấy " + bookings.size() + " kết quả");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tìm kiếm", e.getMessage());
        }
    }

    @FXML
    private void applyFilters(ActionEvent event) {
        applyFilters();
    }

    private void applyFilters() {
        if (bookingList == null) return;
        ObservableList<Booking> filteredList = FXCollections.observableArrayList(bookingList);
        String status = statusFilter.getValue();
        if (status != null && !status.equals("Tất cả")) {
            filteredList.removeIf(b -> !b.getStatus().name().equals(status));
        }
        bookingTable.setItems(filteredList);
    }

    @FXML
    private void handleNewBooking(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/staff/new_booking.fxml"));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setTitle("Đặt lịch mới");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            
            // Sau khi đóng cửa sổ đặt lịch, tải lại danh sách
            loadTodaySchedule();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể mở màn hình đặt lịch mới", e.getMessage());
        }
    }

    @FXML
    private void refreshBookings(ActionEvent event) {
        loadTodaySchedule();
        searchField.clear();
        statusFilter.setValue("Tất cả");
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
            boolean updated = bookingService.updateBookingStatus(selectedBooking.getBookingId(), StatusEnum.CONFIRMED);
            if (updated) {
                loadTodaySchedule();
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã xác nhận khách đến",
                        "Đã xác nhận khách đến cho lịch đặt #" + selectedBooking.getBookingId());
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể xác nhận",
                        "Không thể xác nhận khách đến cho lịch đặt #" + selectedBooking.getBookingId());
            }
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
        if (selectedBooking.getStatus() != StatusEnum.CONFIRMED) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Trạng thái không hợp lệ",
                    "Chỉ có thể bắt đầu dịch vụ cho lịch đặt đã xác nhận.");
            return;
        }
        try {
            // Khi bắt đầu dịch vụ, trạng thái vẫn là CONFIRMED nhưng cập nhật note
            selectedBooking.setNote("Đang thực hiện dịch vụ - " + LocalDateTime.now());
            boolean updated = bookingRepository.update(selectedBooking) > 0;
            if (updated) {
                loadTodaySchedule();
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã bắt đầu dịch vụ",
                        "Dịch vụ đã được bắt đầu cho lịch đặt #" + selectedBooking.getBookingId());
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể bắt đầu dịch vụ",
                        "Không thể bắt đầu dịch vụ cho lịch đặt #" + selectedBooking.getBookingId());
            }
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
        if (selectedBooking.getStatus() != StatusEnum.CONFIRMED) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Trạng thái không hợp lệ",
                    "Chỉ có thể hoàn thành dịch vụ cho lịch đặt đã xác nhận.");
            return;
        }
        try {
            boolean updated = bookingService.updateBookingStatus(selectedBooking.getBookingId(), StatusEnum.COMPLETED);
            if (updated) {
                loadTodaySchedule();
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã hoàn thành dịch vụ",
                        "Dịch vụ đã được hoàn thành cho lịch đặt #" + selectedBooking.getBookingId());
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể hoàn thành dịch vụ",
                        "Không thể hoàn thành dịch vụ cho lịch đặt #" + selectedBooking.getBookingId());
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể hoàn thành dịch vụ", e.getMessage());
        }
    }

    @FXML
    private void printInvoice(ActionEvent event) {
        if (selectedBooking == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Chưa chọn lịch đặt",
                    "Vui lòng chọn một lịch đặt để tạo hóa đơn.");
            return;
        }
        if (selectedBooking.getStatus() != StatusEnum.COMPLETED) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Trạng thái không hợp lệ",
                    "Chỉ có thể tạo hóa đơn cho lịch đặt đã hoàn thành.");
            return;
        }
        try {
            // Lưu bookingId vào Session để trang hóa đơn có thể truy cập
            Session.getInstance().setAttribute("selectedBookingId", selectedBooking.getBookingId());
            // Chuyển đến trang hóa đơn
            SceneSwitcher.switchScene("staff/invoice_view.fxml");
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
        
        // Hiển thị ghi chú của booking
        notesArea.setText(booking.getNote() != null ? booking.getNote() : "Không có ghi chú");
        
        // Kiểm tra trạng thái để bật/tắt các nút
        StatusEnum status = booking.getStatus();
        boolean isPending = status == StatusEnum.PENDING;
        boolean isConfirmed = status == StatusEnum.CONFIRMED;
        boolean isCompleted = status == StatusEnum.COMPLETED;
        
        confirmArrivalButton.setDisable(!isPending);
        startButton.setDisable(!(isPending || isConfirmed));
        completeButton.setDisable(!isConfirmed);
        printInvoiceButton.setDisable(!isCompleted);
    }

    @FXML
    private void viewDateRange(ActionEvent event) {
        loadUpcomingBookings();
    }

    private void loadUpcomingBookings() {
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
        try {
            Staff currentStaff = Session.getCurrentStaff();
            List<Booking> bookings;
            if (currentStaff == null || RoleChecker.hasPermission("VIEW_ALL_BOOKINGS")) {
                bookings = bookingService.getBookingsByDateRange(startDate, endDate);
            } else {
                bookings = bookingService.getBookingsByStaffAndDateRange(currentStaff.getId(), startDate, endDate);
            }
            upcomingBookingList = FXCollections.observableArrayList(bookings);
            applyUpcomingFilters();
            statusMessageLabel.setText("Đã tải danh sách lịch hẹn sắp tới");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải danh sách lịch hẹn", e.getMessage());
        }
    }

    @FXML
    private void applyUpcomingFilters(ActionEvent event) {
        applyUpcomingFilters();
    }

    private void applyUpcomingFilters() {
        if (upcomingBookingList == null) return;
        ObservableList<Booking> filteredList = FXCollections.observableArrayList(upcomingBookingList);
        String status = upcomingStatusFilter.getValue();
        if (status != null && !status.equals("Tất cả")) {
            filteredList.removeIf(b -> !b.getStatus().name().equals(status));
        }
        upcomingBookingTable.setItems(filteredList);
    }

    @FXML
    private void sendReminders(ActionEvent event) {
        try {
            ObservableList<Booking> selectedBookings = upcomingBookingTable.getSelectionModel().getSelectedItems();
            if (selectedBookings.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Chưa chọn lịch hẹn",
                        "Vui lòng chọn ít nhất một lịch hẹn để gửi nhắc nhở.");
                return;
            }
            
            int successCount = 0;
            for (Booking booking : selectedBookings) {
                if (booking.getCustomer() != null && booking.getCustomer().getEmail() != null) {
                    boolean emailSent = sendReminderEmail(booking);
                    if (emailSent) {
                        successCount++;
                        // Ghi chú về việc đã gửi nhắc nhở
                        booking.setNote((booking.getNote() != null ? booking.getNote() + "\n" : "") + 
                                "Đã gửi nhắc nhở qua email vào " + LocalDateTime.now());
                        bookingRepository.update(booking);
                    }
                }
            }
            
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã gửi nhắc nhở",
                    "Nhắc nhở đã được gửi cho " + successCount + " lịch hẹn.");
            statusMessageLabel.setText("Đã gửi nhắc nhở cho " + successCount + " lịch hẹn");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể gửi nhắc nhở", e.getMessage());
        }
    }
    
    private boolean sendReminderEmail(Booking booking) {
        // Trong thực tế, bạn sẽ sử dụng một dịch vụ email để gửi email
        // Đây chỉ là mã giả lập việc gửi email
        try {
            String customerEmail = booking.getCustomer().getEmail();
            String customerName = booking.getCustomer().getFullName();
            String bookingDate = booking.getBookingTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            String petName = booking.getPet() != null ? booking.getPet().getName() : "thú cưng của bạn";
            String serviceName = getServiceNameFromBooking(booking);
            
            System.out.println("Gửi email nhắc nhở cho " + customerEmail);
            System.out.println("Nội dung: Xin chào " + customerName + ", BestPets xin nhắc bạn về lịch hẹn của " + 
                    petName + " vào lúc " + bookingDate + " cho dịch vụ " + serviceName);
            
            // Trong thực tế, bạn sẽ gọi một dịch vụ email ở đây
            // EmailService.sendEmail(customerEmail, "Nhắc nhở lịch hẹn tại BestPets", emailContent);
            
            return true; // Giả định email đã được gửi thành công
        } catch (Exception e) {
            System.err.println("Lỗi khi gửi email: " + e.getMessage());
            return false;
        }
    }

    @FXML
    private void exportBookingList(ActionEvent event) {
        try {
            if (upcomingBookingList == null || upcomingBookingList.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Không có dữ liệu",
                        "Không có lịch hẹn nào để xuất danh sách.");
                return;
            }
            
            // Xuất danh sách lịch hẹn sang file CSV
            String fileName = "bookings_export_" + LocalDate.now().toString() + ".csv";
            try (FileWriter writer = new FileWriter(fileName)) {
                writer.write("Mã đặt lịch,Ngày,Giờ,Khách hàng,Số điện thoại,Thú cưng,Dịch vụ,Trạng thái,Nhân viên phụ trách\n");
                for (Booking booking : upcomingBookingList) {
                    String serviceName = getServiceNameFromBooking(booking);
                    String line = String.format("%d,%s,%s,%s,%s,%s,%s,%s,%s\n",
                            booking.getBookingId(),
                            booking.getBookingTime().toLocalDate(),
                            booking.getBookingTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                            booking.getCustomer() != null ? booking.getCustomer().getFullName() : "",
                            booking.getCustomer() != null ? booking.getCustomer().getPhone() : "",
                            booking.getPet() != null ? booking.getPet().getName() : "",
                            serviceName,
                            booking.getStatus() != null ? booking.getStatus().name() : "",
                            booking.getStaff() != null ? booking.getStaff().getFullName() : "");
                    writer.write(line);
                }
            }
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã xuất danh sách",
                    "Danh sách lịch hẹn đã được xuất sang " + fileName);
            statusMessageLabel.setText("Đã xuất danh sách lịch hẹn sang " + fileName);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể xuất danh sách",
                    "Lỗi khi ghi file: " + e.getMessage());
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể xuất danh sách", e.getMessage());
        }
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
            
            // Lấy dữ liệu thống kê từ database thực
            LocalDate startDate = LocalDate.of(Integer.parseInt(year), Integer.parseInt(month), 1);
            LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
            
            // Lấy tất cả booking trong tháng từ DB
            List<Booking> bookings = bookingService.getBookingsByDateRange(startDate, endDate);
            
            // Tổng số lịch hẹn
            int totalBookings = bookings.size();
            
            // Tổng số booking hoàn thành
            long completedCount = bookings.stream()
                    .filter(b -> StatusEnum.COMPLETED.equals(b.getStatus()))
                    .count();
            
            // Tỉ lệ hoàn thành
            double completionRate = totalBookings > 0 ? (completedCount * 100.0 / totalBookings) : 0;
            
            // Tính toán thống kê dịch vụ phổ biến nhất
            Map<String, Integer> serviceCount = new HashMap<>();
            for (Booking booking : bookings) {
                String serviceName = getServiceNameFromBooking(booking);
                serviceCount.put(serviceName, serviceCount.getOrDefault(serviceName, 0) + 1);
            }
            
            // Tìm dịch vụ phổ biến nhất
            String popularService = "Không có dữ liệu";
            double servicePercent = 0;
            
            if (!serviceCount.isEmpty()) {
                Map.Entry<String, Integer> mostPopular = serviceCount.entrySet().stream()
                        .max(Map.Entry.comparingByValue())
                        .orElse(null);
                
                if (mostPopular != null) {
                    popularService = mostPopular.getKey();
                    servicePercent = totalBookings > 0 ? 
                            (mostPopular.getValue() * 100.0 / totalBookings) : 0;
                }
            }
            
            // Tính số khách hàng thân thiết (đặt ≥ 3 lần/tháng)
            Map<Integer, Long> customerBookingCount = bookings.stream()
                    .filter(b -> b.getCustomer() != null)
                    .collect(Collectors.groupingBy(
                            b -> b.getCustomer().getId(), 
                            Collectors.counting()));
            
            int loyalCustomers = (int) customerBookingCount.entrySet().stream()
                    .filter(entry -> entry.getValue() >= 3)
                    .count();
            
            // Lấy dữ liệu tháng trước
            LocalDate prevMonthStart = startDate.minusMonths(1);
            LocalDate prevMonthEnd = prevMonthStart.withDayOfMonth(prevMonthStart.lengthOfMonth());
            List<Booking> prevMonthBookings = bookingService.getBookingsByDateRange(prevMonthStart, prevMonthEnd);
            
            // Tổng số booking tháng trước
            int prevTotalBookings = prevMonthBookings.size();
            
            // Tỉ lệ hoàn thành tháng trước
            long prevCompletedCount = prevMonthBookings.stream()
                    .filter(b -> StatusEnum.COMPLETED.equals(b.getStatus()))
                    .count();
            double prevCompletionRate = prevTotalBookings > 0 ? 
                    (prevCompletedCount * 100.0 / prevTotalBookings) : 0;
            
            // Tính xu hướng so với tháng trước
            double bookingTrend = prevTotalBookings > 0 ? 
                    ((totalBookings - prevTotalBookings) * 100.0 / prevTotalBookings) : 0;
            double completionTrend = prevCompletionRate > 0 ? 
                    (completionRate - prevCompletionRate) : 0;
            
            // Cập nhật giao diện
            totalBookingsLabel.setText(String.valueOf(totalBookings));
            completionRateLabel.setText(String.format("%.1f%%", completionRate));
            popularServiceLabel.setText(popularService);
            servicePercentLabel.setText(String.format("%.1f%% tổng số lịch hẹn", servicePercent));
            loyalCustomerLabel.setText(String.valueOf(loyalCustomers) + " khách hàng");
            
            // Biểu thị xu hướng (tăng/giảm)
            bookingTrendLabel.setText(String.format("%.1f%% %s", 
                    Math.abs(bookingTrend), bookingTrend >= 0 ? "↑" : "↓"));
            completionTrendLabel.setText(String.format("%.1f%% %s", 
                    Math.abs(completionTrend), completionTrend >= 0 ? "↑" : "↓"));
            
            // Thiết lập màu sắc cho xu hướng
            bookingTrendLabel.setStyle(bookingTrend >= 0 ? 
                    "-fx-text-fill: #4CAF50; -fx-font-weight: bold;" : 
                    "-fx-text-fill: #F44336; -fx-font-weight: bold;");
            completionTrendLabel.setStyle(completionTrend >= 0 ? 
                    "-fx-text-fill: #4CAF50; -fx-font-weight: bold;" : 
                    "-fx-text-fill: #F44336; -fx-font-weight: bold;");
            
            statusMessageLabel.setText("Đã tải thống kê tháng " + month + "/" + year);
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
            
            // Xuất báo cáo thống kê sang file CSV
            String fileName = "statistics_report_" + year + "_" + month + ".csv";
            try (FileWriter writer = new FileWriter(fileName)) {
                writer.write("Thống kê, Giá trị\n");
                writer.write("Tổng số lịch hẹn," + totalBookingsLabel.getText() + "\n");
                writer.write("Tỉ lệ hoàn thành," + completionRateLabel.getText() + "\n");
                writer.write("Dịch vụ phổ biến nhất," + popularServiceLabel.getText() + "\n");
                writer.write("Tỷ lệ dịch vụ phổ biến," + servicePercentLabel.getText() + "\n");
                writer.write("Khách hàng thân thiết," + loyalCustomerLabel.getText() + "\n");
                writer.write("So với tháng trước (lịch hẹn)," + bookingTrendLabel.getText() + "\n");
                writer.write("So với tháng trước (tỉ lệ hoàn thành)," + completionTrendLabel.getText() + "\n");
            }
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã xuất báo cáo",
                    "Báo cáo thống kê đã được xuất sang " + fileName);
            statusMessageLabel.setText("Đã xuất báo cáo thống kê sang " + fileName);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể xuất báo cáo",
                    "Lỗi khi ghi file: " + e.getMessage());
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể xuất báo cáo", e.getMessage());
        }
    }

    @FXML
    private void showHelp(ActionEvent event) {
        Alert helpAlert = new Alert(AlertType.INFORMATION);
        helpAlert.setTitle("Trợ giúp");
        helpAlert.setHeaderText("Hướng dẫn sử dụng màn hình Quản lý đặt lịch");
        
        String helpContent = 
                "1. Xem lịch hẹn: Chọn ngày cần xem và nhấn 'Xem' hoặc 'Hôm nay' để xem lịch hẹn hôm nay.\n\n" +
                "2. Tìm kiếm: Nhập tên khách hàng hoặc số điện thoại vào ô tìm kiếm và nhấn 'Tìm kiếm'.\n\n" +
                "3. Đặt lịch mới: Nhấn 'Đặt lịch mới' để tạo lịch hẹn mới.\n\n" +
                "4. Xác nhận khách đến: Chọn lịch hẹn và nhấn 'Xác nhận đến' khi khách hàng đến.\n\n" +
                "5. Quản lý trạng thái: Sử dụng các nút 'Bắt đầu dịch vụ' và 'Hoàn thành' để cập nhật tiến độ.\n\n" +
                "6. Tạo hóa đơn: Sau khi hoàn thành dịch vụ, nhấn 'Tạo hóa đơn' để chuyển sang màn hình thanh toán.\n\n" +
                "7. Xem thống kê: Chuyển sang tab 'Thống kê' để xem báo cáo hoạt động theo tháng.\n\n" +
                "Để được hỗ trợ thêm, vui lòng liên hệ quản trị viên.";
        
        TextArea textArea = new TextArea(helpContent);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefHeight(300);
        textArea.setPrefWidth(500);
        
        helpAlert.getDialogPane().setContent(textArea);
        helpAlert.showAndWait();
    }

    @FXML
    private void exitApplication(ActionEvent event) {
        // Hiển thị hộp thoại xác nhận trước khi thoát
        Alert confirmExit = new Alert(Alert.AlertType.CONFIRMATION);
        confirmExit.setTitle("Xác nhận");
        confirmExit.setHeaderText("Thoát ứng dụng");
        confirmExit.setContentText("Bạn có chắc chắn muốn thoát không?");
        
        Optional<ButtonType> result = confirmExit.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Quay lại màn hình chính
            SceneSwitcher.switchScene("dashboard.fxml");
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}