package controllers.Staff;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

import enums.PaymentMethodEnum;
import enums.StatusEnum;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import model.Invoice;
import model.Order;
import service.InvoiceService;
import utils.Session;
import utils.RoleChecker;
import controllers.InvoiceDetailController;

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
    
    @FXML
    private TextField searchField;
    
    @FXML
    private ComboBox<String> statusFilter;
    
    @FXML
    private ComboBox<String> paymentMethodFilter;
    
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
        
        // Thiết lập các giá trị cho ComboBox
        setupComboBoxes();
        
        // Tải dữ liệu hóa đơn
        loadInvoices();
        
        // Xử lý sự kiện khi chọn một hóa đơn
        invoiceTable.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> handleInvoiceSelection(newValue));
        
        // Thiết lập hành động tìm kiếm cho TextField
        setupSearchField();
        
        // Kiểm tra quyền và hiển thị/ẩn các nút tương ứng
        setupButtonVisibility();
    }
    
    /**
     * Khởi tạo các cột cho bảng
     */
    private void initializeTableColumns() {
        // Cột ID hóa đơn
        idColumn.setCellValueFactory(cellData -> 
            new SimpleIntegerProperty(cellData.getValue().getInvoiceId()).asObject()
        );
        
        // Cột ID đơn hàng
        orderIdColumn.setCellValueFactory(cellData -> {
            Invoice invoice = cellData.getValue();
            return new SimpleIntegerProperty(
                invoice.getOrder() != null ? invoice.getOrder().getOrderId() : 0
            ).asObject();
        });
        
        // Cột ngày
        dateColumn.setCellValueFactory(cellData -> {
            Invoice invoice = cellData.getValue();
            // Chuyển đổi Timestamp sang LocalDateTime
            return new SimpleObjectProperty<>(
                invoice.getPaymentDate() != null 
                    ? invoice.getPaymentDate().toLocalDateTime() 
                    : null
            );
        });
        
        // Cột tổng tiền
        totalColumn.setCellValueFactory(cellData -> 
            new SimpleObjectProperty<>(
                cellData.getValue().getTotal() != null 
                    ? cellData.getValue().getTotal().doubleValue() 
                    : 0.0
            )
        );
        
        // Cột phương thức thanh toán
        paymentMethodColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(
                cellData.getValue().getPaymentMethod() != null 
                    ? cellData.getValue().getPaymentMethod().name() 
                    : "N/A"
            )
        );
        
        // Cột trạng thái
        statusColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(
                cellData.getValue().getStatus() != null 
                    ? cellData.getValue().getStatus().name() 
                    : "N/A"
            )
        );
        
        // Format ngày giờ
        dateColumn.setCellFactory(column -> new FormattedTableCell<>(
            item -> item != null 
                ? item.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) 
                : "N/A"
        ));
        
        // Format số tiền
        totalColumn.setCellFactory(column -> new FormattedTableCell<>(
            item -> item != null 
                ? String.format("%,.0f VND", item) 
                : "0 VND"
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
     * Thiết lập các giá trị cho ComboBox
     */
    private void setupComboBoxes() {
        // Status filter
        ObservableList<String> statusList = FXCollections.observableArrayList();
        statusList.add("Tất cả");
        for (StatusEnum status : StatusEnum.values()) {
            statusList.add(status.name());
        }
        statusFilter.setItems(statusList);
        statusFilter.setValue("Tất cả");
        
        // Payment method filter
        ObservableList<String> paymentMethodList = FXCollections.observableArrayList();
        paymentMethodList.add("Tất cả");
        for (PaymentMethodEnum method : PaymentMethodEnum.values()) {
            paymentMethodList.add(method.name());
        }
        paymentMethodFilter.setItems(paymentMethodList);
        paymentMethodFilter.setValue("Tất cả");
        
        // Add listeners for filter changes
        statusFilter.setOnAction(event -> applyFilters());
        paymentMethodFilter.setOnAction(event -> applyFilters());
    }
    
    /**
     * Thiết lập hành động tìm kiếm cho TextField
     */
    private void setupSearchField() {
        searchField.setOnAction(event -> searchInvoices());
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
            
            // Apply current filters
            applyFilters();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải danh sách hóa đơn", e.getMessage());
        }
    }
    
    /**
     * Áp dụng các bộ lọc cho danh sách hóa đơn
     */
    private void applyFilters() {
        if (invoiceList == null) return;
        
        ObservableList<Invoice> filteredList = FXCollections.observableArrayList(invoiceList);
        
        // Áp dụng bộ lọc trạng thái
        String selectedStatus = statusFilter.getValue();
        if (selectedStatus != null && !selectedStatus.equals("Tất cả")) {
            filteredList.removeIf(invoice -> 
                invoice.getStatus() == null || 
                !invoice.getStatus().name().equals(selectedStatus)
            );
        }
        
        // Áp dụng bộ lọc phương thức thanh toán
        String selectedMethod = paymentMethodFilter.getValue();
        if (selectedMethod != null && !selectedMethod.equals("Tất cả")) {
            filteredList.removeIf(invoice -> 
                invoice.getPaymentMethod() == null || 
                !invoice.getPaymentMethod().name().equals(selectedMethod)
            );
        }
        
        // Áp dụng tìm kiếm văn bản nếu có
        String searchText = searchField.getText();
        if (searchText != null && !searchText.trim().isEmpty()) {
            String lowerCaseSearch = searchText.toLowerCase();
            filteredList.removeIf(invoice -> {
                // Tìm theo ID hóa đơn
                if (String.valueOf(invoice.getInvoiceId()).contains(lowerCaseSearch)) {
                    return false;
                }
                
                // Tìm theo ID đơn hàng
                if (invoice.getOrder() != null && 
                    String.valueOf(invoice.getOrder().getOrderId()).contains(lowerCaseSearch)) {
                    return false;
                }
                
                // Tìm theo tên khách hàng (nếu có thông tin khách hàng)
                if (invoice.getOrder() != null && invoice.getOrder().getCustomer() != null && 
                    invoice.getOrder().getCustomer().getFullName().toLowerCase().contains(lowerCaseSearch)) {
                    return false;
                }
                
                return true;
            });
        }
        
        // Cập nhật bảng với danh sách đã lọc
        invoiceTable.setItems(filteredList);
    }

    /**
     * Xử lý khi chọn một hóa đơn trong bảng
     */
    private void handleInvoiceSelection(Invoice invoice) {
        selectedInvoice = invoice;
        
        boolean hasSelection = (invoice != null);
        boolean isCompleted = hasSelection && 
            (invoice.getStatus() != null && invoice.getStatus().name().equals("COMPLETED"));
        
        // Cập nhật trạng thái của các nút
        viewDetailsButton.setDisable(!hasSelection);
        reprintButton.setDisable(!(hasSelection && isCompleted));
        sendEmailButton.setDisable(!hasSelection);
    }
    
    /**
     * Tìm kiếm hóa đơn theo văn bản đã nhập
     */
    @FXML
    private void searchInvoices() {
        applyFilters();
    }
    
    /**
     * Xem chi tiết hóa đơn được chọn
     */
    @FXML
    private void viewDetails(ActionEvent event) {
        if (selectedInvoice == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Chưa chọn hóa đơn", 
                    "Vui lòng chọn một hóa đơn để xem chi tiết.");
            return;
        }
        
        try {
            // Tạo FXMLLoader để tải màn hình chi tiết hóa đơn
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/invoice/invoice_detail.fxml"));
            Parent root = loader.load();
            
            // Lấy controller của màn hình chi tiết hóa đơn
            InvoiceDetailController detailController = loader.getController();
            // Gọi phương thức để thiết lập dữ liệu hóa đơn
            detailController.setInvoice(selectedInvoice);
            
            // Tạo scene mới và hiển thị
            Scene scene = new Scene(root);
            Stage stage = (Stage) viewDetailsButton.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể mở chi tiết hóa đơn", e.getMessage());
        }
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
        
        if (selectedInvoice.getStatus() == null || 
            !selectedInvoice.getStatus().name().equals("COMPLETED")) {
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
        if (selectedInvoice == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Chưa chọn hóa đơn", 
                    "Vui lòng chọn một hóa đơn để gửi email.");
            return;
        }
        
        try {
            invoiceService.sendInvoiceByEmail(selectedInvoice.getInvoiceId());
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã gửi email", 
                "Hóa đơn #" + selectedInvoice.getInvoiceId() + " đã được gửi đến email khách hàng.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể gửi email", e.getMessage());
        }
    }
    
    /**
     * Reset lại bộ lọc và hiển thị tất cả hóa đơn gần đây
     */
    @FXML
    private void resetFilter(ActionEvent event) {
        LocalDateTime now = LocalDateTime.now();
        fromDatePicker.setValue(now.toLocalDate().minusDays(30));
        toDatePicker.setValue(now.toLocalDate());
        
        searchField.clear();
        statusFilter.setValue("Tất cả");
        paymentMethodFilter.setValue("Tất cả");
        
        loadInvoices();
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
}