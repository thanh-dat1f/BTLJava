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
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import model.Invoice;
import model.Staff;
import service.InvoiceService;
import utils.RoleChecker;
import utils.Session;
import controllers.SceneSwitcher;

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

    private final InvoiceService invoiceService;
    private ObservableList<Invoice> invoiceList;
    private ObservableList<InvoiceItem> invoiceItems;
    private ObservableList<RevenueReport> revenueReports;
    private Invoice selectedInvoice;

    public InvoiceViewController() {
        this.invoiceService = new InvoiceService();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Khởi tạo ngày giờ và thông tin nhân viên
        dateTimeLabel.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm")));
        Staff currentStaff = Session.getInstance().getCurrentStaff();
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
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("paymentDate"));
        dateColumn.setCellFactory(column -> new TableCell<Invoice, LocalDateTime>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "N/A" : formatter.format(item));
            }
        });
        serviceColumn.setCellValueFactory(cellData -> {
            Invoice invoice = cellData.getValue();
            return new SimpleStringProperty("Dịch vụ mẫu"); // Giả lập
        });
        totalColumn.setCellValueFactory(new PropertyValueFactory<>("total"));
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

        // Các cột bảng báo cáo (giả lập)
        revenueReportTable.setPlaceholder(new Label("Chưa có dữ liệu báo cáo"));
    }

    private void initializeInvoiceItemsTable() {
        invoiceItems = FXCollections.observableArrayList();
        invoiceItemsTable.setItems(invoiceItems);
        // Cấu hình các cột
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

        // Bộ chọn dịch vụ
        serviceSelector.getItems().addAll("Tắm và cắt tỉa lông", "Khám sức khỏe");
        // Sử dụng các giá trị hợp lệ từ PaymentMethodEnum
        paymentMethodComboBox.getItems().addAll(
                PaymentMethodEnum.CASH.name(),
                PaymentMethodEnum.CARD.name(),
                PaymentMethodEnum.MOMO.name(),
                PaymentMethodEnum.BANKING.name());

        // Bộ chọn loại báo cáo
        reportTypeSelector.getItems().addAll("Doanh thu theo ngày", "Doanh thu theo dịch vụ");
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

    private void loadInvoices() {
        try {
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
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải danh sách hóa đơn", e.getMessage());
        }
    }

    private void initializeInvoiceCreation() {
        // Kiểm tra bookingId từ Session
        Object bookingIdObj = Session.getInstance().getAttribute("selectedBookingId");
        if (bookingIdObj instanceof Integer) {
            int bookingId = (Integer) bookingIdObj;
            // Giả lập: Tự động điền thông tin khách hàng và dịch vụ
            customerIdField.setText("KH-" + String.format("%05d", bookingId));
            customerNameField.setText("Khách hàng mẫu");
            customerPhoneField.setText("0123456789");
            customerEmailField.setText("example@email.com");
            customerPointsLabel.setText("100");
            invoiceIdLabel.setText("HĐ-" + String.format("%05d", bookingId));
            invoiceDateLabel.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            subtotalLabel.setText("0 VND");
            discountAmountLabel.setText("0 VND");
            totalAmountLabel.setText("0 VND");
            changeAmountLabel.setText("0 VND");
            // Giả lập thêm dịch vụ
            invoiceItems.add(new InvoiceItem(1, "Tắm và cắt tỉa lông", 1, 100000, 100000));
            updateInvoiceSummary();
        }
    }

    private void updateInvoiceSummary() {
        double subtotal = invoiceItems.stream().mapToDouble(InvoiceItem::getTotal).sum();
        double discount = 0; // Giả lập, cần logic thực tế
        double total = subtotal - discount;
        subtotalLabel.setText(String.format("%,.0f VND", subtotal));
        discountAmountLabel.setText(String.format("%,.0f VND", discount));
        totalAmountLabel.setText(String.format("%,.0f VND", total));
    }

    private void updateSummaryLabels() {
        if (invoiceList == null) return;
        int total = invoiceList.size();
        int paid = 0;
        int pending = 0;
        double totalRevenue = 0;

        for (Invoice invoice : invoiceList) {
            if (invoice.getStatus() == StatusEnum.COMPLETED) {
                paid++;
                totalRevenue += invoice.getTotal() != null ? invoice.getTotal().doubleValue() : 0;
            } else if (invoice.getStatus() == StatusEnum.PENDING) {
                pending++;
            }
        }

        totalInvoicesLabel.setText(String.valueOf(total));
        paidInvoicesLabel.setText(String.valueOf(paid));
        pendingInvoicesLabel.setText(String.valueOf(pending));
        totalRevenueLabel.setText(String.format("%,.0f VND", totalRevenue));
    }

    private void applyFilters() {
        if (invoiceList == null) return;
        ObservableList<Invoice> filteredList = FXCollections.observableArrayList(invoiceList);

        String selectedStatus = statusFilter.getValue();
        if (selectedStatus != null && !selectedStatus.equals("Tất cả")) {
            filteredList.removeIf(invoice ->
                    invoice.getStatus() == null || !invoice.getStatus().name().equals(selectedStatus));
        }

        String selectedMethod = paymentMethodFilter.getValue();
        if (selectedMethod != null && !selectedMethod.equals("Tất cả")) {
            filteredList.removeIf(invoice ->
                    invoice.getPaymentMethod() == null || !invoice.getPaymentMethod().name().equals(selectedMethod));
        }

        String searchText = searchField.getText();
        if (searchText != null && !searchText.trim().isEmpty()) {
            String lowerCaseSearch = searchText.toLowerCase();
            filteredList.removeIf(invoice -> {
                if (String.valueOf(invoice.getInvoiceId()).contains(lowerCaseSearch)) return false;
                if (invoice.getOrder() != null && String.valueOf(invoice.getOrder().getOrderId()).contains(lowerCaseSearch)) return false;
                if (invoice.getOrder() != null && invoice.getOrder().getCustomer() != null &&
                        invoice.getOrder().getCustomer().getFullName().toLowerCase().contains(lowerCaseSearch)) return false;
                return true;
            });
        }

        invoiceTable.setItems(filteredList);
        updateSummaryLabels();
    }

    private void handleInvoiceSelection(Invoice invoice) {
        selectedInvoice = invoice;
        boolean hasSelection = (invoice != null);
        boolean isCompleted = hasSelection && (invoice.getStatus() != null && invoice.getStatus().equals(StatusEnum.COMPLETED));
        boolean isPending = hasSelection && (invoice.getStatus() != null && invoice.getStatus().equals(StatusEnum.PENDING));

        viewDetailsButton.setDisable(!hasSelection);
        reprintButton.setDisable(!(hasSelection && isCompleted));
        sendEmailButton.setDisable(!(hasSelection && isCompleted));
        processPaymentButton.setDisable(!(hasSelection && isPending));
        applyDiscountButton.setDisable(!(hasSelection && isPending));
        refundButton.setDisable(!(hasSelection && isCompleted));
    }

    @FXML
    private void searchInvoices() {
        applyFilters();
    }

    @FXML
    private void createNewInvoice() {
        // Đã được xử lý bởi tab "Tạo hóa đơn mới"
    }

    @FXML
    private void viewDetails(ActionEvent event) {
        if (selectedInvoice == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Chưa chọn hóa đơn",
                    "Vui lòng chọn một hóa đơn để xem chi tiết.");
            return;
        }
        try {
            SceneSwitcher.switchScene("invoice_detail.fxml");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể mở chi tiết hóa đơn", e.getMessage());
        }
    }

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
        showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Chức năng đang phát triển",
                "Chức năng thanh toán đang được phát triển.");
    }

    @FXML
    private void applyDiscount(ActionEvent event) {
        showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Chức năng đang phát triển",
                "Chức năng áp dụng khuyến mãi đang được phát triển.");
    }

    @FXML
    private void reprintInvoice(ActionEvent event) {
        if (selectedInvoice == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Chưa chọn hóa đơn",
                    "Vui lòng chọn một hóa đơn để in lại.");
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

    @FXML
    private void processRefund(ActionEvent event) {
        showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Chức năng đang phát triển",
                "Chức năng hoàn tiền đang được phát triển.");
    }

    @FXML
    private void resetFilter(ActionEvent event) {
        LocalDate now = LocalDate.now();
        fromDatePicker.setValue(now.minusDays(30));
        toDatePicker.setValue(now);
        searchField.clear();
        statusFilter.setValue("Tất cả");
        paymentMethodFilter.setValue("Tất cả");
        loadInvoices();
    }

    @FXML
    private void searchCustomer() {
        String query = customerSearchField.getText().trim();
        if (query.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Chưa nhập thông tin",
                    "Vui lòng nhập tên hoặc số điện thoại khách hàng.");
            return;
        }
        // Giả lập: Tìm khách hàng
        customerIdField.setText("KH-001");
        customerNameField.setText("Khách hàng mẫu");
        customerPhoneField.setText("0123456789");
        customerEmailField.setText("example@email.com");
        customerPointsLabel.setText("100");
        showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã tìm thấy khách hàng",
                "Tìm thấy khách hàng: Khách hàng mẫu.");
    }

    @FXML
    private void addNewCustomer() {
        showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Chức năng đang phát triển",
                "Chức năng thêm khách hàng mới đang được phát triển.");
    }

    @FXML
    private void addServiceToInvoice() {
        String service = serviceSelector.getValue();
        String quantityStr = quantityField.getText().trim();
        if (service == null || quantityStr.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Chưa đủ thông tin",
                    "Vui lòng chọn dịch vụ và nhập số lượng.");
            return;
        }
        try {
            int quantity = Integer.parseInt(quantityStr);
            if (quantity <= 0) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Số lượng không hợp lệ",
                        "Số lượng phải lớn hơn 0.");
                return;
            }
            // Giả lập: Thêm dịch vụ vào invoiceItems
            double unitPrice = 100000; // Giả lập
            invoiceItems.add(new InvoiceItem(invoiceItems.size() + 1, service, quantity, unitPrice, quantity * unitPrice));
            updateInvoiceSummary();
            quantityField.clear();
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Số lượng không hợp lệ",
                    "Vui lòng nhập số lượng là một số nguyên.");
        }
    }

    @FXML
    private void applyPromotionCode() {
        showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Chức năng đang phát triển",
                "Chức năng áp dụng mã khuyến mãi đang được phát triển.");
    }

    @FXML
    private void processPaymentAndPrint() {
        if (invoiceItems.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Chưa có dịch vụ",
                    "Vui lòng thêm ít nhất một dịch vụ vào hóa đơn.");
            return;
        }
        String amountPaidStr = amountPaidField.getText().trim();
        try {
            double amountPaid = Double.parseDouble(amountPaidStr);
            double total = invoiceItems.stream().mapToDouble(InvoiceItem::getTotal).sum();
            if (amountPaid < total) {
                showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Số tiền không đủ",
                        "Số tiền khách trả phải lớn hơn hoặc bằng tổng thanh toán.");
                return;
            }
            changeAmountLabel.setText(String.format("%,.0f VND", amountPaid - total));
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Thanh toán hoàn tất",
                    "Hóa đơn đã được thanh toán và gửi lệnh in.");
            // Giả lập lưu hóa đơn
            invoiceItems.clear();
            updateInvoiceSummary();
            amountPaidField.clear();
            changeAmountLabel.setText("0 VND");
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Số tiền không hợp lệ",
                    "Vui lòng nhập số tiền là một số hợp lệ.");
        }
    }

    @FXML
    private void cancelInvoice() {
        invoiceItems.clear();
        updateInvoiceSummary();
        customerIdField.clear();
        customerNameField.clear();
        customerPhoneField.clear();
        customerEmailField.clear();
        customerPointsLabel.setText("0");
        invoiceNoteField.clear();
        amountPaidField.clear();
        changeAmountLabel.setText("0 VND");
        showAlert(Alert.AlertType.INFORMATION, "Thành công", "Hóa đơn đã được hủy",
                "Hóa đơn đã được hủy.");
    }

    @FXML
    private void viewReport() {
        // Giả lập báo cáo
        revenueReports = FXCollections.observableArrayList();
        revenueReportTable.setItems(revenueReports);
        reportTotalRevenueLabel.setText("15,250,000 VND");
        reportInvoiceCountLabel.setText("48");
        reportAverageValueLabel.setText("317,708 VND");
        reportTopServiceLabel.setText("Tắm spa");
    }

    @FXML
    private void exportToExcel() {
        showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Chức năng đang phát triển",
                "Chức năng xuất Excel đang được phát triển.");
    }

    @FXML
    private void printReport() {
        showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Chức năng đang phát triển",
                "Chức năng in báo cáo đang được phát triển.");
    }

    @FXML
    private void showHelp() {
        showAlert(Alert.AlertType.INFORMATION, "Trợ giúp", "Hướng dẫn sử dụng",
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

    // Giả lập lớp InvoiceItem
    private static class InvoiceItem {
        private final int index;
        private final String serviceName;
        private final int quantity;
        private final double unitPrice;
        private final double total;

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
        public double getUnitPrice() { return unitPrice; }
        public double getTotal() { return total; }
    }

    // Giả lập lớp RevenueReport
    private static class RevenueReport {
        // Cần định nghĩa các thuộc tính phù hợp khi có dữ liệu thực tế
    }
}