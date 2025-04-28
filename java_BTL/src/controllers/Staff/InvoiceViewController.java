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

    // Repositories và Services
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
    
    // Khởi tạo controller
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
        // Khởi tạo ngày giờ và thông tin nhân viên
        updateDateTime();
        Staff currentStaff = Session.getCurrentStaff();
        staffNameLabel.setText("Thu ngân: " + (currentStaff != null ? currentStaff.getFullName() : "N/A"));
        cashierNameLabel.setText(currentStaff != null ? currentStaff.getFullName() : "N/A");

        // Khởi tạo các cột bảng
        initializeTableColumns();

        // Khởi tạo bảng chi tiết hóa đơn
        initializeInvoiceItemsTable();

        // Thiết lập DatePicker
        setupDatePickers();

        // Thiết lập ComboBox
        setupComboBoxes();

        // Tải dữ liệu hóa đơn
        loadInvoices();

        // Khởi tạo tab tạo hóa đơn
        initializeInvoiceCreation();

        // Thiết lập lắng nghe sự kiện chọn hóa đơn
        invoiceTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> handleInvoiceSelection(newValue));

        // Thiết lập hành động tìm kiếm
        setupSearchField();

        // Thiết lập hiển thị nút dựa trên quyền
        setupButtonVisibility();

        // Cập nhật nhãn tóm tắt
        updateSummaryLabels();
        
        // Timer để cập nhật thời gian
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                javafx.application.Platform.runLater(() -> updateDateTime());
            }
        }, 0, 60000); // Cập nhật mỗi phút
        
        // Thiết lập lắng nghe sự kiện cho các trường nhập liệu
        setupEventListeners();
    }

    private void updateDateTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm");
        dateTimeLabel.setText(LocalDateTime.now().format(formatter));
    }

    private void initializeTableColumns() {
        // Các cột bảng hóa đơn
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
                
                // Lấy danh sách chi tiết đơn hàng từ database
                List<OrderDetail> details = orderDetailRepository.selectByCondition("order_id = ?", invoice.getOrder().getOrderId());
                if (details.isEmpty()) {
                    return new SimpleStringProperty("Không có dịch vụ");
                }
                
                // Nếu có nhiều dịch vụ, lấy dịch vụ đầu tiên và đánh dấu có thêm dịch vụ khác
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

        // Các cột bảng báo cáo
        initializeRevenueReportTable();
    }

    private void initializeRevenueReportTable() {
        // Thiết lập các cột cho bảng báo cáo doanh thu
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
        // Cấu hình các cột
        if (invoiceItemsTable.getColumns().size() >= 6) {
            invoiceItemsTable.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("index"));
            invoiceItemsTable.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("serviceName"));
            invoiceItemsTable.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("quantity"));
            invoiceItemsTable.getColumns().get(3).setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
            invoiceItemsTable.getColumns().get(4).setCellValueFactory(new PropertyValueFactory<>("total"));
            
            // Cột "Xóa"
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
        // Bộ lọc trạng thái
        ObservableList<String> statusList = FXCollections.observableArrayList("Tất cả");
        for (StatusEnum status : StatusEnum.values()) {
            statusList.add(status.name());
        }
        statusFilter.setItems(statusList);
        statusFilter.setValue("Tất cả");

        // Bộ lọc phương thức thanh toán
        ObservableList<String> paymentMethodList = FXCollections.observableArrayList("Tất cả");
        for (PaymentMethodEnum method : PaymentMethodEnum.values()) {
            paymentMethodList.add(method.name());
        }
        paymentMethodFilter.setItems(paymentMethodList);
        paymentMethodFilter.setValue("Tất cả");

        // Bộ chọn dịch vụ - Tải từ database
        loadServices();
        
        // Sử dụng các giá trị hợp lệ từ PaymentMethodEnum
        ObservableList<String> paymentMethods = FXCollections.observableArrayList();
        for (PaymentMethodEnum method : PaymentMethodEnum.values()) {
            paymentMethods.add(method.name());
        }
        paymentMethodComboBox.setItems(paymentMethods);
        paymentMethodComboBox.setValue(PaymentMethodEnum.CASH.name());

        // Bộ chọn loại báo cáo
        reportTypeSelector.getItems().addAll("Doanh thu theo ngày", "Doanh thu theo dịch vụ", "Doanh thu theo phương thức thanh toán");
        reportTypeSelector.setValue("Doanh thu theo ngày");
    }
    
    private void loadServices() {
        try {
            // Lấy danh sách dịch vụ từ database
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
        // Thiết lập sự kiện cho discountField để tính lại tổng tiền khi thay đổi
        discountField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                discountField.setText(newValue.replaceAll("[^\\d]", ""));
            } else {
                updateInvoiceSummary();
            }
        });
        
        // Thiết lập sự kiện cho amountPaidField để tính tiền thối
        amountPaidField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                amountPaidField.setText(newValue.replaceAll("[^\\d]", ""));
            } else {
                updateChangeAmount();
            }
        });
        
        // Thiết lập sự kiện cho usePointsCheckbox
        usePointsCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            pointsUsedField.setDisable(!newValue);
            if (!newValue) {
                pointsUsedField.setText("0");
                pointsValueLabel.setText("0 VND");
            }
            updateInvoiceSummary();
        });
        
        // Thiết lập sự kiện cho pointsUsedField
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
        // Kiểm tra bookingId từ Session
        Object bookingIdObj = Session.getInstance().getAttribute("selectedBookingId");
        if (bookingIdObj instanceof Integer) {
            int bookingId = (Integer) bookingIdObj;
            
            try {
                // Lấy thông tin booking từ database
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
                            // Điền thông tin khách hàng
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
                            
                            // Tạo hóa đơn mới
                            invoiceIdLabel.setText("HĐ-" + String.format("%05d", getNextInvoiceId()));
                            invoiceDateLabel.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                            
                            // Lấy thông tin dịch vụ của booking
                            loadBookingServices(bookingId);
                        }
                    }
                }
            } catch (SQLException e) {
                showAlert(AlertType.ERROR, "Lỗi", "Không thể tải thông tin booking", e.getMessage());
            }
        } else {
            // Khởi tạo hóa đơn mới trống
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
        return 1; // Mặc định là 1 nếu không có hóa đơn
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
        
        // Tính giảm giá nếu có
        try {
            if (discountField.getText() != null && !discountField.getText().isEmpty()) {
                double discountPercent = Double.parseDouble(discountField.getText());
                if (discountPercent > 0 && discountPercent <= 100) {
                    discount = subtotal * discountPercent / 100;
                }
            }
        } catch (NumberFormatException e) {
            // Bỏ qua nếu không phải số
        }
        
        // Tính điểm tích lũy sử dụng
        if (usePointsCheckbox.isSelected() && pointsUsedField.getText() != null && !pointsUsedField.getText().isEmpty()) {
            try {
                int pointsUsed = Integer.parseInt(pointsUsedField.getText());
                // Giả sử 1 điểm = 1,000 VND
                pointsValue = pointsUsed * 1000;
                // Hiển thị giá trị điểm
                pointsValueLabel.setText(String.format("%,.0f VND", pointsValue));
            } catch (NumberFormatException e) {
                // Bỏ qua nếu không phải số
            }
        }
        
        double total = subtotal - discount - pointsValue;
        if (total < 0) total = 0; // Đảm bảo tổng không âm
        
        // Cập nhật giao diện
        subtotalLabel.setText(String.format("%,.0f VND", subtotal));
        discountAmountLabel.setText(String.format("%,.0f VND", discount));
        totalAmountLabel.setText(String.format("%,.0f VND", total));
        
        // Cập nhật tiền thối lại
        updateChangeAmount();
    }
    
    private void updateChangeAmount() {
        try {
            if (amountPaidField.getText() != null && !amountPaidField.getText().isEmpty()) {
                double amountPaid = Double.parseDouble(amountPaidField.getText());
                // Lấy tổng thanh toán từ Label
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
                // Giả sử 1 điểm = 1,000 VND
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
    }
    
    private void searchInvoices() {
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
                customerIdField.setText("KH-" + String.format("%05d", customer.getCustomerId()));
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
    private void onViewDetailsButtonClick() {
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
    private void onProcessPaymentButtonClick() {
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
    private void onCreateInvoiceButtonClick() {
        // Validate
        if (invoiceItems.isEmpty()) {
            showAlert(AlertType.WARNING, "Cảnh báo", "Không có dịch vụ", "Vui lòng thêm ít nhất một dịch vụ.");
            return;
        }
        
        if (customerIdField.getText().isEmpty() || customerNameField.getText().isEmpty()) {
            showAlert(AlertType.WARNING, "Cảnh báo", "Thông tin khách hàng thiếu", "Vui lòng nhập thông tin khách hàng.");
            return;
        }
        
        // Get customer ID from field
        int customerId = 0;
        try {
            String customerIdText = customerIdField.getText().replace("KH-", "");
            customerId = Integer.parseInt(customerIdText);
        } catch (NumberFormatException e) {
            showAlert(AlertType.ERROR, "Lỗi", "Mã khách hàng không hợp lệ", "Vui lòng kiểm tra lại mã khách hàng.");
            return;
        }
        
        try {
            // Create new order
            Order order = new Order();
            order.setCustomerId(customerId);
            order.setOrderDate(Timestamp.valueOf(LocalDateTime.now()));
            order.setStatus(StatusEnum.COMPLETED);
            order.setNote(invoiceNoteField.getText());
            
            // Save order to get ID
            int orderId = orderRepository.insert(order);
            order.setOrderId(orderId);
            
            // Create order details
            for (InvoiceItem item : invoiceItems) {
                Service service = serviceRepository.findByName(item.getServiceName());
                if (service != null) {
                    OrderDetail detail = new OrderDetail();
                    detail.setOrderId(orderId);
                    detail.setServiceId(service.getServiceId());
                    detail.setQuantity(item.getQuantity());
                    detail.setUnitPrice(BigDecimal.valueOf(item.getUnitPrice()));
                    orderDetailRepository.insert(detail);
                }
            }
            
            // Create invoice
            Invoice invoice = new Invoice();
            invoice.setOrderId(orderId);
            invoice.setStaffId(Session.getCurrentStaff().getStaffId());
            invoice.setPaymentDate(Timestamp.valueOf(LocalDateTime.now()));
            
            // Calculate amounts
            double subtotal = invoiceItems.stream().mapToDouble(InvoiceItem::getTotal).sum();
            double discountAmount = 0;
            int discountPercent = 0;
            
            if (discountField.getText() != null && !discountField.getText().isEmpty()) {
                try {
                    discountPercent = Integer.parseInt(discountField.getText());
                    if (discountPercent > 0) {
                        discountAmount = subtotal * discountPercent / 100.0;
                    }
                } catch (NumberFormatException e) {
                    // Ignore if not a number
                }
            }
            
            // Calculate points value
            int pointsUsed = 0;
            if (usePointsCheckbox.isSelected() && pointsUsedField.getText() != null && !pointsUsedField.getText().isEmpty()) {
                try {
                    pointsUsed = Integer.parseInt(pointsUsedField.getText());
                } catch (NumberFormatException e) {
                    // Ignore if not a number
                }
            }
            
            // Calculate total
            double pointsValue = pointsUsed * 1000; // Assuming 1 point = 1000 VND
            double total = subtotal - discountAmount - pointsValue;
            if (total < 0) total = 0;
            
            // Set amount paid
            double amountPaid = 0;
            if (amountPaidField.getText() != null && !amountPaidField.getText().isEmpty()) {
                try {
                    amountPaid = Double.parseDouble(amountPaidField.getText());
                } catch (NumberFormatException e) {
                    // Ignore if not a number
                }
            }
            
            // Set invoice fields
            invoice.setSubtotal(BigDecimal.valueOf(subtotal));
            invoice.setDiscountPercent(discountPercent);
            invoice.setDiscountAmount(BigDecimal.valueOf(discountAmount));
            invoice.setPointsUsed(pointsUsed);
            invoice.setTotal(BigDecimal.valueOf(total));
            invoice.setAmountPaid(BigDecimal.valueOf(amountPaid));
            invoice.setNote(invoiceNoteField.getText());
            invoice.setPromotionCode(promotionCodeField.getText());
            
            // Set payment method
            try {
                PaymentMethodEnum paymentMethod = PaymentMethodEnum.valueOf(paymentMethodComboBox.getValue());
                invoice.setPaymentMethod(paymentMethod);
            } catch (IllegalArgumentException e) {
                invoice.setPaymentMethod(PaymentMethodEnum.CASH); // Default to cash
            }
            
            // Set status based on payment vs total
            if (amountPaid >= total) {
                invoice.setStatus(StatusEnum.PAID);
                
                // Update customer points if paid
                Customer customer = customerRepository.selectById(customerId);
                if (customer != null) {
                    // Deduct points used
                    int currentPoints = customer.getPoint() - pointsUsed;
                    // Add new points (1 point for each 100,000 VND)
                    int newPoints = (int)(total / 100000);
                    customer.setPoint(Math.max(0, currentPoints + newPoints));
                    customerRepository.update(customer);
                }
            } else {
                invoice.setStatus(StatusEnum.PENDING);
            }
            
            // Save invoice
            int invoiceId = invoiceRepository.insert(invoice);
            
            // Show success message
            showAlert(AlertType.INFORMATION, "Thành công", "Tạo hóa đơn thành công", 
                    "Hóa đơn #" + invoiceId + " đã được tạo thành công.");
            
            // Print receipt
            if (invoice.getStatus() == StatusEnum.PAID) {
                printReceipt(invoiceId);
            }
            
            // Reset form
            resetInvoiceForm();
            
            // Refresh table
            loadInvoices();
            
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Lỗi", "Không thể tạo hóa đơn", e.getMessage());
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
    private void onGenerateReportButtonClick() {
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
                stmtTop.setTimestamp(2, Timestamp.valueOf(                stmtTop.setTimestamp(2, Timestamp.valueOf(toDate.plusDays(1).atStartOfDay()));
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