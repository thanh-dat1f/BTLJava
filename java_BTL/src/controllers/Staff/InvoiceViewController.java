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
import javafx.scene.control.Label;
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
    private TableColumn<Invoice, String> customerColumn;
    
    @FXML
    private Button viewDetailsButton;
    
    @FXML
    private Button reprintButton;
    
    @FXML
    private Button sendEmailButton;
    
    @FXML
    private Button processPaymentButton;
    
    @FXML
    private Button applyDiscountButton;
    
    @FXML
    private Button refundButton;
    
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
    
    @FXML
    private Label totalInvoicesLabel;
    
    @FXML
    private Label totalRevenueLabel;
    
    private final InvoiceService invoiceService;
    private ObservableList<Invoice> invoiceList;
    private Invoice selectedInvoice;
    
    public InvoiceViewController() {
        this.invoiceService = new InvoiceService();
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize table columns
        initializeTableColumns();
        
        // Set default values for DatePicker
        setupDatePickers();
        
        // Set up ComboBoxes
        setupComboBoxes();
        
        // Load invoice data
        loadInvoices();
        
        // Set up selection listener for invoice table
        invoiceTable.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> handleInvoiceSelection(newValue));
        
        // Set up search field action
        setupSearchField();
        
        // Check permissions and hide/show buttons
        setupButtonVisibility();
        
        // Update summary labels
        updateSummaryLabels();
    }
    
    /**
     * Initialize table columns
     */
    private void initializeTableColumns() {
        // ID column
        idColumn.setCellValueFactory(cellData -> 
            new SimpleIntegerProperty(cellData.getValue().getInvoiceId()).asObject()
        );
        
        // Order ID column
        orderIdColumn.setCellValueFactory(cellData -> {
            Invoice invoice = cellData.getValue();
            return new SimpleIntegerProperty(
                invoice.getOrder() != null ? invoice.getOrder().getOrderId() : 0
            ).asObject();
        });
        
        // Customer column
        customerColumn.setCellValueFactory(cellData -> {
            Invoice invoice = cellData.getValue();
            if (invoice.getOrder() != null && invoice.getOrder().getCustomer() != null) {
                return new SimpleStringProperty(invoice.getOrder().getCustomer().getFullName());
            }
            return new SimpleStringProperty("N/A");
        });
        
        // Date column
        dateColumn.setCellValueFactory(cellData -> {
            Invoice invoice = cellData.getValue();
            // Convert Timestamp to LocalDateTime
            return new SimpleObjectProperty<>(
                invoice.getPaymentDate() != null 
                    ? invoice.getPaymentDate().toLocalDateTime() 
                    : null
            );
        });
        
        // Total column
        totalColumn.setCellValueFactory(cellData -> 
            new SimpleObjectProperty<>(
                cellData.getValue().getTotal() != null 
                    ? cellData.getValue().getTotal().doubleValue() 
                    : 0.0
            )
        );
        
        // Payment method column
        paymentMethodColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(
                cellData.getValue().getPaymentMethod() != null 
                    ? cellData.getValue().getPaymentMethod().name() 
                    : "N/A"
            )
        );
        
        // Status column
        statusColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(
                cellData.getValue().getStatus() != null 
                    ? cellData.getValue().getStatus().name() 
                    : "N/A"
            )
        );
        
        // Format date/time
        dateColumn.setCellFactory(column -> new FormattedTableCell<>(
            item -> item != null 
                ? item.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) 
                : "N/A"
        ));
        
        // Format amount
        totalColumn.setCellFactory(column -> new FormattedTableCell<>(
            item -> item != null 
                ? String.format("%,.0f VND", item) 
                : "0 VND"
        ));
    }
    
    /**
     * Set up date pickers with default values
     */
    private void setupDatePickers() {
        LocalDateTime now = LocalDateTime.now();
        fromDatePicker.setValue(now.toLocalDate().minusDays(30));
        toDatePicker.setValue(now.toLocalDate());
    }
    
    /**
     * Set up combo boxes
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
     * Set up search field
     */
    private void setupSearchField() {
        searchField.setOnAction(event -> searchInvoices());
    }
    
    /**
     * Set button visibility based on permissions
     */
    private void setupButtonVisibility() {
        boolean canViewInvoice = RoleChecker.hasPermission("VIEW_INVOICE");
        boolean canPrintReceipt = RoleChecker.hasPermission("PRINT_RECEIPT");
        boolean canManagePayment = RoleChecker.hasPermission("MANAGE_PAYMENT");
        
        viewDetailsButton.setVisible(canViewInvoice);
        reprintButton.setVisible(canPrintReceipt);
        sendEmailButton.setVisible(canManagePayment || canViewInvoice);
        processPaymentButton.setVisible(canManagePayment);
        applyDiscountButton.setVisible(canManagePayment);
        refundButton.setVisible(canManagePayment);
    }
    
    /**
     * Load invoices within date range
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
            
            // Update summary labels
            updateSummaryLabels();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải danh sách hóa đơn", e.getMessage());
        }
    }
    
    /**
     * Update summary labels (total invoices and revenue)
     */
    private void updateSummaryLabels() {
        if (invoiceTable.getItems() != null) {
            int total = invoiceTable.getItems().size();
            totalInvoicesLabel.setText(String.valueOf(total));
            
            double totalRevenue = 0;
            for (Invoice invoice : invoiceTable.getItems()) {
                if (invoice.getTotal() != null && StatusEnum.COMPLETED.equals(invoice.getStatus())) {
                    totalRevenue += invoice.getTotal().doubleValue();
                }
            }
            totalRevenueLabel.setText(String.format("%,.0f VND", totalRevenue));
        }
    }
    
    /**
     * Apply filters to invoice list
     */
    private void applyFilters() {
        if (invoiceList == null) return;
        
        ObservableList<Invoice> filteredList = FXCollections.observableArrayList(invoiceList);
        
        // Apply status filter
        String selectedStatus = statusFilter.getValue();
        if (selectedStatus != null && !selectedStatus.equals("Tất cả")) {
            filteredList.removeIf(invoice -> 
                invoice.getStatus() == null || 
                !invoice.getStatus().name().equals(selectedStatus)
            );
        }
        
        // Apply payment method filter
        String selectedMethod = paymentMethodFilter.getValue();
        if (selectedMethod != null && !selectedMethod.equals("Tất cả")) {
            filteredList.removeIf(invoice -> 
                invoice.getPaymentMethod() == null || 
                !invoice.getPaymentMethod().name().equals(selectedMethod)
            );
        }
        
        // Apply text search if provided
        String searchText = searchField.getText();
        if (searchText != null && !searchText.trim().isEmpty()) {
            String lowerCaseSearch = searchText.toLowerCase();
            filteredList.removeIf(invoice -> {
                // Search by invoice ID
                if (String.valueOf(invoice.getInvoiceId()).contains(lowerCaseSearch)) {
                    return false;
                }
                
                // Search by order ID
                if (invoice.getOrder() != null && 
                    String.valueOf(invoice.getOrder().getOrderId()).contains(lowerCaseSearch)) {
                    return false;
                }
                
                // Search by customer name
                if (invoice.getOrder() != null && invoice.getOrder().getCustomer() != null && 
                    invoice.getOrder().getCustomer().getFullName().toLowerCase().contains(lowerCaseSearch)) {
                    return false;
                }
                
                return true;
            });
        }
        
        // Update table with filtered list
        invoiceTable.setItems(filteredList);
        
        // Update summary labels after filtering
        updateSummaryLabels();
    }

    /**
     * Handle invoice selection
     */
    private void handleInvoiceSelection(Invoice invoice) {
        selectedInvoice = invoice;
        
        boolean hasSelection = (invoice != null);
        boolean isCompleted = hasSelection && 
            (invoice.getStatus() != null && invoice.getStatus().equals(StatusEnum.COMPLETED));
        boolean isPending = hasSelection && 
            (invoice.getStatus() != null && invoice.getStatus().equals(StatusEnum.PENDING));
        
        // Update button states
        viewDetailsButton.setDisable(!hasSelection);
        reprintButton.setDisable(!(hasSelection && isCompleted));
        sendEmailButton.setDisable(!(hasSelection && isCompleted));
        processPaymentButton.setDisable(!(hasSelection && isPending));
        applyDiscountButton.setDisable(!(hasSelection && isPending));
        refundButton.setDisable(!(hasSelection && isCompleted));
    }
    
    /**
     * Search invoices by text
     */
    @FXML
    private void searchInvoices() {
        applyFilters();
    }
    
    /**
     * Create a new invoice
     */
    @FXML
    private void createNewInvoice() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/invoice/create_invoice.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) invoiceTable.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể mở màn hình tạo hóa đơn", e.getMessage());
        }
    }
    
    /**
     * View invoice details
     */
    @FXML
    private void viewDetails(ActionEvent event) {
        if (selectedInvoice == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Chưa chọn hóa đơn", 
                    "Vui lòng chọn một hóa đơn để xem chi tiết.");
            return;
        }
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/invoice/invoice_detail.fxml"));
            Parent root = loader.load();
            
            InvoiceDetailController detailController = loader.getController();
            detailController.setInvoice(selectedInvoice);
            
            Scene scene = new Scene(root);
            Stage stage = (Stage) viewDetailsButton.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể mở chi tiết hóa đơn", e.getMessage());
        }
    }
    
    /**
     * Process payment for the selected invoice
     */
    @FXML
    private void processPayment(ActionEvent event) {
        if (selectedInvoice == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Chưa chọn hóa đơn", 
                    "Vui lòng chọn một hóa đơn để thanh toán.");
            return;
        }
        
        if (selectedInvoice.getStatus() != StatusEnum.PENDING) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Không thể thanh toán", 
                    "Chỉ có thể thanh toán các hóa đơn đang chờ.");
            return;
        }
        
        try {
            viewDetails(event); // Navigate to invoice detail screen for payment
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể xử lý thanh toán", e.getMessage());
        }
    }
    
    /**
     * Apply discount to the selected invoice
     */
    @FXML
    private void applyDiscount(ActionEvent event) {
        if (selectedInvoice == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Chưa chọn hóa đơn", 
                    "Vui lòng chọn một hóa đơn để áp dụng khuyến mãi.");
            return;
        }
        
        if (selectedInvoice.getStatus() != StatusEnum.PENDING) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Không thể áp dụng khuyến mãi", 
                    "Chỉ có thể áp dụng khuyến mãi cho các hóa đơn đang chờ.");
            return;
        }
        
        try {
            // Navigate to promotion screen or show dialog
            showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Chức năng đang phát triển", 
                    "Chức năng áp dụng khuyến mãi đang được phát triển.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể áp dụng khuyến mãi", e.getMessage());
        }
    }
    
    /**
     * Process refund for the selected invoice
     */
    @FXML
    private void processRefund(ActionEvent event) {
        if (selectedInvoice == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Chưa chọn hóa đơn", 
                    "Vui lòng chọn một hóa đơn để hoàn tiền.");
            return;
        }
        
        if (selectedInvoice.getStatus() != StatusEnum.COMPLETED) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Không thể hoàn tiền", 
                    "Chỉ có thể hoàn tiền cho các hóa đơn đã hoàn thành.");
            return;
        }
        
        try {
            // Show refund confirmation dialog
            showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Chức năng đang phát triển", 
                    "Chức năng hoàn tiền đang được phát triển.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể xử lý hoàn tiền", e.getMessage());
        }
    }
    
    /**
     * Reprint invoice
     */
    @FXML
    private void reprintInvoice(ActionEvent event) {
        if (selectedInvoice == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Chưa chọn hóa đơn", 
                    "Vui lòng chọn một hóa đơn để in lại.");
            return;
        }
        
        if (selectedInvoice.getStatus() != StatusEnum.COMPLETED) {
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
     * Send email
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
     * Reset filters
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
     * Show alert dialog
     */
    private void showAlert(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    /**
     * Table cell formatter helper class
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