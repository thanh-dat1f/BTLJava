package controllers.Staff;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

import controllers.SceneSwitcher;
import enums.PaymentMethodEnum;
import enums.StatusEnum;
import javafx.beans.property.SimpleDoubleProperty;
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
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Customer;
import model.Invoice;
import model.Order;
import model.OrderDetail;
import model.Service;
import model.Staff;
import repository.CustomerRepository;
import repository.InvoiceRepository;
import repository.OrderDetailRepository;
import repository.OrderRepository;
import repository.ServiceRepository;
import service.InvoiceService;
import utils.DatabaseConnection;
import utils.RoleChecker;
import utils.Session;

public class InvoiceViewController implements Initializable {

    @FXML private Label dateTimeLabel;
    @FXML private Label staffNameLabel;
    @FXML private DatePicker fromDatePicker;
    @FXML private DatePicker toDatePicker;
    @FXML private Button searchButton;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilter;
    @FXML private ComboBox<String> paymentMethodFilter;
    @FXML private TableView<Invoice> invoiceTable;
    @FXML private TableColumn<Invoice, Integer> idColumn;
    @FXML private TableColumn<Invoice, Integer> orderIdColumn;
    @FXML private TableColumn<Invoice, String> customerColumn;
    @FXML private TableColumn<Invoice, String> phoneColumn;
    @FXML private TableColumn<Invoice, LocalDateTime> dateColumn;
    @FXML private TableColumn<Invoice, String> serviceColumn;
    @FXML private TableColumn<Invoice, Double> totalColumn;
    @FXML private TableColumn<Invoice, String> paymentMethodColumn;
    @FXML private TableColumn<Invoice, String> statusColumn;
    @FXML private Label totalInvoicesLabel;
    @FXML private Label paidInvoicesLabel;
    @FXML private Label pendingInvoicesLabel;
    @FXML private Label totalRevenueLabel;
    @FXML private Button viewDetailsButton;
    @FXML private Button processPaymentButton;
    @FXML private Button applyDiscountButton;
    @FXML private Button reprintButton;
    @FXML private Button sendEmailButton;
    @FXML private Button refundButton;
    @FXML private TextField customerSearchField;
    @FXML private TextField customerIdField;
    @FXML private TextField customerNameField;
    @FXML private TextField customerPhoneField;
    @FXML private TextField customerEmailField;
    @FXML private Label customerPointsLabel;
    @FXML private CheckBox usePointsCheckbox;
    @FXML private Label invoiceIdLabel;
    @FXML private Label invoiceDateLabel;
    @FXML private Label cashierNameLabel;
    @FXML private Label subtotalLabel;
    @FXML private TextField discountField;
    @FXML private Label discountAmountLabel;
    @FXML private TextField promotionCodeField;
    @FXML private TextField pointsUsedField;
    @FXML private Label pointsValueLabel;
    @FXML private Label totalAmountLabel;
    @FXML private ComboBox<String> paymentMethodComboBox;
    @FXML private TextField amountPaidField;
    @FXML private Label changeAmountLabel;
    @FXML private TextArea invoiceNoteField;
    @FXML private ComboBox<String> serviceSelector;
    @FXML private TextField quantityField;
    @FXML private TableView<InvoiceItem> invoiceItemsTable;
    @FXML private ComboBox<String> reportTypeSelector;
    @FXML private DatePicker reportFromDatePicker;
    @FXML private DatePicker reportToDatePicker;
    @FXML private Label reportTotalRevenueLabel;
    @FXML private Label reportInvoiceCountLabel;
    @FXML private Label reportAverageValueLabel;
    @FXML private Label reportTopServiceLabel;
    @FXML private TableView<RevenueReport> revenueReportTable;
    @FXML private ProgressBar progressBar;
    @FXML private Label statusMessageLabel;

    // Repositories and Services
    private final InvoiceRepository invoiceRepository;
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final ServiceRepository serviceRepository;
    private final CustomerRepository customerRepository;
    private final InvoiceService invoiceService;
    
    // Collections
    private ObservableList<Invoice> invoiceList;
    private ObservableList<InvoiceItem> invoiceItems;
    private ObservableList<RevenueReport> revenueReports;
    private Invoice selectedInvoice;
    
    // Initialize controller
    public InvoiceViewController() {
        this.invoiceRepository = InvoiceRepository.getInstance();
        this.orderRepository = OrderRepository.getInstance();
        this.orderDetailRepository = OrderDetailRepository.getInstance();
        this.serviceRepository = ServiceRepository.getInstance();
        this.customerRepository = CustomerRepository.getInstance();
        this.invoiceService = new InvoiceService();
        this.invoiceItems = FXCollections.observableArrayList();
        this.revenueReports = FXCollections.observableArrayList();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize date time and staff information
        updateDateTime();
        Staff currentStaff = Session.getCurrentStaff();
        staffNameLabel.setText("Thu ngân: " + (currentStaff != null ? currentStaff.getFullName() : "N/A"));
        cashierNameLabel.setText(currentStaff != null ? currentStaff.getFullName() : "N/A");

        // Initialize table columns
        initializeTableColumns();

        // Initialize invoice items table
        initializeInvoiceItemsTable();

        // Setup DatePickers
        setupDatePickers();

        // Setup ComboBoxes
        setupComboBoxes();

        // Load invoices
        loadInvoices();

        // Initialize invoice creation tab
        initializeInvoiceCreation();

        // Setup invoice selection listener
        invoiceTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> handleInvoiceSelection(newValue));

        // Setup search field
        setupSearchField();

        // Setup button visibility based on permissions
        setupButtonVisibility();

        // Update summary labels
        updateSummaryLabels();
        
        // Timer to update time
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                javafx.application.Platform.runLater(() -> updateDateTime());
            }
        }, 0, 60000); // Update every minute
        
        // Setup event listeners for input fields
        setupEventListeners();
    }

    private void updateDateTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm");
        dateTimeLabel.setText(LocalDateTime.now().format(formatter));
    }

    private void initializeTableColumns() {
        // Invoice table columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("invoiceId"));
        orderIdColumn.setCellValueFactory(cellData -> {
            Invoice invoice = cellData.getValue();
            return new SimpleIntegerProperty(invoice.getOrder() != null ? invoice.getOrder().getOrderId() : 0).asObject();
        });
        customerColumn.setCellValueFactory(cellData -> {
            Invoice invoice = cellData.getValue();
            return new SimpleStringProperty(
                    invoice.getOrder() != null && invoice.getOrder().getCustomer() != null
                            ? invoice.getOrder().getCustomer().getFullName() : "N/A");
        });
        phoneColumn.setCellValueFactory(cellData -> {
            Invoice invoice = cellData.getValue();
            return new SimpleStringProperty(
                    invoice.getOrder() != null && invoice.getOrder().getCustomer() != null
                            ? invoice.getOrder().getCustomer().getPhone() : "N/A");
        });
        dateColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(
                cellData.getValue().getPaymentDate() != null ? 
                cellData.getValue().getPaymentDate().toLocalDateTime() : null));
        dateColumn.setCellFactory(column -> new TableCell<Invoice, LocalDateTime>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "N/A" : formatter.format(item));
            }
        });
        serviceColumn.setCellValueFactory(cellData -> {
            try {
                Invoice invoice = cellData.getValue();
                if (invoice == null || invoice.getOrder() == null) {
                    return new SimpleStringProperty("N/A");
                }
                
                // Get order details from database
                List<OrderDetail> details = orderDetailRepository.selectByCondition("order_id = ?", invoice.getOrder().getOrderId());
                if (details.isEmpty()) {
                    return new SimpleStringProperty("Không có dịch vụ");
                }
                
                // If multiple services, show first with "+X more" indicator
                String serviceName = details.get(0).getService().getName();
                if (details.size() > 1) {
                    serviceName += " + " + (details.size() - 1) + " dịch vụ khác";
                }
                return new SimpleStringProperty(serviceName);
            } catch (Exception e) {
                return new SimpleStringProperty("Lỗi: " + e.getMessage());
            }
        });
        totalColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(
                cellData.getValue().getTotal() != null ? cellData.getValue().getTotal().doubleValue() : 0).asObject());
        totalColumn.setCellFactory(column -> new TableCell<Invoice, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "0 VND" : String.format("%,.0f VND", item));
            }
        });
        paymentMethodColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getPaymentMethod() != null
                        ? cellData.getValue().getPaymentMethod().name() : "N/A"));
        statusColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getStatus() != null
                        ? cellData.getValue().getStatus().name() : "N/A"));

        // Revenue report table
        initializeRevenueReportTable();
    }

    private void initializeRevenueReportTable() {
        // Setup columns for revenue report table
        if (revenueReportTable.getColumns().size() >= 8) {
            revenueReportTable.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("date"));
            revenueReportTable.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("invoiceCount"));
            revenueReportTable.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("revenue"));
            revenueReportTable.getColumns().get(3).setCellValueFactory(new PropertyValueFactory<>("discount"));
            revenueReportTable.getColumns().get(4).setCellValueFactory(new PropertyValueFactory<>("promotion"));
            revenueReportTable.getColumns().get(5).setCellValueFactory(new PropertyValueFactory<>("points"));
            revenueReportTable.getColumns().get(6).setCellValueFactory(new PropertyValueFactory<>("netRevenue"));
            revenueReportTable.getColumns().get(7).setCellValueFactory(new PropertyValueFactory<>("trend"));
        }
    }

    private void initializeInvoiceItemsTable() {
        invoiceItemsTable.setItems(invoiceItems);
        // Configure columns
        if (invoiceItemsTable.getColumns().size() >= 6) {
            invoiceItemsTable.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("index"));
            invoiceItemsTable.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("serviceName"));
            invoiceItemsTable.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("quantity"));
            invoiceItemsTable.getColumns().get(3).setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
            invoiceItemsTable.getColumns().get(4).setCellValueFactory(new PropertyValueFactory<>("total"));
            
            // "Delete" column
            TableColumn<InvoiceItem, Void> deleteColumn = (TableColumn<InvoiceItem, Void>) invoiceItemsTable.getColumns().get(5);
            deleteColumn.setCellFactory(column -> new TableCell<InvoiceItem, Void>() {
                private final Button deleteButton = new Button("Xóa");
                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        deleteButton.setOnAction(event -> {
                            InvoiceItem invoiceItem = getTableView().getItems().get(getIndex());
                            invoiceItems.remove(invoiceItem);
                            updateInvoiceSummary();
                        });
                        setGraphic(deleteButton);
                    }
                }
            });
        }
    }

    private void setupDatePickers() {
        LocalDate now = LocalDate.now();
        fromDatePicker.setValue(now.minusDays(30));
        toDatePicker.setValue(now);
        reportFromDatePicker.setValue(now.minusDays(30));
        reportToDatePicker.setValue(now);
    }

    private void setupComboBoxes() {
        // Status filter
        ObservableList<String> statusList = FXCollections.observableArrayList("Tất cả");
        for (StatusEnum status : StatusEnum.values()) {
            statusList.add(status.name());
        }
        statusFilter.setItems(statusList);
        statusFilter.setValue("Tất cả");

        // Payment method filter
        ObservableList<String> paymentMethodList = FXCollections.observableArrayList("Tất cả");
        for (PaymentMethodEnum method : PaymentMethodEnum.values()) {
            paymentMethodList.add(method.name());
        }
        paymentMethodFilter.setItems(paymentMethodList);
        paymentMethodFilter.setValue("Tất cả");

        // Service selector - load from database
        loadServices();
        
        // Payment method combo box
        ObservableList<String> paymentMethods = FXCollections.observableArrayList();
        for (PaymentMethodEnum method : PaymentMethodEnum.values()) {
            paymentMethods.add(method.name());
        }
        paymentMethodComboBox.setItems(paymentMethods);
        paymentMethodComboBox.setValue(PaymentMethodEnum.CASH.name());

        // Report type selector
        reportTypeSelector.getItems().addAll("Doanh thu theo ngày", "Doanh thu theo dịch vụ", "Doanh thu theo phương thức thanh toán");
        reportTypeSelector.setValue("Doanh thu theo ngày");
    }
    
    private void loadServices() {
        try {
            // Load services from database
            List<Service> services = serviceRepository.selectAll();
            ObservableList<String> serviceNames = FXCollections.observableArrayList();
            
            for (Service service : services) {
                serviceNames.add(service.getName());
            }
            
            serviceSelector.setItems(serviceNames);
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Lỗi", "Không thể tải danh sách dịch vụ", e.getMessage());
        }
    }

    private void setupSearchField() {
        searchField.setOnAction(event -> searchInvoices());
        customerSearchField.setOnAction(event -> searchCustomer());
    }

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
    
    private void setupEventListeners() {
        // Set up listener for discount field to recalculate total on change
        discountField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                discountField.setText(newValue.replaceAll("[^\\d]", ""));
            } else {
                updateInvoiceSummary();
            }
        });
        
        // Set up listener for amount paid field to calculate change
        amountPaidField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                amountPaidField.setText(newValue.replaceAll("[^\\d]", ""));
            } else {
                updateChangeAmount();
            }
        });
        
        // Set up listener for use points checkbox
        usePointsCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            pointsUsedField.setDisable(!newValue);
            if (!newValue) {
                pointsUsedField.setText("0");
                pointsValueLabel.setText("0 VND");
            }
            updateInvoiceSummary();
        });
        
        // Set up listener for points used field
        pointsUsedField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                pointsUsedField.setText(newValue.replaceAll("[^\\d]", ""));
            } else {
                updatePointsValue();
                updateInvoiceSummary();
            }
        });
    }

    private void loadInvoices() {
        try {
            progressBar.setVisible(true);
            List<Invoice> invoices;
            
            if (fromDatePicker.getValue() != null && toDatePicker.getValue() != null) {
                LocalDateTime startDate = fromDatePicker.getValue().atStartOfDay();
                LocalDateTime endDate = toDatePicker.getValue().plusDays(1).atStartOfDay();
                invoices = invoiceService.getInvoicesByDateRange(startDate, endDate);
            } else {
                invoices = invoiceService.getRecentInvoices(30);
            }
            
            invoiceList = FXCollections.observableArrayList(invoices);
            invoiceTable.setItems(invoiceList);
            applyFilters();
            updateSummaryLabels();
            progressBar.setVisible(false);
            statusMessageLabel.setText("Đã tải " + invoices.size() + " hóa đơn");
        } catch (Exception e) {
            progressBar.setVisible(false);
            showAlert(AlertType.ERROR, "Lỗi", "Không thể tải danh sách hóa đơn", e.getMessage());
            statusMessageLabel.setText("Lỗi: " + e.getMessage());
        }
    }

    private void initializeInvoiceCreation() {
        // Check for bookingId from Session
        Object bookingIdObj = Session.getInstance().getAttribute("selectedBookingId");
        if (bookingIdObj instanceof Integer) {
            int bookingId = (Integer) bookingIdObj;
            
            try {
                // Get booking info from database
                String sql = "SELECT b.booking_id, b.booking_time, " +
                      "c.customer_id, c.point, p.person_id, p.full_name, p.phone, p.email, p.address, " +
                      "pet.pet_id, pet.name AS pet_name " +
                      "FROM booking b " +
                      "JOIN customer c ON b.customer_id = c.customer_id " +
                      "JOIN person p ON c.customer_id = p.person_id " +
                      "JOIN pet ON b.pet_id = pet.pet_id " +
                      "WHERE b.booking_id = ?"; 
                
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(sql)) {
                    
                    stmt.setInt(1, bookingId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            // Fill customer info
                            int customerId = rs.getInt("customer_id");
                            String customerName = rs.getString("full_name");
                            String customerPhone = rs.getString("phone");
                            String customerEmail = rs.getString("email");
                            int points = rs.getInt("point");
                            
                            customerIdField.setText("KH-" + String.format("%05d", customerId));
                            customerNameField.setText(customerName);
                            customerPhoneField.setText(customerPhone);
                            customerEmailField.setText(customerEmail);
                            customerPointsLabel.setText(String.valueOf(points));
                            
                            // Create new invoice
                            invoiceIdLabel.setText("HĐ-" + String.format("%05d", getNextInvoiceId()));
                            invoiceDateLabel.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                            
                            // Get booking services
                            loadBookingServices(bookingId);
                        }
                    }
                }
            } catch (SQLException e) {
                showAlert(AlertType.ERROR, "Lỗi", "Không thể tải thông tin booking", e.getMessage());
            }
        } else {
            // Initialize new empty invoice
            subtotalLabel.setText("0 VND");
            discountAmountLabel.setText("0 VND");
            totalAmountLabel.setText("0 VND");
            changeAmountLabel.setText("0 VND");
            invoiceIdLabel.setText("HĐ-" + String.format("%05d", getNextInvoiceId()));
            invoiceDateLabel.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        }
    }
    
    private int getNextInvoiceId() {
        try {
            String sql = "SELECT MAX(invoice_id) AS max_id FROM invoice";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                
                if (rs.next()) {
                    int maxId = rs.getInt("max_id");
                    return maxId + 1;
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy ID hóa đơn tiếp theo: " + e.getMessage());
        }
        return 1; // Default to 1 if no invoices exist
    }
    
    private void loadBookingServices(int bookingId) {
        try {
            String sql = "SELECT bd.*, s.name, s.price " +
                     "FROM booking_detail bd " +
                     "JOIN service s ON bd.service_id = s.service_id " +
                     "WHERE bd.booking_id = ?";
            
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setInt(1, bookingId);
                try (ResultSet rs = stmt.executeQuery()) {
                    invoiceItems.clear();
                    int index = 1;
                    
                    while (rs.next()) {
                        String serviceName = rs.getString("name");
                        int quantity = rs.getInt("quantity");
                        double price = rs.getDouble("price");
                        double total = quantity * price;
                        
                        invoiceItems.add(new InvoiceItem(index++, serviceName, quantity, price, total));
                    }
                    
                    updateInvoiceSummary();
                }
            }
            
        } catch (SQLException e) {
            showAlert(AlertType.ERROR, "Lỗi", "Không thể tải dịch vụ", e.getMessage());
        }
    }

    private void updateInvoiceSummary() {
        double subtotal = invoiceItems.stream().mapToDouble(InvoiceItem::getTotal).sum();
        double discount = 0;
        double pointsValue = 0;
        
        // Calculate discount if provided
        try {
            if (discountField.getText() != null && !discountField.getText().isEmpty()) {
                double discountPercent = Double.parseDouble(discountField.getText());
                if (discountPercent > 0 && discountPercent <= 100) {
                    discount = subtotal * discountPercent / 100;
                }
            }
        } catch (NumberFormatException e) {
            // Ignore if not a number
        }
        
        // Calculate points value if used
        if (usePointsCheckbox.isSelected() && pointsUsedField.getText() != null && !pointsUsedField.getText().isEmpty()) {
            try {
                int pointsUsed = Integer.parseInt(pointsUsedField.getText());
                // Assume 1 point = 1,000 VND
                pointsValue = pointsUsed * 1000;
                // Update points value display
                pointsValueLabel.setText(String.format("%,.0f VND", pointsValue));
            } catch (NumberFormatException e) {
                // Ignore if not a number
            }
        }
        
        double total = subtotal - discount - pointsValue;
        if (total < 0) total = 0; // Ensure total is not negative
        
        // Update UI
        subtotalLabel.setText(String.format("%,.0f VND", subtotal));
        discountAmountLabel.setText(String.format("%,.0f VND", discount));
        totalAmountLabel.setText(String.format("%,.0f VND", total));
        
        // Update change amount
        updateChangeAmount();
    }
    
    private void updateChangeAmount() {
        try {
            if (amountPaidField.getText() != null && !amountPaidField.getText().isEmpty()) {
                double amountPaid = Double.parseDouble(amountPaidField.getText());
                // Get total amount from label
                String totalText = totalAmountLabel.getText().replaceAll("[^\\d]", "");
                double total = Double.parseDouble(totalText);
                double change = amountPaid - total;
                changeAmountLabel.setText(String.format("%,.0f VND", Math.max(0, change)));
            }
        } catch (NumberFormatException e) {
            changeAmountLabel.setText("0 VND");
        }
    }
    
    private void updatePointsValue() {
        try {
            if (pointsUsedField.getText() != null && !pointsUsedField.getText().isEmpty()) {
                int pointsUsed = Integer.parseInt(pointsUsedField.getText());
                // Assume 1 point = 1,000 VND
                double pointsValue = pointsUsed * 1000;
                pointsValueLabel.setText(String.format("%,.0f VND", pointsValue));
                
                // Check if points used exceed available points
                try {
                    int availablePoints = Integer.parseInt(customerPointsLabel.getText());
                    if (pointsUsed > availablePoints) {
                        showAlert(AlertType.WARNING, "Cảnh báo", "Điểm sử dụng vượt quá điểm hiện có", 
                                "Khách hàng chỉ có " + availablePoints + " điểm.");
                        pointsUsedField.setText(String.valueOf(availablePoints));
                    }
                } catch (NumberFormatException e) {
                    // Ignore if points label is not a number
                }
            }
        } catch (NumberFormatException e) {
            pointsValueLabel.setText("0 VND");
        }
    }

    private void handleInvoiceSelection(Invoice invoice) {
        selectedInvoice = invoice;
        boolean hasSelectedInvoice = invoice != null;
        
        viewDetailsButton.setDisable(!hasSelectedInvoice);
        processPaymentButton.setDisable(!hasSelectedInvoice || 
                (hasSelectedInvoice && StatusEnum.PAID.equals(invoice.getStatus())));
        applyDiscountButton.setDisable(!hasSelectedInvoice);
        reprintButton.setDisable(!hasSelectedInvoice);
        sendEmailButton.setDisable(!hasSelectedInvoice);
        refundButton.setDisable(!hasSelectedInvoice || 
                (hasSelectedInvoice && !StatusEnum.PAID.equals(invoice.getStatus())));
        
        if (hasSelectedInvoice) {
            loadInvoiceDetails(invoice);
        }
    }
    
    private void loadInvoiceDetails(Invoice invoice) {
        try {
            // Clear previous items
            invoiceItems.clear();
            
            // Set invoice header details
            invoiceIdLabel.setText("HĐ-" + String.format("%05d", invoice.getInvoiceId()));
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            invoiceDateLabel.setText(invoice.getPaymentDate() != null ? 
                    invoice.getPaymentDate().toLocalDateTime().format(formatter) : "N/A");
            
            // Load cashier name
            Staff cashier = invoice.getStaff();
            cashierNameLabel.setText(cashier != null ? cashier.getFullName() : "N/A");
            
            // Load customer details
            Order order = invoice.getOrder();
            if (order != null && order.getCustomer() != null) {
                Customer customer = order.getCustomer();
                customerIdField.setText("KH-" + String.format("%05d", customer.getCustomerId()));
                customerNameField.setText(customer.getFullName());
                customerPhoneField.setText(customer.getPhone());
                customerEmailField.setText(customer.getEmail());
                customerPointsLabel.setText(String.valueOf(customer.getPoint()));
            } else {
                customerIdField.clear();
                customerNameField.clear();
                customerPhoneField.clear();
                customerEmailField.clear();
                customerPointsLabel.setText("0");
            }
            
            // Load invoice items
            if (order != null) {
                List<OrderDetail> details = orderDetailRepository.selectByCondition("order_id = ?", order.getOrderId());
                int index = 1;
                
                for (OrderDetail detail : details) {
                    Service service = detail.getService();
                    if (service != null) {
                        double unitPrice = service.getPrice() != null ? service.getPrice().doubleValue() : 0;
                        double total = unitPrice * detail.getQuantity();
                        
                        invoiceItems.add(new InvoiceItem(
                                index++,
                                service.getName(),
                                detail.getQuantity(),
                                unitPrice,
                                total
                        ));
                    }
                }
            }
            
            // Set payment details
            subtotalLabel.setText(String.format("%,.0f VND", invoice.getSubtotal() != null ? invoice.getSubtotal().doubleValue() : 0));
            discountField.setText(invoice.getDiscountPercent() != null ? invoice.getDiscountPercent().toString() : "0");
            discountAmountLabel.setText(String.format("%,.0f VND", invoice.getDiscountAmount() != null ? invoice.getDiscountAmount().doubleValue() : 0));
            
            // Points used
            int pointsUsed = invoice.getPointsUsed() != null ? invoice.getPointsUsed() : 0;
            pointsUsedField.setText(String.valueOf(pointsUsed));
            pointsValueLabel.setText(String.format("%,.0f VND", pointsUsed * 1000)); // Assuming 1 point = 1000 VND
            usePointsCheckbox.setSelected(pointsUsed > 0);
            
            // Promotion code
            promotionCodeField.setText(invoice.getPromotionCode());
            
            // Total and payment
            totalAmountLabel.setText(String.format("%,.0f VND", invoice.getTotal() != null ? invoice.getTotal().doubleValue() : 0));
            if (invoice.getPaymentMethod() != null) {
                paymentMethodComboBox.setValue(invoice.getPaymentMethod().name());
            }
            
            // Amount paid and change
            amountPaidField.setText(invoice.getAmountPaid() != null ? invoice.getAmountPaid().toString() : "0");
            double amountPaid = invoice.getAmountPaid() != null ? invoice.getAmountPaid().doubleValue() : 0;
            double total = invoice.getTotal() != null ? invoice.getTotal().doubleValue() : 0;
            changeAmountLabel.setText(String.format("%,.0f VND", Math.max(0, amountPaid - total)));
            
            // Notes
            invoiceNoteField.setText(invoice.getNote());
            
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Lỗi", "Không thể tải chi tiết hóa đơn", e.getMessage());
        }
    }    private void searchInvoices() {
        String searchQuery = searchField.getText().trim().toLowerCase();
        
        if (searchQuery.isEmpty()) {
            applyFilters(); // If search query is empty, just apply filters
            return;
        }
        
        // Filter by search query and other filters
        ObservableList<Invoice> filteredList = FXCollections.observableArrayList();
        
        for (Invoice invoice : invoiceList) {
            boolean matchesFilter = 
                // Match invoice ID
                String.valueOf(invoice.getInvoiceId()).contains(searchQuery) ||
                // Match order ID
                (invoice.getOrder() != null && String.valueOf(invoice.getOrder().getOrderId()).contains(searchQuery)) ||
                // Match customer name
                (invoice.getOrder() != null && invoice.getOrder().getCustomer() != null && 
                        invoice.getOrder().getCustomer().getFullName().toLowerCase().contains(searchQuery)) ||
                // Match customer phone
                (invoice.getOrder() != null && invoice.getOrder().getCustomer() != null && 
                        invoice.getOrder().getCustomer().getPhone().toLowerCase().contains(searchQuery));
            
            if (matchesFilter) {
                // Check status filter
                boolean statusMatch = statusFilter.getValue().equals("Tất cả") || 
                        (invoice.getStatus() != null && invoice.getStatus().name().equals(statusFilter.getValue()));
                
                // Check payment method filter
                boolean paymentMethodMatch = paymentMethodFilter.getValue().equals("Tất cả") || 
                        (invoice.getPaymentMethod() != null && invoice.getPaymentMethod().name().equals(paymentMethodFilter.getValue()));
                
                if (statusMatch && paymentMethodMatch) {
                    filteredList.add(invoice);
                }
            }
        }
        
        invoiceTable.setItems(filteredList);
        updateSummaryLabels(filteredList);
        statusMessageLabel.setText("Tìm thấy " + filteredList.size() + " kết quả");
    }
    
    private void applyFilters() {
        String statusValue = statusFilter.getValue();
        String paymentMethodValue = paymentMethodFilter.getValue();
        
        if (statusValue.equals("Tất cả") && paymentMethodValue.equals("Tất cả")) {
            invoiceTable.setItems(invoiceList);
            updateSummaryLabels();
            return;
        }
        
        ObservableList<Invoice> filteredList = FXCollections.observableArrayList();
        
        for (Invoice invoice : invoiceList) {
            boolean statusMatch = statusValue.equals("Tất cả") || 
                    (invoice.getStatus() != null && invoice.getStatus().name().equals(statusValue));
            
            boolean paymentMethodMatch = paymentMethodValue.equals("Tất cả") || 
                    (invoice.getPaymentMethod() != null && invoice.getPaymentMethod().name().equals(paymentMethodValue));
            
            if (statusMatch && paymentMethodMatch) {
                filteredList.add(invoice);
            }
        }
        
        invoiceTable.setItems(filteredList);
        updateSummaryLabels(filteredList);
    }
    
    private void updateSummaryLabels() {
        updateSummaryLabels(invoiceTable.getItems());
    }
    
    private void updateSummaryLabels(ObservableList<Invoice> invoices) {
        int totalCount = invoices.size();
        int paidCount = 0;
        int pendingCount = 0;
        double totalRevenue = 0;
        
        for (Invoice invoice : invoices) {
            if (StatusEnum.PAID.equals(invoice.getStatus())) {
                paidCount++;
                if (invoice.getTotal() != null) {
                    totalRevenue += invoice.getTotal().doubleValue();
                }
            } else if (StatusEnum.PENDING.equals(invoice.getStatus())) {
                pendingCount++;
            }
        }
        
        totalInvoicesLabel.setText(String.valueOf(totalCount));
        paidInvoicesLabel.setText(String.valueOf(paidCount));
        pendingInvoicesLabel.setText(String.valueOf(pendingCount));
        totalRevenueLabel.setText(String.format("%,.0f VND", totalRevenue));
    }
    
    private void searchCustomer() {
        String searchQuery = customerSearchField.getText().trim();
        
        if (searchQuery.isEmpty()) {
            showAlert(AlertType.WARNING, "Cảnh báo", "Tìm kiếm trống", "Vui lòng nhập số điện thoại để tìm kiếm khách hàng.");
            return;
        }
        
        try {
            Customer customer = customerRepository.findByPhone(searchQuery);
            
            if (customer != null) {
                customerIdField.setText("KH-" + String.format("%05d", customer.getId()));
                customerNameField.setText(customer.getFullName());
                customerPhoneField.setText(customer.getPhone());
                customerEmailField.setText(customer.getEmail());
                customerPointsLabel.setText(String.valueOf(customer.getPoint()));
            } else {
                showAlert(AlertType.INFORMATION, "Thông báo", "Không tìm thấy", 
                        "Không tìm thấy khách hàng với số điện thoại " + searchQuery);
            }
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Lỗi", "Không thể tìm kiếm khách hàng", e.getMessage());
        }
    }
    
    @FXML
    private void onAddServiceButtonClick() {
        if (serviceSelector.getValue() == null || serviceSelector.getValue().isEmpty()) {
            showAlert(AlertType.WARNING, "Cảnh báo", "Dịch vụ trống", "Vui lòng chọn một dịch vụ.");
            return;
        }
        
        int quantity = 1;
        try {
            if (quantityField.getText() != null && !quantityField.getText().isEmpty()) {
                quantity = Integer.parseInt(quantityField.getText());
                if (quantity <= 0) {
                    showAlert(AlertType.WARNING, "Cảnh báo", "Số lượng không hợp lệ", "Số lượng phải lớn hơn 0.");
                    return;
                }
            }
        } catch (NumberFormatException e) {
            showAlert(AlertType.WARNING, "Cảnh báo", "Số lượng không hợp lệ", "Vui lòng nhập một số hợp lệ.");
            return;
        }
        
        try {
            // Find the service
            Service service = serviceRepository.findByName(serviceSelector.getValue());
            
            if (service != null) {
                // Check if this service is already in the list
                Optional<InvoiceItem> existingItem = invoiceItems.stream()
                        .filter(item -> item.getServiceName().equals(service.getName()))
                        .findFirst();
                
                if (existingItem.isPresent()) {
                    // Update quantity of existing item
                    InvoiceItem item = existingItem.get();
                    int newQuantity = item.getQuantity() + quantity;
                    item.setQuantity(newQuantity);
                    item.setTotal(item.getUnitPrice() * newQuantity);
                    invoiceItemsTable.refresh();
                } else {
                    // Add new item
                    double unitPrice = service.getPrice() != null ? service.getPrice().doubleValue() : 0;
                    invoiceItems.add(new InvoiceItem(
                            invoiceItems.size() + 1,
                            service.getName(),
                            quantity,
                            unitPrice,
                            unitPrice * quantity
                    ));
                }
                
                // Reset input fields
                serviceSelector.setValue(null);
                quantityField.setText("1");
                
                // Update summary
                updateInvoiceSummary();
            } else {
                showAlert(AlertType.WARNING, "Cảnh báo", "Dịch vụ không tồn tại", 
                        "Không tìm thấy dịch vụ " + serviceSelector.getValue());
            }
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Lỗi", "Không thể thêm dịch vụ", e.getMessage());
        }
    }
    
    @FXML
    private void onSearchButtonClick() {
        if (fromDatePicker.getValue() == null || toDatePicker.getValue() == null) {
            showAlert(AlertType.WARNING, "Cảnh báo", "Ngày không hợp lệ", "Vui lòng chọn khoảng thời gian.");
            return;
        }
        
        loadInvoices();
    }
    
    @FXML
    private void viewDetails() {
        if (selectedInvoice == null) {
            showAlert(AlertType.WARNING, "Cảnh báo", "Không có hóa đơn được chọn", "Vui lòng chọn một hóa đơn để xem chi tiết.");
            return;
        }
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/staff/InvoiceDetailsView.fxml"));
            Parent root = loader.load();
            
            InvoiceDetailsController controller = loader.getController();
            controller.setInvoice(selectedInvoice);
            
            Stage stage = new Stage();
            stage.setTitle("Chi tiết hóa đơn");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            showAlert(AlertType.ERROR, "Lỗi", "Không thể mở form chi tiết", e.getMessage());
        }
    }
    
    @FXML
    private void processPayment() {
        if (selectedInvoice == null) {
            showAlert(AlertType.WARNING, "Cảnh báo", "Không có hóa đơn được chọn", "Vui lòng chọn một hóa đơn để xử lý thanh toán.");
            return;
        }
        
        if (StatusEnum.PAID.equals(selectedInvoice.getStatus())) {
            showAlert(AlertType.INFORMATION, "Thông báo", "Hóa đơn đã thanh toán", "Hóa đơn này đã được thanh toán rồi.");
            return;
        }
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/staff/PaymentProcessView.fxml"));
            Parent root = loader.load();
            
            PaymentProcessController controller = loader.getController();
            controller.setInvoice(selectedInvoice);
            
            Stage stage = new Stage();
            stage.setTitle("Xử lý thanh toán");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            
            // Refresh data after payment
            loadInvoices();
        } catch (IOException e) {
            showAlert(AlertType.ERROR, "Lỗi", "Không thể mở form thanh toán", e.getMessage());
        }
    }
    
    @FXML
    private void createNewInvoice() {
        // Reset form
        resetInvoiceForm();
        
        // Switch to create invoice tab
        TabPane tabPane = (TabPane) invoiceTable.getScene().lookup("#tabPane");
        if (tabPane != null) {
            tabPane.getSelectionModel().select(1); // Select create invoice tab
        }
    }
    
    private void resetInvoiceForm() {
        // Clear customer fields
        customerIdField.clear();
        customerNameField.clear();
        customerPhoneField.clear();
        customerEmailField.clear();
        customerPointsLabel.setText("0");
        
        // Clear invoice items
        invoiceItems.clear();
        
        // Reset amount fields
        subtotalLabel.setText("0 VND");
        discountField.setText("0");
        discountAmountLabel.setText("0 VND");
        pointsUsedField.setText("0");
        pointsValueLabel.setText("0 VND");
        usePointsCheckbox.setSelected(false);
        totalAmountLabel.setText("0 VND");
        amountPaidField.setText("0");
        changeAmountLabel.setText("0 VND");
        
        // Reset other fields
        invoiceNoteField.clear();
        promotionCodeField.clear();
        invoiceIdLabel.setText("HĐ-" + String.format("%05d", getNextInvoiceId()));
        invoiceDateLabel.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
    }
    
    @FXML
    private void processPaymentAndPrint() {
        processPayment();
        reprintInvoice();
    }
    
    @FXML
    private void applyDiscount() {
        if (selectedInvoice == null) {
            showAlert(AlertType.WARNING, "Cảnh báo", "Không có hóa đơn được chọn", "Vui lòng chọn một hóa đơn để áp dụng khuyến mãi.");
            return;
        }
        
        // Open discount dialog
        TextInputDialog dialog = new TextInputDialog("");
        dialog.setTitle("Áp dụng khuyến mãi");
        dialog.setHeaderText("Nhập phần trăm khuyến mãi");
        dialog.setContentText("Phần trăm giảm giá:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(percent -> {
            try {
                int discountPercent = Integer.parseInt(percent);
                if (discountPercent <= 0 || discountPercent > 100) {
                    showAlert(AlertType.WARNING, "Cảnh báo", "Giá trị không hợp lệ", 
                            "Phần trăm khuyến mãi phải nằm trong khoảng 1-100%.");
                    return;
                }
                
                // Apply discount to invoice
                applyDiscountToInvoice(selectedInvoice, discountPercent);
                
            } catch (NumberFormatException e) {
                showAlert(AlertType.WARNING, "Cảnh báo", "Giá trị không hợp lệ", 
                        "Vui lòng nhập một số nguyên cho phần trăm khuyến mãi.");
            }
        });
    }
    
    private void applyDiscountToInvoice(Invoice invoice, int discountPercent) {
        try {
            // Calculate new total with discount
            BigDecimal subtotal = invoice.getSubtotal() != null ? invoice.getSubtotal() : BigDecimal.ZERO;
            BigDecimal discountAmount = subtotal.multiply(BigDecimal.valueOf(discountPercent)).divide(BigDecimal.valueOf(100));
            BigDecimal newTotal = subtotal.subtract(discountAmount);
            
            // Update invoice
            invoice.setDiscountPercent(discountPercent);
            invoice.setDiscountAmount(discountAmount);
            invoice.setTotal(newTotal);
            
            // Save to database
            boolean success = invoiceRepository.update(invoice) > 0;
            if (success) {
                showAlert(AlertType.INFORMATION, "Thành công", "Đã áp dụng khuyến mãi", 
                        "Đã áp dụng khuyến mãi " + discountPercent + "% vào hóa đơn.");
                
                // Refresh data
                loadInvoices();
            } else {
                showAlert(AlertType.ERROR, "Lỗi", "Không thể áp dụng khuyến mãi", 
                        "Đã xảy ra lỗi khi cập nhật hóa đơn.");
            }
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Lỗi", "Không thể áp dụng khuyến mãi", e.getMessage());
        }
    }
    
    @FXML
    private void reprintInvoice() {
        if (selectedInvoice == null) {
            showAlert(AlertType.WARNING, "Cảnh báo", "Không có hóa đơn được chọn", "Vui lòng chọn một hóa đơn để in lại.");
            return;
        }
        
        try {
            // Generate PDF for printing
            String fileName = "invoice_" + selectedInvoice.getInvoiceId() + ".pdf";
            invoiceService.generateInvoicePDF(selectedInvoice.getOrder().getOrderId(), fileName);
            
            // Open PDF file
            File file = new File(fileName);
            if (file.exists()) {
                java.awt.Desktop.getDesktop().open(file);
                showAlert(AlertType.INFORMATION, "Thành công", "Đã mở file hóa đơn", 
                        "Hóa đơn đã được mở bằng ứng dụng mặc định.");
            } else {
                showAlert(AlertType.ERROR, "Lỗi", "Không thể mở file hóa đơn", 
                        "File không tồn tại: " + fileName);
            }
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Lỗi", "Không thể in hóa đơn", e.getMessage());
        }
    }
    
    @FXML
    private void sendEmail() {
        if (selectedInvoice == null) {
            showAlert(AlertType.WARNING, "Cảnh báo", "Không có hóa đơn được chọn", "Vui lòng chọn một hóa đơn để gửi email.");
            return;
        }
        
        try {
            // Check if customer has email
            Customer customer = selectedInvoice.getOrder().getCustomer();
            if (customer == null || customer.getEmail() == null || customer.getEmail().isEmpty()) {
                showAlert(AlertType.WARNING, "Cảnh báo", "Thiếu thông tin email", 
                        "Khách hàng không có địa chỉ email.");
                return;
            }
            
            // Send invoice by email
            invoiceService.sendInvoiceByEmail(selectedInvoice.getInvoiceId());
            
            showAlert(AlertType.INFORMATION, "Thành công", "Đã gửi hóa đơn", 
                    "Hóa đơn đã được gửi đến " + customer.getEmail());
            
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Lỗi", "Không thể gửi email", e.getMessage());
        }
    }
    
    @FXML
    private void processRefund() {
        if (selectedInvoice == null) {
            showAlert(AlertType.WARNING, "Cảnh báo", "Không có hóa đơn được chọn", "Vui lòng chọn một hóa đơn để hoàn tiền.");
            return;
        }
        
        if (selectedInvoice.getStatus() != StatusEnum.PAID) {
            showAlert(AlertType.WARNING, "Cảnh báo", "Hóa đơn chưa thanh toán", 
                    "Chỉ có thể hoàn tiền cho hóa đơn đã thanh toán.");
            return;
        }
        
        // Confirm refund
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận hoàn tiền");
        alert.setHeaderText("Bạn có chắc chắn muốn hoàn tiền?");
        alert.setContentText("Hoàn tiền cho hóa đơn #" + selectedInvoice.getInvoiceId() + 
                " với số tiền " + selectedInvoice.getTotal() + " VND?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Update invoice status to CANCELLED
                selectedInvoice.setStatus(StatusEnum.CANCELLED);
                boolean success = invoiceRepository.update(selectedInvoice) > 0;
                
                if (success) {
                    // Update order status as well
                    Order order = selectedInvoice.getOrder();
                    order.setStatus(StatusEnum.CANCELLED);
                    orderRepository.update(order);
                    
                    showAlert(AlertType.INFORMATION, "Thành công", "Đã hoàn tiền", 
                            "Hóa đơn đã được đánh dấu là đã hoàn tiền.");
                    
                    // Refresh data
                    loadInvoices();
                } else {
                    showAlert(AlertType.ERROR, "Lỗi", "Không thể hoàn tiền", 
                            "Đã xảy ra lỗi khi cập nhật trạng thái hóa đơn.");
                }
            } catch (Exception e) {
                showAlert(AlertType.ERROR, "Lỗi", "Không thể hoàn tiền", e.getMessage());
            }
        }
    }
    
    @FXML
    private void addNewCustomer() {
        // Open new customer form
        try {
            // This should be replaced with your actual form path
            Parent root = FXMLLoader.load(getClass().getResource("/views/customer/AddCustomerView.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Thêm khách hàng mới");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            showAlert(AlertType.ERROR, "Lỗi", "Không thể mở form thêm khách hàng", e.getMessage());
        }
    }
    
    @FXML
    private void applyPromotionCode() {
        String code = promotionCodeField.getText().trim();
        if (code.isEmpty()) {
            showAlert(AlertType.WARNING, "Cảnh báo", "Mã khuyến mãi trống", 
                    "Vui lòng nhập mã khuyến mãi.");
            return;
        }
        
        // In real application, validate promotion code here
        showAlert(AlertType.INFORMATION, "Thông báo", "Mã khuyến mãi", 
                "Tính năng áp dụng mã khuyến mãi đang phát triển.");
    }
    
    @FXML
    private void cancelInvoice() {
        // Reset form
        resetInvoiceForm();
    }
    
    @FXML
    private void viewReport() {
        if (reportFromDatePicker.getValue() == null || reportToDatePicker.getValue() == null) {
            showAlert(AlertType.WARNING, "Cảnh báo", "Ngày không hợp lệ", "Vui lòng chọn khoảng thời gian.");
            return;
        }
        
        String reportType = reportTypeSelector.getValue();
        LocalDate fromDate = reportFromDatePicker.getValue();
        LocalDate toDate = reportToDatePicker.getValue();
        
        try {
            switch (reportType) {
                case "Doanh thu theo ngày":
                    generateDailyRevenueReport(fromDate, toDate);
                    break;
                case "Doanh thu theo dịch vụ":
                    generateServiceRevenueReport(fromDate, toDate);
                    break;
                case "Doanh thu theo phương thức thanh toán":
                    generatePaymentMethodRevenueReport(fromDate, toDate);
                    break;
                default:
                    showAlert(AlertType.WARNING, "Cảnh báo", "Loại báo cáo không hợp lệ", "Vui lòng chọn loại báo cáo.");
            }
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Lỗi", "Không thể tạo báo cáo", e.getMessage());
        }
    }
    
    private void generateDailyRevenueReport(LocalDate fromDate, LocalDate toDate) throws SQLException {
        revenueReports.clear();
        
        // Get revenue by day
        String sql = "SELECT DATE(payment_date) as date, " +
               "COUNT(*) as invoice_count, " +
               "SUM(subtotal) as subtotal, " +
               "SUM(discount_amount) as discount, " +
               "SUM(points_used * 1000) as points_value, " +
               "SUM(total) as total " +
               "FROM invoice " +
               "WHERE payment_date BETWEEN ? AND ? AND status = 'PAID' " +
               "GROUP BY DATE(payment_date) " +
               "ORDER BY date";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setTimestamp(1, Timestamp.valueOf(fromDate.atStartOfDay()));
            stmt.setTimestamp(2, Timestamp.valueOf(toDate.plusDays(1).atStartOfDay()));
            
            double totalRevenue = 0;
            int totalInvoices = 0;
            double previousDayRevenue = 0;
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String date = rs.getDate("date").toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                    int invoiceCount = rs.getInt("invoice_count");
                    double subtotal = rs.getDouble("subtotal");
                    double discount = rs.getDouble("discount");
                    double pointsValue = rs.getDouble("points_value");
                    double netRevenue = rs.getDouble("total");
                    
                    // Calculate trend (% change from previous day)
                    String trend = "";
                    if (previousDayRevenue > 0) {
                        double percentChange = ((netRevenue - previousDayRevenue) / previousDayRevenue) * 100;
                        trend = String.format("%+.1f%%", percentChange);
                    }
                    
                    revenueReports.add(new RevenueReport(
                            date,
                            invoiceCount,
                            subtotal,
                            discount,
                            0, // No promotion in this example
                            pointsValue,
                            netRevenue,
                            trend
                    ));
                    
                    totalRevenue += netRevenue;
                    totalInvoices += invoiceCount;
                    previousDayRevenue = netRevenue;
                }
            }
            
            // Update summary labels
            reportTotalRevenueLabel.setText(String.format("%,.0f VND", totalRevenue));
            reportInvoiceCountLabel.setText(String.valueOf(totalInvoices));
            double averageValue = totalInvoices > 0 ? totalRevenue / totalInvoices : 0;
            reportAverageValueLabel.setText(String.format("%,.0f VND", averageValue));
            
            // Get top service
            String topServiceSql = "SELECT s.name, SUM(od.quantity) as total_quantity " +
                   "FROM invoice i " +
                   "JOIN `order` o ON i.order_id = o.order_id " +
                   "JOIN order_detail od ON o.order_id = od.order_id " +
                   "JOIN service s ON od.service_id = s.service_id " +
                   "WHERE i.payment_date BETWEEN ? AND ? AND i.status = 'PAID' " +
                   "GROUP BY s.service_id " +
                   "ORDER BY total_quantity DESC " +
                   "LIMIT 1";
            
            try (PreparedStatement stmtTop = conn.prepareStatement(topServiceSql)) {
                stmtTop.setTimestamp(1, Timestamp.valueOf(fromDate.atStartOfDay()));
                stmtTop.setTimestamp(2, Timestamp.valueOf(toDate.plusDays(1).atStartOfDay()));
                try (ResultSet rsTop = stmtTop.executeQuery()) {
                    if (rsTop.next()) {
                        String topServiceName = rsTop.getString("name");
                        reportTopServiceLabel.setText(topServiceName);
                    } else {
                        reportTopServiceLabel.setText("N/A");
                    }
                }
            }

            // Update table
            revenueReportTable.setItems(revenueReports);
            statusMessageLabel.setText("Đã tạo báo cáo doanh thu theo ngày thành công");
        } catch (SQLException e) {
            showAlert(AlertType.ERROR, "Lỗi", "Không thể tạo báo cáo doanh thu theo ngày", e.getMessage());
            statusMessageLabel.setText("Lỗi: " + e.getMessage());
        }
    }
    
    private void generateServiceRevenueReport(LocalDate fromDate, LocalDate toDate) {
        // Implementation for service revenue report
        showAlert(AlertType.INFORMATION, "Thông báo", "Đang phát triển", 
                "Tính năng báo cáo doanh thu theo dịch vụ đang được phát triển.");
    }
    
    private void generatePaymentMethodRevenueReport(LocalDate fromDate, LocalDate toDate) {
        // Implementation for payment method revenue report
        showAlert(AlertType.INFORMATION, "Thông báo", "Đang phát triển", 
                "Tính năng báo cáo doanh thu theo phương thức thanh toán đang được phát triển.");
    }
    
    @FXML
    private void exportToExcel() {
        // Export report to Excel
        if (revenueReports.isEmpty()) {
            showAlert(AlertType.WARNING, "Cảnh báo", "Không có dữ liệu", 
                    "Vui lòng tạo báo cáo trước khi xuất Excel.");
            return;
        }
        
        try {
            // Create file chooser dialog
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Lưu báo cáo Excel");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Excel Files", "*.xls", "*.xlsx"));
            fileChooser.setInitialFileName("revenue_report.xlsx");
            
            // Show save dialog
            File file = fileChooser.showSaveDialog(null);
            if (file != null) {
                // Export to Excel (implementation would depend on Excel library)
                // For demo purposes, just create a CSV file
                try (FileWriter writer = new FileWriter(file)) {
                    // Write header
                    writer.append("Ngày,Số hóa đơn,Doanh thu,Giảm giá,Khuyến mãi,Điểm tích lũy,Doanh thu thuần,Xu hướng\n");
                    
                    // Write data
                    for (RevenueReport report : revenueReports) {
                        writer.append(String.format("%s,%d,%,.0f,%,.0f,%,.0f,%,.0f,%,.0f,%s\n",
                                report.getDate(),
                                report.getInvoiceCount(),
                                report.getRevenue(),
                                report.getDiscount(),
                                report.getPromotion(),
                                report.getPoints(),
                                report.getNetRevenue(),
                                report.getTrend()
                        ));
                    }
                    
                    showAlert(AlertType.INFORMATION, "Thành công", "Đã xuất báo cáo", 
                            "Báo cáo đã được lưu thành công tại:\n" + file.getAbsolutePath());
                }
            }
        } catch (IOException e) {
            showAlert(AlertType.ERROR, "Lỗi", "Không thể xuất báo cáo", e.getMessage());
        }
    }
    
    @FXML
    private void printReport() {
        // Print report
        showAlert(AlertType.INFORMATION, "Thông báo", "Đang phát triển", 
                "Tính năng in báo cáo đang được phát triển.");
    }
    
    @FXML
    private void resetFilter() {
        fromDatePicker.setValue(LocalDate.now().minusDays(30));
        toDatePicker.setValue(LocalDate.now());
        statusFilter.setValue("Tất cả");
        paymentMethodFilter.setValue("Tất cả");
        searchField.clear();
        loadInvoices();
    }
    
    @FXML
    private void showHelp() {
        showAlert(AlertType.INFORMATION, "Trợ giúp", "Hướng dẫn sử dụng",
                "Liên hệ quản trị viên để được hỗ trợ thêm.");
    }
    
    @FXML
    private void exitApplication() {
        Stage stage = (Stage) invoiceTable.getScene().getWindow();
        stage.close();
    }
    
    private void showAlert(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    // Inner class for invoice items
    public static class InvoiceItem {
        private final int index;
        private final String serviceName;
        private int quantity;
        private final double unitPrice;
        private double total;
        
        public InvoiceItem(int index, String serviceName, int quantity, double unitPrice, double total) {
            this.index = index;
            this.serviceName = serviceName;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.total = total;
        }
        
        public int getIndex() { return index; }
        public String getServiceName() { return serviceName; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public double getUnitPrice() { return unitPrice; }
        public double getTotal() { return total; }
        public void setTotal(double total) { this.total = total; }
    }
    
    // Inner class for revenue report
    public static class RevenueReport {
        private final String date;
        private final int invoiceCount;
        private final double revenue;
        private final double discount;
        private final double promotion;
        private final double points;
        private final double netRevenue;
        private final String trend;
        
        public RevenueReport(String date, int invoiceCount, double revenue, double discount, 
                             double promotion, double points, double netRevenue, String trend) {
            this.date = date;
            this.invoiceCount = invoiceCount;
            this.revenue = revenue;
            this.discount = discount;
            this.promotion = promotion;
            this.points = points;
            this.netRevenue = netRevenue;
            this.trend = trend;
        }
        
        public String getDate() { return date; }
        public int getInvoiceCount() { return invoiceCount; }
        public double getRevenue() { return revenue; }
        public double getDiscount() { return discount; }
        public double getPromotion() { return promotion; }
        public double getPoints() { return points; }
        public double getNetRevenue() { return netRevenue; }
        public String getTrend() { return trend; }
    }
}