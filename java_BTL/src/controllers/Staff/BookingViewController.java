package controllers.Staff;

import java.net.URL;
import java.time.LocalDateTime;
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
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import model.Booking;
import service.BookingService;
import controllers.SceneSwitcher;
import utils.Session;
import utils.RoleChecker; // Create this utility class if it doesn't exist
import model.Staff;
import enums.StatusEnum;

public class BookingViewController implements Initializable {

    @FXML
    private TableView<Booking> bookingTable;
    
    @FXML
    private TableColumn<Booking, Integer> idColumn;
    
    @FXML
    private TableColumn<Booking, String> customerColumn;
    
    @FXML
    private TableColumn<Booking, String> petColumn;
    
    @FXML
    private TableColumn<Booking, String> serviceColumn;
    
    @FXML
    private TableColumn<Booking, LocalDateTime> timeColumn;
    
    @FXML
    private TableColumn<Booking, String> statusColumn;
    
    @FXML
    private Button startButton;
    
    @FXML
    private Button completeButton;
    
    @FXML
    private Button printInvoiceButton;
    
    @FXML
    private Button detailsButton;
    
    @FXML
    private Button confirmArrivalButton;
    
    @FXML
    private Button viewNotesButton;
    
    @FXML
    private TextArea notesArea;
    
    private BookingService bookingService;
    private ObservableList<Booking> bookingList;
    private Booking selectedBooking;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Khởi tạo service
        bookingService = new BookingService();
        
        // Khởi tạo các cột cho bảng
        idColumn.setCellValueFactory(new PropertyValueFactory<>("bookingId"));
        customerColumn.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        petColumn.setCellValueFactory(new PropertyValueFactory<>("petName"));
        serviceColumn.setCellValueFactory(new PropertyValueFactory<>("serviceName"));
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("bookingTime"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        // Format ngày giờ
        timeColumn.setCellFactory(column -> {
            return new javafx.scene.control.TableCell<Booking, LocalDateTime>() {
                private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                
                @Override
                protected void updateItem(LocalDateTime item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(formatter.format(item));
                    }
                }
            };
        });
        
        // Tải dữ liệu booking
        loadBookings();
        
        // Xử lý sự kiện khi chọn một booking
        bookingTable.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> handleBookingSelection(newValue));
        
        // Kiểm tra quyền và hiển thị/ẩn các nút tương ứng
        setupButtonVisibility();
    }
    
    /**
     * Thiết lập hiển thị/ẩn các nút dựa trên quyền của người dùng
     */
    private void setupButtonVisibility() {
        boolean canMarkServiceDone = RoleChecker.hasPermission("MARK_SERVICE_DONE");
        boolean canCreateBooking = RoleChecker.hasPermission("CREATE_BOOKING");
        boolean canPrintReceipt = RoleChecker.hasPermission("PRINT_RECEIPT");
        boolean canManagePayment = RoleChecker.hasPermission("MANAGE_PAYMENT");
        
        startButton.setVisible(canMarkServiceDone || canCreateBooking);
        completeButton.setVisible(canMarkServiceDone);
        printInvoiceButton.setVisible(canPrintReceipt || canManagePayment);
        confirmArrivalButton.setVisible(canCreateBooking);
        
        // Chi tiết và ghi chú luôn hiển thị
        detailsButton.setVisible(true);
        viewNotesButton.setVisible(true);
    }
    
    /**
     * Tải danh sách booking được gán cho nhân viên hiện tại
     */
    private void loadBookings() {
        try {
            Staff currentStaff = Session.getInstance().getCurrentStaff();
            int staffId = currentStaff.getId(); // Changed from getStaffId() to getId()
            List<Booking> bookings;
            
            if (RoleChecker.hasPermission("VIEW_ALL_BOOKINGS")) {
                // If allowed to view all bookings
                bookings = bookingService.getAllBookings();
            } else {
                // If only allowed to view assigned bookings
                bookings = bookingService.getBookingsByStaffId(staffId);
            }
            
            bookingList = FXCollections.observableArrayList(bookings);
            bookingTable.setItems(bookingList);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải danh sách đặt lịch", e.getMessage());
        }
    }

    
    /**
     * Xử lý khi chọn một booking trong bảng
     */
    private void handleBookingSelection(Booking booking) {
        selectedBooking = booking;
        
        boolean hasSelection = (booking != null);
        boolean isPending = hasSelection && "PENDING".equals(booking.getStatus());
        boolean isConfirmed = hasSelection && "CONFIRMED".equals(booking.getStatus());
        boolean isStarted = hasSelection && "STARTED".equals(booking.getStatus());
        
        // Cập nhật trạng thái của các nút
        startButton.setDisable(!(isPending || isConfirmed));
        completeButton.setDisable(!(isStarted));
        printInvoiceButton.setDisable(!hasSelection);
        detailsButton.setDisable(!hasSelection);
        confirmArrivalButton.setDisable(!(isPending));
        viewNotesButton.setDisable(!hasSelection);
        
        // Xóa nội dung ghi chú
        notesArea.clear();
    }
    
    /**
     * Bắt đầu dịch vụ cho booking được chọn
     */
    @FXML
    private void startService(ActionEvent event) {
        if (selectedBooking == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Chưa chọn lịch đặt", 
                    "Vui lòng chọn một lịch đặt để bắt đầu dịch vụ.");
            return;
        }
        
        try {
            bookingService.updateBookingStatus(selectedBooking.getBookingId(), "STARTED");
            loadBookings(); // Tải lại danh sách
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã bắt đầu dịch vụ", 
                    "Dịch vụ đã được bắt đầu cho lịch đặt #" + selectedBooking.getBookingId());
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể bắt đầu dịch vụ", e.getMessage());
        }
    }
    
    /**
     * Hoàn thành dịch vụ cho booking được chọn
     */
    @FXML
    private void completeService(ActionEvent event) {
        if (selectedBooking == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Chưa chọn lịch đặt", 
                    "Vui lòng chọn một lịch đặt để hoàn thành dịch vụ.");
            return;
        }
        
        try {
            bookingService.updateBookingStatus(selectedBooking.getBookingId(), "COMPLETED");
            loadBookings(); // Tải lại danh sách
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã hoàn thành dịch vụ", 
                    "Dịch vụ đã được hoàn thành cho lịch đặt #" + selectedBooking.getBookingId());
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể hoàn thành dịch vụ", e.getMessage());
        }
    }
    
    /**
     * In hóa đơn cho booking được chọn
     */
    @FXML
    private void printInvoice(ActionEvent event) {
        if (selectedBooking == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Chưa chọn lịch đặt", 
                    "Vui lòng chọn một lịch đặt để in hóa đơn.");
            return;
        }
        
        try {
            // Chuyển sang màn hình xử lý hóa đơn
            Stage currentStage = (Stage) printInvoiceButton.getScene().getWindow();
            SceneSwitcher.switchToInvoiceScene(currentStage, selectedBooking.getBookingId());
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể mở màn hình hóa đơn", e.getMessage());
        }
    }
    
    /**
     * Xem chi tiết booking được chọn
     */
    @FXML
    private void viewDetails(ActionEvent event) {
        if (selectedBooking == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Chưa chọn lịch đặt", 
                    "Vui lòng chọn một lịch đặt để xem chi tiết.");
            return;
        }
        
        try {
            // Hiển thị chi tiết booking trong một cửa sổ mới
            Stage currentStage = (Stage) detailsButton.getScene().getWindow();
            SceneSwitcher.switchToBookingDetailScene(currentStage, selectedBooking.getBookingId());
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể mở chi tiết đặt lịch", e.getMessage());
        }
    }
    
    /**
     * Xác nhận khách hàng đã đến
     */
    @FXML
    private void confirmArrival(ActionEvent event) {
        if (selectedBooking == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Chưa chọn lịch đặt", 
                    "Vui lòng chọn một lịch đặt để xác nhận khách đến.");
            return;
        }
        
        try {
            bookingService.updateBookingStatus(selectedBooking.getBookingId(), "CONFIRMED");
            loadBookings(); // Tải lại danh sách
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã xác nhận khách đến", 
                    "Đã xác nhận khách đến cho lịch đặt #" + selectedBooking.getBookingId());
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể xác nhận khách đến", e.getMessage());
        }
    }
    
    /**
     * Xem ghi chú của booking được chọn
     */
    @FXML
    private void viewNotes(ActionEvent event) {
        if (selectedBooking == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Chưa chọn lịch đặt", 
                    "Vui lòng chọn một lịch đặt để xem ghi chú.");
            return;
        }
        
        // Hiển thị ghi chú trong TextArea
        String notes = selectedBooking.getNote();
        notesArea.setText(notes != null ? notes : "Không có ghi chú");
    }
    
    /**
     * Làm mới danh sách booking
     */
    @FXML
    private void refreshBookings(ActionEvent event) {
        loadBookings();
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