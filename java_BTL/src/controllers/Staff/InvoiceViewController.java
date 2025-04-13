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
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import model.Invoice;
import service.InvoiceService;
import utils.Session;
import utils.RoleChecker; // Create this utility class if it doesn't exist
import java.time.LocalDate;
import controllers.SceneSwitcher; // Create this utility class if it doesn't exist


public class InvoiceViewController implements Initializable {

    @FXML
    private TableView<Invoice> invoiceTable;
    
    @FXML
    private TableColumn<Invoice, Integer> idColumn;
    
    @FXML
    private TableColumn<Invoice, Integer> orderIdColumn;
    
    @FXML
    private TableColumn<Invoice, LocalDateTime> dateColumn;
    
    @FXML
    private TableColumn<Invoice, Double> totalColumn;
    
    @FXML
    private TableColumn<Invoice, String> paymentMethodColumn;
    
    @FXML
    private TableColumn<Invoice, String> statusColumn;
    
    @FXML
    private Button viewDetailsButton;
    
    @FXML
    private Button reprintButton;
    
    @FXML
    private Button sendEmailButton;
    
    @FXML
    private DatePicker fromDatePicker;
    
    @FXML
    private DatePicker toDatePicker;
    
    @FXML
    private Button searchButton;
    
    private final InvoiceService invoiceService;
    private ObservableList<Invoice> invoiceList;
    private Invoice selectedInvoice;
    
    public InvoiceViewController() {
        this.invoiceService = new InvoiceService();
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Khởi tạo các cột cho bảng
        initializeTableColumns();
        
        // Thiết lập giá trị mặc định cho DatePicker
        setupDatePickers();
        
        // Tải dữ liệu hóa đơn
        loadInvoices();
        
        // Xử lý sự kiện khi chọn một hóa đơn
        invoiceTable.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> handleInvoiceSelection(newValue));
        
        // Kiểm tra quyền và hiển thị/ẩn các nút tương ứng
        setupButtonVisibility();
    }
    
    /**
     * Khởi tạo các cột cho bảng
     */
    private void initializeTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("invoiceId"));
        orderIdColumn.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("paymentDate"));
        totalColumn.setCellValueFactory(new PropertyValueFactory<>("total"));
        paymentMethodColumn.setCellValueFactory(new PropertyValueFactory<>("paymentMethod"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        // Format ngày giờ
        dateColumn.setCellFactory(column -> new FormattedTableCell<>(
            item -> item.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
        ));
        
        // Format số tiền
        totalColumn.setCellFactory(column -> new FormattedTableCell<>(
            item -> String.format("%,.0f VND", item)
        ));
    }
    
    /**
     * Thiết lập giá trị mặc định cho DatePicker
     */
    private void setupDatePickers() {
        LocalDateTime now = LocalDateTime.now();
        fromDatePicker.setValue(now.toLocalDate().minusDays(30));
        toDatePicker.setValue(now.toLocalDate());
    }
    
    /**
     * Thiết lập hiển thị/ẩn các nút dựa trên quyền của người dùng
     */
    private void setupButtonVisibility() {
        boolean canViewInvoice = RoleChecker.hasPermission("VIEW_INVOICE");
        boolean canPrintReceipt = RoleChecker.hasPermission("PRINT_RECEIPT");
        boolean canManagePayment = RoleChecker.hasPermission("MANAGE_PAYMENT");
        
        viewDetailsButton.setVisible(canViewInvoice);
        reprintButton.setVisible(canPrintReceipt);
        sendEmailButton.setVisible(canManagePayment || canViewInvoice);
    }
    
    /**
     * Tải danh sách hóa đơn trong khoảng thời gian
     */
    private void loadInvoices() {
        try {
            List<Invoice> invoices;
            
            if (fromDatePicker.getValue() != null && toDatePicker.getValue() != null) {
                LocalDateTime startDate = fromDatePicker.getValue().atStartOfDay();
                LocalDateTime endDate = toDatePicker.getValue().plusDays(1).atStartOfDay();
                
                invoices = invoiceService.getInvoicesByDateRange(startDate, endDate);
            } else {
                // Default to recent invoices
                invoices = invoiceService.getRecentInvoices(30);
            }
            
            invoiceList = FXCollections.observableArrayList(invoices);
            invoiceTable.setItems(invoiceList);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải danh sách hóa đơn", e.getMessage());
        }
    }

    
    /**
     * Lấy danh sách hóa đơn
     */
    private List<Invoice> fetchInvoices() {
        if (fromDatePicker.getValue() != null && toDatePicker.getValue() != null) {
            return invoiceService.getInvoicesByDateRange(
                fromDatePicker.getValue().atStartOfDay(), 
                toDatePicker.getValue().plusDays(1).atStartOfDay()
            );
        } else {
            // Mặc định lấy hóa đơn trong 30 ngày gần nhất
            return invoiceService.getRecentInvoices(30);
        }
    }
    
    /**
     * Xử lý khi chọn một hóa đơn trong bảng
     */
    private void handleInvoiceSelection(Invoice invoice) {
        selectedInvoice = invoice;
        
        boolean hasSelection = (invoice != null);
        boolean isCompleted = hasSelection && "COMPLETED".equals(invoice.getStatus());
        
        // Cập nhật trạng thái của các nút
        viewDetailsButton.setDisable(!hasSelection);
        reprintButton.setDisable(!(hasSelection && isCompleted));
        sendEmailButton.setDisable(!hasSelection);
    }
    
    /**
     * Xem chi tiết hóa đơn được chọn
     */
    @FXML
    private void viewDetails(ActionEvent event) {
        validateAndExecuteInvoiceAction(
            () -> SceneSwitcher.switchToInvoiceDetailScene(
                (Stage) viewDetailsButton.getScene().getWindow(), 
                selectedInvoice.getInvoiceId()
            ), 
            "Vui lòng chọn một hóa đơn để xem chi tiết.", 
            "Không thể mở chi tiết hóa đơn"
        );
    }
    
    /**
     * In lại hóa đơn đã chọn
     */
    @FXML
    private void reprintInvoice(ActionEvent event) {
        if (selectedInvoice == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Chưa chọn hóa đơn", 
                    "Vui lòng chọn một hóa đơn để in lại.");
            return;
        }
        
        if (!"COMPLETED".equals(selectedInvoice.getStatus().name())) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Không thể in", 
                    "Chỉ có thể in lại các hóa đơn đã hoàn thành.");
            return;
        }
        
        try {
            invoiceService.printInvoice(selectedInvoice.getInvoiceId());
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã gửi lệnh in", 
                    "Hóa đơn #" + selectedInvoice.getInvoiceId() + " đã được gửi đến máy in.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể in hóa đơn", e.getMessage());
        }
    }
    
    /**
     * Gửi email hóa đơn cho khách hàng
     */
    @FXML
    private void sendEmail(ActionEvent event) {
        validateAndExecuteInvoiceAction(
            () -> {
                invoiceService.sendInvoiceByEmail(selectedInvoice.getInvoiceId());
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã gửi email", 
                    "Hóa đơn #" + selectedInvoice.getInvoiceId() + " đã được gửi đến email khách hàng.");
            }, 
            "Vui lòng chọn một hóa đơn để gửi email.", 
            "Không thể gửi email"
        );
    }
    
    /**
     * Tìm kiếm hóa đơn theo khoảng thời gian
     */
    @FXML
    private void searchInvoices(ActionEvent event) {
        if (fromDatePicker.getValue() == null || toDatePicker.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Thiếu thông tin", 
                    "Vui lòng chọn đầy đủ ngày bắt đầu và kết thúc.");
            return;
        }
        
        if (fromDatePicker.getValue().isAfter(toDatePicker.getValue())) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Thời gian không hợp lệ", 
                    "Ngày bắt đầu phải trước hoặc cùng ngày kết thúc.");
            return;
        }
        
        loadInvoices();
    }
    
    /**
     * Reset lại bộ lọc và hiển thị tất cả hóa đơn gần đây
     */
    @FXML
    private void resetFilter(ActionEvent event) {
        LocalDateTime now = LocalDateTime.now();
        fromDatePicker.setValue(now.toLocalDate().minusDays(30));
        toDatePicker.setValue(now.toLocalDate());
        loadInvoices();
    }
    
    /**
     * Xác thực và thực thi hành động với hóa đơn
     */
    private void validateAndExecuteInvoiceAction(
        Runnable action, 
        String noSelectionMessage, 
        String errorTitle
    ) {
        validateAndExecuteInvoiceAction(
            action, 
            noSelectionMessage, 
            errorTitle, 
            () -> false, 
            null
        );
    }
    
    /**
     * Xác thực và thực thi hành động với hóa đơn (có điều kiện)
     */
    private void validateAndExecuteInvoiceAction(
        Runnable action, 
        String noSelectionMessage, 
        String errorTitle, 
        Invoker<Boolean> additionalCondition, 
        String additionalConditionMessage
    ) {
        if (selectedInvoice == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Chưa chọn hóa đơn", noSelectionMessage);
            return;
        }
        
        try {
            if (additionalCondition != null && additionalCondition.invoke()) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Không thể thực hiện", additionalConditionMessage);
                return;
            }
            
            action.run();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", errorTitle, e.getMessage());
        }
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
    
    /**
     * Lớp hỗ trợ định dạng ô bảng
     */
    private static class FormattedTableCell<S, T> extends javafx.scene.control.TableCell<S, T> {
        private final java.util.function.Function<T, String> formatter;
        
        public FormattedTableCell(java.util.function.Function<T, String> formatter) {
            this.formatter = formatter;
        }
        
        @Override
        protected void updateItem(T item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
            } else {
                setText(formatter.apply(item));
            }
        }
    }
    
    /**
     * Giao diện hỗ trợ cho việc thêm điều kiện động
     */
    @FunctionalInterface
    private interface Invoker<T> {
        T invoke();
    }
}