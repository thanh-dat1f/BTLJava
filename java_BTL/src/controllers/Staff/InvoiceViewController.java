
package controllers.Staff;

import java.io.File;
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
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import java.io.FileWriter;
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
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
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
    @FXML private Button applyDiscountButton;
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

    private final InvoiceRepository invoiceRepository;
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final ServiceRepository serviceRepository;
    private final CustomerRepository customerRepository;
    private final InvoiceService invoiceService;
    
    private ObservableList<Invoice> invoiceList;
    private ObservableList<InvoiceItem> invoiceItems;
    private ObservableList<RevenueReport> revenueReports;
    private Invoice selectedInvoice;
    
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
        updateDateTime();
        Staff currentStaff = Session.getCurrentStaff();
        staffNameLabel.setText("Thu ngân: " + (currentStaff != null ? currentStaff.getFullName() : "N/A"));
        cashierNameLabel.setText(currentStaff != null ? currentStaff.getFullName() : "N/A");

        initializeTableColumns();
        initializeInvoiceItemsTable();
        setupDatePickers();
        setupComboBoxes();
        loadInvoices();
        initializeInvoiceCreation();
        invoiceTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> handleInvoiceSelection(newValue));
        setupSearchField();
        setupButtonVisibility();
        updateSummaryLabels();
        
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                javafx.application.Platform.runLater(() -> updateDateTime());
            }
        }, 0, 60000);
        
        setupEventListeners();
    }

    private void updateDateTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm");
        dateTimeLabel.setText(LocalDateTime.now().format(formatter));
    }

    private void initializeTableColumns() {
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
                
                List<OrderDetail> details = orderDetailRepository.selectByCondition("order_id = ?", invoice.getOrder().getOrderId());
                if (details.isEmpty()) {
                    return new SimpleStringProperty("Không có dịch vụ");
                }
                
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
    }

    private void initializeInvoiceItemsTable() {
        invoiceItemsTable.setItems(invoiceItems);
        if (invoiceItemsTable.getColumns().size() >= 6) {
            invoiceItemsTable.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("index"));
            invoiceItemsTable.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("serviceName"));
            invoiceItemsTable.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("quantity"));
            invoiceItemsTable.getColumns().get(3).setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
            invoiceItemsTable.getColumns().get(4).setCellValueFactory(new PropertyValueFactory<>("total"));
            
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
        ObservableList<String> statusList = FXCollections.observableArrayList("Tất cả");
        for (StatusEnum status : StatusEnum.values()) {
            statusList.add(status.name());
        }
        statusFilter.setItems(statusList);
        statusFilter.setValue("Tất cả");

        ObservableList<String> paymentMethodList = FXCollections.observableArrayList("Tất cả");
        for (PaymentMethodEnum method : PaymentMethodEnum.values()) {
            paymentMethodList.add(method.name());
        }
        paymentMethodFilter.setItems(paymentMethodList);
        paymentMethodFilter.setValue("Tất cả");

        loadServices();
        
        ObservableList<String> paymentMethods = FXCollections.observableArrayList();
        for (PaymentMethodEnum method : PaymentMethodEnum.values()) {
            paymentMethods.add(method.name());
        }
        paymentMethodComboBox.setItems(paymentMethods);
        paymentMethodComboBox.setValue(PaymentMethodEnum.CASH.name());

        reportTypeSelector.getItems().addAll("Doanh thu theo ngày", "Doanh thu theo dịch vụ", "Doanh thu theo phương thức thanh toán");
        reportTypeSelector.setValue("Doanh thu theo ngày");
    }
    
    private void loadServices() {
        try {
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
        boolean canManagePayment = RoleChecker.hasPermission("MANAGE_PAYMENT");

        viewDetailsButton.setVisible(canViewInvoice);
        applyDiscountButton.setVisible(canManagePayment);
        refundButton.setVisible(canManagePayment);
    }
    
    private void setupEventListeners() {
        discountField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                discountField.setText(newValue.replaceAll("[^\\d]", ""));
            } else {
                updateInvoiceSummary();
            }
        });
        
        amountPaidField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                amountPaidField.setText(newValue.replaceAll("[^\\d]", ""));
            } else {
                updateChangeAmount();
            }
        });
        
        usePointsCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            pointsUsedField.setDisable(!newValue);
            if (!newValue) {
                pointsUsedField.setText("0");
                pointsValueLabel.setText("0 VND");
            }
            updateInvoiceSummary();
        });
        
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
        Object bookingIdObj = Session.getInstance().getAttribute("selectedBookingId");
        if (bookingIdObj instanceof Integer) {
            int bookingId = (Integer) bookingIdObj;
            
            try {
                String sql = "SELECT b.booking_id, b.booking_time, " +
                             "c.customer_id, c.point, p.person_id, p.full_name, p.phone, p.email, p.address, " +
                             "pet.pet_id, pet.name AS pet_name " +
                             "FROM booking b " +
                             "JOIN customer c ON b.customer_id = c.customer_id " +
                             "JOIN person p ON c.person_id = p.person_id " +
                             "JOIN pet ON b.pet_id = pet.pet_id " +
                             "WHERE b.booking_id = ?"; 
                
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(sql)) {
                    
                    stmt.setInt(1, bookingId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
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
                            
                            invoiceIdLabel.setText("HĐ-" + String.format("%05d", getNextInvoiceId()));
                            invoiceDateLabel.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                            
                            loadBookingServices(bookingId);
                        }
                    }
                }
            } catch (SQLException e) {
                showAlert(AlertType.ERROR, "Lỗi", "Không thể tải thông tin booking", e.getMessage());
            }
        } else {
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
        return 1;
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
        
        if (usePointsCheckbox.isSelected() && pointsUsedField.getText() != null && !pointsUsedField.getText().isEmpty()) {
            try {
                int pointsUsed = Integer.parseInt(pointsUsedField.getText());
                pointsValue = pointsUsed * 1000;
                pointsValueLabel.setText(String.format("%,.0f VND", pointsValue));
            } catch (NumberFormatException e) {
                // Ignore if not a number
            }
        }
        
        double total = subtotal - discount - pointsValue;
        if (total < 0) total = 0;
        
        subtotalLabel.setText(String.format("%,.0f VND", subtotal));
        discountAmountLabel.setText(String.format("%,.0f VND", discount));
        totalAmountLabel.setText(String.format("%,.0f VND", total));
        
        updateChangeAmount();
    }
    
    private void updateChangeAmount() {
        try {
            if (amountPaidField.getText() != null && !amountPaidField.getText().isEmpty()) {
                double amountPaid = Double.parseDouble(amountPaidField.getText());
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
                double pointsValue = pointsUsed * 1000;
                pointsValueLabel.setText(String.format("%,.0f VND", pointsValue));
                
                try {
                    int availablePoints = Integer.parseInt(customerPointsLabel.getText());
                    if (pointsUsed > availablePoints) {
                        showAlert(AlertType.WARNING, "Cảnh báo", 
                                "Điểm sử dụng vượt quá điểm hiện có", 
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
        applyDiscountButton.setDisable(!hasSelectedInvoice);
        refundButton.setDisable(!hasSelectedInvoice || 
                (hasSelectedInvoice && !StatusEnum.PAID.equals(invoice.getStatus())));
        
        if (hasSelectedInvoice) {
            loadInvoiceDetails(invoice);
        }
    }
    
    private void loadInvoiceDetails(Invoice invoice) {
        try {
            invoiceItems.clear();
            
            invoiceIdLabel.setText("HĐ-" + String.format("%05d", invoice.getInvoiceId()));
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            invoiceDateLabel.setText(invoice.getPaymentDate() != null ? 
                    invoice.getPaymentDate().toLocalDateTime().format(formatter) : "N/A");
            
            Staff cashier = invoice.getStaff();
            cashierNameLabel.setText(cashier != null ? cashier.getFullName() : "N/A");
            
            Order order = invoice.getOrder();
            if (order != null && order.getCustomer() != null) {
                Customer customer = order.getCustomer();
                customerIdField.setText("KH-" + String.format("%05d", customer.getId()));
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
            
            if (order != null) {
                List<OrderDetail> details = orderDetailRepository.selectByCondition("order_id = ?", order.getOrderId());
                int index = 1;
                
                for (OrderDetail detail : details) {
                    Service service = detail.getService();
                    if (service != null) {
                        double unitPrice = service.getPrice();
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
            
            subtotalLabel.setText(String.format("%,.0f VND", 
                    invoice.getSubtotal() != null ? invoice.getSubtotal().doubleValue() : 0));
            discountField.setText(invoice.getDiscountPercent() != null ? 
                    invoice.getDiscountPercent().toString() : "0");
            discountAmountLabel.setText(String.format("%,.0f VND", 
                    invoice.getDiscountAmount() != null ? invoice.getDiscountAmount().doubleValue() : 0));
            
            int pointsUsed = invoice.getPointsUsed() != null ? invoice.getPointsUsed() : 0;
            pointsUsedField.setText(String.valueOf(pointsUsed));
            pointsValueLabel.setText(String.format("%,.0f VND", pointsUsed * 1000));
            usePointsCheckbox.setSelected(pointsUsed > 0);
            
            promotionCodeField.setText(invoice.getPromotionCode() != null ? 
                    invoice.getPromotionCode() : "");
            
            totalAmountLabel.setText(String.format("%,.0f VND", 
                    invoice.getTotal() != null ? invoice.getTotal().doubleValue() : 0));
            if (invoice.getPaymentMethod() != null) {
                paymentMethodComboBox.setValue(invoice.getPaymentMethod().name());
            }
            
            amountPaidField.setText(invoice.getAmountPaid() != null ? 
                    invoice.getAmountPaid().toString() : "0");
            double amountPaid = invoice.getAmountPaid() != null ? 
                    invoice.getAmountPaid().doubleValue() : 0;
            double total = invoice.getTotal() != null ? 
                    invoice.getTotal().doubleValue() : 0;
            changeAmountLabel.setText(String.format("%,.0f VND", Math.max(0, amountPaid - total)));
            
            invoiceNoteField.setText(invoice.getNote() != null ? invoice.getNote() : "");
            
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Lỗi", "Không thể tải chi tiết hóa đơn", e.getMessage());
        }
    }

    @FXML
    private void onAddServiceButtonClick() {
        if (serviceSelector.getValue() == null || serviceSelector.getValue().isEmpty()) {
            showAlert(AlertType.WARNING, "Cảnh báo", "Dịch vụ trống", 
                    "Vui lòng chọn một dịch vụ.");
            return;
        }
        
        int quantity = 1;
        try {
            if (quantityField.getText() != null && !quantityField.getText().isEmpty()) {
                quantity = Integer.parseInt(quantityField.getText());
                if (quantity <= 0) {
                    showAlert(AlertType.WARNING, "Cảnh báo", "Số lượng không hợp lệ", 
                            "Số lượng phải lớn hơn 0.");
                    return;
                }
            }
        } catch (NumberFormatException e) {
            showAlert(AlertType.WARNING, "Cảnh báo", "Số lượng không hợp lệ", 
                    "Vui lòng nhập một số hợp lệ.");
            return;
        }
        
        try {
            String condition = "name = ?";
            List<Service> services = serviceRepository.selectByCondition(condition, 
                    serviceSelector.getValue());
            Service service = services.isEmpty() ? null : services.get(0);
            
            if (service != null) {
                Optional<InvoiceItem> existingItem = invoiceItems.stream()
                        .filter(item -> item.getServiceName().equals(service.getName()))
                        .findFirst();
                
                if (existingItem.isPresent()) {
                    InvoiceItem item = existingItem.get();
                    int newQuantity = item.getQuantity() + quantity;
                    item.setQuantity(newQuantity);
                    item.setTotal(item.getUnitPrice() * newQuantity);
                    invoiceItemsTable.refresh();
                } else {
                    double unitPrice = service.getPrice();
                    invoiceItems.add(new InvoiceItem(
                            invoiceItems.size() + 1,
                            service.getName(),
                            quantity,
                            unitPrice,
                            unitPrice * quantity
                    ));
                }
                
                serviceSelector.setValue(null);
                quantityField.setText("1");
                
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
    private void processPaymentAndPrint() {
        try {
            if (invoiceItems.isEmpty()) {
                showAlert(AlertType.WARNING, "Cảnh báo", "Hóa đơn trống", 
                        "Vui lòng thêm ít nhất một dịch vụ vào hóa đơn.");
                return;
            }
            
            double total = Double.parseDouble(totalAmountLabel.getText().replaceAll("[^\\d]", ""));
            double amountPaid = amountPaidField.getText().isEmpty() ? 0 : 
                    Double.parseDouble(amountPaidField.getText());
            
            if (amountPaid < total) {
                showAlert(AlertType.WARNING, "Cảnh báo", "Số tiền không đủ", 
                        "Số tiền khách trả phải lớn hơn hoặc bằng tổng thanh toán.");
                return;
            }
            
            if (customerIdField.getText().isEmpty()) {
                showAlert(AlertType.WARNING, "Cảnh báo", "Thiếu thông tin khách hàng", 
                        "Vui lòng tìm hoặc thêm khách hàng trước khi thanh toán.");
                return;
            }
            
            Invoice invoice = new Invoice();
            invoice.setInvoiceId(Integer.parseInt(invoiceIdLabel.getText().replace("HĐ-", "")));
            invoice.setSubtotal(new BigDecimal(subtotalLabel.getText().replaceAll("[^\\d]", "")));
            invoice.setDiscountPercent(discountField.getText().isEmpty() ? 
                    BigDecimal.ZERO : BigDecimal.valueOf(Integer.parseInt(discountField.getText())));
            invoice.setDiscountAmount(new BigDecimal(discountAmountLabel.getText().replaceAll("[^\\d]", "")));
            invoice.setPointsUsed(Integer.parseInt(pointsUsedField.getText().isEmpty() ? 
                    "0" : pointsUsedField.getText()));
            invoice.setTotal(new BigDecimal(total));
            invoice.setPaymentMethod(PaymentMethodEnum.valueOf(paymentMethodComboBox.getValue()));
            invoice.setAmountPaid(new BigDecimal(amountPaid));
            invoice.setPaymentDate(Timestamp.valueOf(LocalDateTime.now()));
            invoice.setStatus(StatusEnum.PAID);
            invoice.setNote(invoiceNoteField.getText());
            invoice.setStaff(Session.getCurrentStaff());
            
            Order order = new Order();
            String customerIdStr = customerIdField.getText().replace("KH-", "");
            int customerId = Integer.parseInt(customerIdStr);
            List<Customer> customers = customerRepository.selectByCondition("customer_id = ?", customerId);
            order.setCustomer(customers.isEmpty() ? null : customers.get(0));
            order.setOrderDate(Timestamp.valueOf(LocalDateTime.now()));
            order.setStatus(StatusEnum.COMPLETED);
            order.setTotalAmount(total);
            orderRepository.insert(order);
            
            for (InvoiceItem item : invoiceItems) {
                OrderDetail detail = new OrderDetail();
                detail.setOrder(order);
                List<Service> services = serviceRepository.selectByCondition("name = ?", item.getServiceName());
                detail.setService(services.isEmpty() ? null : services.get(0));
                detail.setQuantity(item.getQuantity());
                detail.setPrice(BigDecimal.valueOf(item.getUnitPrice()));
                orderDetailRepository.insert(detail);
            }
            
            invoice.setOrder(order);
            invoiceRepository.insert(invoice);
            
            String fileName = "invoice_" + invoice.getInvoiceId() + ".pdf";
            invoiceService.generateInvoicePDF(order.getOrderId(), fileName);
            
            File file = new File(fileName);
            if (file.exists()) {
                java.awt.Desktop.getDesktop().open(file);
            }
            
            showAlert(AlertType.INFORMATION, "Thành công", "Thanh toán và in hóa đơn", 
                    "Hóa đơn đã được lưu và in thành công.");
            
            resetInvoiceForm();
            loadInvoices();
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Lỗi", "Không thể xử lý thanh toán và in", e.getMessage());
        }
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
                    (invoice.getStatus() != null && 
                            invoice.getStatus().name().equals(statusValue));
            
            boolean paymentMethodMatch = paymentMethodValue.equals("Tất cả") || 
                    (invoice.getPaymentMethod() != null && 
                            invoice.getPaymentMethod().name().equals(paymentMethodValue));
            
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

    private void searchInvoices() {
        if (fromDatePicker.getValue() == null || toDatePicker.getValue() == null) {
            showAlert(AlertType.WARNING, "Cảnh báo", "Ngày không hợp lệ", 
                    "Vui lòng chọn khoảng thời gian.");
            return;
        }
        
        String searchQuery = searchField.getText().trim().toLowerCase();
        
        ObservableList<Invoice> filteredList = FXCollections.observableArrayList();
        
        for (Invoice invoice : invoiceList) {
            boolean matchesFilter = searchQuery.isEmpty() ||
                    String.valueOf(invoice.getInvoiceId()).contains(searchQuery) ||
                    (invoice.getOrder() != null && 
                            String.valueOf(invoice.getOrder().getOrderId()).contains(searchQuery)) ||
                    (invoice.getOrder() != null && invoice.getOrder().getCustomer() != null && 
                            invoice.getOrder().getCustomer().getFullName().toLowerCase().contains(searchQuery)) ||
                    (invoice.getOrder() != null && invoice.getOrder().getCustomer() != null && 
                            invoice.getOrder().getCustomer().getPhone().toLowerCase().contains(searchQuery));
            
            if (matchesFilter) {
                boolean statusMatch = statusFilter.getValue().equals("Tất cả") || 
                        (invoice.getStatus() != null && 
                                invoice.getStatus().name().equals(statusFilter.getValue()));
                
                boolean paymentMethodMatch = paymentMethodFilter.getValue().equals("Tất cả") || 
                        (invoice.getPaymentMethod() != null && 
                                invoice.getPaymentMethod().name().equals(paymentMethodFilter.getValue()));
                
                if (statusMatch && paymentMethodMatch) {
                    filteredList.add(invoice);
                }
            }
        }
        
        invoiceTable.setItems(filteredList);
        updateSummaryLabels(filteredList);
        statusMessageLabel.setText("Tìm thấy " + filteredList.size() + " kết quả");
    }

    private void searchCustomer() {
        String searchQuery = customerSearchField.getText().trim();
        
        if (searchQuery.isEmpty()) {
            showAlert(AlertType.WARNING, "Cảnh báo", "Tìm kiếm trống", 
                    "Vui lòng nhập số điện thoại để tìm kiếm khách hàng.");
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
    private void onSearchButtonClick() {
        searchInvoices();
    }

    @FXML
    private void viewDetails() {
        if (selectedInvoice == null) {
            showAlert(AlertType.WARNING, "Cảnh báo", "Không có hóa đơn được chọn", 
                    "Vui lòng chọn một hóa đơn để xem chi tiết.");
            return;
        }
        
        try {
            Stage detailStage = new Stage();
            VBox detailPane = new VBox(10);
            detailPane.setPadding(new Insets(10));
            
            Label titleLabel = new Label("Chi tiết hóa đơn #" + selectedInvoice.getInvoiceId());
            titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16;");
            
            Label customerLabel = new Label("Khách hàng: " + 
                    (selectedInvoice.getOrder() != null && 
                            selectedInvoice.getOrder().getCustomer() != null ? 
                            selectedInvoice.getOrder().getCustomer().getFullName() : "N/A"));
            Label dateLabel = new Label("Ngày thanh toán: " + 
                    (selectedInvoice.getPaymentDate() != null ? 
                            selectedInvoice.getPaymentDate().toLocalDateTime().format(
                                    DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "N/A"));
            Label totalLabel = new Label("Tổng tiền: " + 
                    String.format("%,.0f VND", selectedInvoice.getTotal() != null ? 
                            selectedInvoice.getTotal().doubleValue() : 0));
            
            Button reprintButton = new Button("In lại");
            reprintButton.setOnAction(e -> reprintInvoice());
            
            Button sendEmailButton = new Button("Gửi email");
            sendEmailButton.setOnAction(e -> sendEmail());
            
            HBox buttonBox = new HBox(10, reprintButton, sendEmailButton);
            
            detailPane.getChildren().addAll(titleLabel, customerLabel, dateLabel, totalLabel, buttonBox);
            
            Scene scene = new Scene(detailPane, 400, 200);
            detailStage.setTitle("Chi tiết hóa đơn");
            detailStage.setScene(scene);
            detailStage.initModality(Modality.APPLICATION_MODAL);
            detailStage.showAndWait();
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Lỗi", "Không thể mở chi tiết hóa đơn", e.getMessage());
        }
    }

    private void reprintInvoice() {
        try {
            String fileName = "invoice_" + selectedInvoice.getInvoiceId() + ".pdf";
            invoiceService.generateInvoicePDF(selectedInvoice.getOrder().getOrderId(), fileName);
            
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

    private void sendEmail() {
        try {
            Customer customer = selectedInvoice.getOrder().getCustomer();
            if (customer == null || customer.getEmail() == null || customer.getEmail().isEmpty()) {
                showAlert(AlertType.WARNING, "Cảnh báo", "Thiếu thông tin email", 
                        "Khách hàng không có địa chỉ email.");
                return;
            }
            
            invoiceService.sendInvoiceByEmail(selectedInvoice.getInvoiceId());
            
            showAlert(AlertType.INFORMATION, "Thành công", "Đã gửi hóa đơn", 
                    "Hóa đơn đã được gửi đến " + customer.getEmail());
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Lỗi", "Không thể gửi email", e.getMessage());
        }
    }

    @FXML
    private void createNewInvoice() {
        resetInvoiceForm();
        TabPane tabPane = (TabPane) invoiceTable.getScene().lookup("#tabPane");
        if (tabPane != null) {
            tabPane.getSelectionModel().select(1);
        }
    }
    
    private void resetInvoiceForm() {
        customerIdField.clear();
        customerNameField.clear();
        customerPhoneField.clear();
        customerEmailField.clear();
        customerPointsLabel.setText("0");
        invoiceItems.clear();
        subtotalLabel.setText("0 VND");
        discountField.setText("0");
        discountAmountLabel.setText("0 VND");
        pointsUsedField.setText("0");
        pointsValueLabel.setText("0 VND");
        usePointsCheckbox.setSelected(false);
        totalAmountLabel.setText("0 VND");
        amountPaidField.setText("0");
        changeAmountLabel.setText("0 VND");
        invoiceNoteField.clear();
        promotionCodeField.clear();
        invoiceIdLabel.setText("HĐ-" + String.format("%05d", getNextInvoiceId()));
        invoiceDateLabel.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
    }
    
    @FXML
    private void applyDiscount() {
        if (selectedInvoice == null) {
            showAlert(AlertType.WARNING, "Cảnh báo", "Không có hóa đơn được chọn", 
                    "Vui lòng chọn một hóa đơn để áp dụng khuyến mãi.");
            return;
        }
        
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
                
                applyDiscountToInvoice(selectedInvoice, discountPercent);
                
            } catch (NumberFormatException e) {
                showAlert(AlertType.WARNING, "Cảnh báo", "Giá trị không hợp lệ", 
                        "Vui lòng nhập một số nguyên cho phần trăm khuyến mãi.");
            }
        });
    }
    
    private void applyDiscountToInvoice(Invoice invoice, int discountPercent) {
        try {
            BigDecimal subtotal = invoice.getSubtotal() != null ? 
                    invoice.getSubtotal() : BigDecimal.ZERO;
            BigDecimal discountAmount = subtotal.multiply(
                    BigDecimal.valueOf(discountPercent)).divide(BigDecimal.valueOf(100));
            BigDecimal newTotal = subtotal.subtract(discountAmount);
            
            invoice.setDiscountPercent(BigDecimal.valueOf(discountPercent));
            invoice.setDiscountAmount(discountAmount);
            invoice.setTotal(newTotal);
            
            boolean success = invoiceRepository.update(invoice) > 0;
            if (success) {
                showAlert(AlertType.INFORMATION, "Thành công", "Đã áp dụng khuyến mãi", 
                        "Đã áp dụng khuyến mãi " + discountPercent + "% vào hóa đơn.");
                
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
    private void processRefund() {
        if (selectedInvoice == null) {
            showAlert(AlertType.WARNING, "Cảnh báo", "Không có hóa đơn được chọn", 
                    "Vui lòng chọn một hóa đơn để hoàn tiền.");
            return;
        }
        
        if (selectedInvoice.getStatus() != StatusEnum.PAID) {
            showAlert(AlertType.WARNING, "Cảnh báo", "Hóa đơn chưa thanh toán", 
                    "Chỉ có thể hoàn tiền cho hóa đơn đã thanh toán.");
            return;
        }
        
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận hoàn tiền");
        alert.setHeaderText("Bạn có chắc chắn muốn hoàn tiền?");
        alert.setContentText("Hoàn tiền cho hóa đơn #" + selectedInvoice.getInvoiceId() + 
                " với số tiền " + selectedInvoice.getTotal() + " VND?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                selectedInvoice.setStatus(StatusEnum.CANCELLED);
                boolean success = invoiceRepository.update(selectedInvoice) > 0;
                
                if (success) {
                    Order order = selectedInvoice.getOrder();
                    order.setStatus(StatusEnum.CANCELLED);
                    orderRepository.update(order);
                    
                    showAlert(AlertType.INFORMATION, "Thành công", "Đã hoàn tiền", 
                            "Hóa đơn đã được đánh dấu là đã hoàn tiền.");
                    
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
        try {
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
        
        showAlert(AlertType.INFORMATION, "Thông báo", "Mã khuyến mãi", 
                "Tính năng áp dụng mã khuyến mãi đang phát triển.");
    }
    
    @FXML
    private void cancelInvoice() {
        resetInvoiceForm();
    }

    @FXML
    private void viewReport() {
        if (reportFromDatePicker.getValue() == null || reportToDatePicker.getValue() == null) {
            showAlert(AlertType.WARNING, "Cảnh báo", "Ngày không hợp lệ", 
                    "Vui lòng chọn khoảng thời gian.");
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
                    showAlert(AlertType.WARNING, "Cảnh báo", "Loại báo cáo không hợp lệ", 
                            "Vui lòng chọn loại báo cáo.");
            }
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Lỗi", "Không thể tạo báo cáo", e.getMessage());
        }
    }
    
    private void generateDailyRevenueReport(LocalDate fromDate, LocalDate toDate) throws SQLException {
        revenueReports.clear();
        
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
                    String date = rs.getDate("date").toLocalDate().format(
                            DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                    int invoiceCount = rs.getInt("invoice_count");
                    double subtotal = rs.getDouble("subtotal");
                    double discount = rs.getDouble("discount");
                    double pointsValue = rs.getDouble("points_value");
                    double netRevenue = rs.getDouble("total");
                    
                    String trend = "";
                    if (previousDayRevenue > 0) {
                        double percentChange = ((netRevenue - previousDayRevenue) / 
                                previousDayRevenue) * 100;
                        trend = String.format("%+.1f%%", percentChange);
                    }
                    
                    revenueReports.add(new RevenueReport(
                            date,
                            invoiceCount,
                            subtotal,
                            discount,
                            0,
                            pointsValue,
                            netRevenue,
                            trend
                    ));
                    
                    totalRevenue += netRevenue;
                    totalInvoices += invoiceCount;
                    previousDayRevenue = netRevenue;
                }
            }
            
            reportTotalRevenueLabel.setText(String.format("%,.0f VND", totalRevenue));
            reportInvoiceCountLabel.setText(String.valueOf(totalInvoices));
            double averageValue = totalInvoices > 0 ? totalRevenue / totalInvoices : 0;
            reportAverageValueLabel.setText(String.format("%,.0f VND", averageValue));
            
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

            revenueReportTable.setItems(revenueReports);
            statusMessageLabel.setText("Đã tạo báo cáo doanh thu theo ngày thành công");
        } catch (SQLException e) {
            showAlert(AlertType.ERROR, "Lỗi", "Không thể tạo báo cáo doanh thu theo ngày", 
                    e.getMessage());
            statusMessageLabel.setText("Lỗi: " + e.getMessage());
        }
    }
    
    private void generateServiceRevenueReport(LocalDate fromDate, LocalDate toDate) {
        showAlert(AlertType.INFORMATION, "Thông báo", "Đang phát triển", 
                "Tính năng báo cáo doanh thu theo dịch vụ đang được phát triển.");
    }
    
    private void generatePaymentMethodRevenueReport(LocalDate fromDate, LocalDate toDate) {
        showAlert(AlertType.INFORMATION, "Thông báo", "Đang phát triển", 
                "Tính năng báo cáo doanh thu theo phương thức thanh toán đang được phát triển.");
    }
    
    @FXML
    private void exportToExcel() {
        if (revenueReports.isEmpty()) {
            showAlert(AlertType.WARNING, "Cảnh báo", "Không có dữ liệu", 
                    "Vui lòng tạo báo cáo trước khi xuất Excel.");
            return;
        }
        
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Lưu báo cáo Excel");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
            fileChooser.setInitialFileName("revenue_report.csv");
            
            File file = fileChooser.showSaveDialog(null);
            if (file != null) {
                try (FileWriter writer = new FileWriter(file)) {
                    writer.append("Ngày,Số hóa đơn,Doanh thu,Giảm giá,Khuyến mãi,Điểm tích lũy," +
                            "Doanh thu thuần,Xu hướng\n");
                    
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
