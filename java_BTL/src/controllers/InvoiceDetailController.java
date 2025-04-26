package controllers;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.List;

import enums.PaymentMethodEnum;
import enums.StatusEnum;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.print.PrinterJob;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Customer;
import model.Invoice;
import model.Order;
import model.OrderDetail;
import model.Service;
import model.Staff;
import service.InvoiceService;
import service.OrderDetailService;
import service.OrderService;
import utils.Session;

public class InvoiceDetailController {

    @FXML private Label invoiceNumberLabel;
    @FXML private Label dateLabel;
    @FXML private Label customerNameLabel;
    @FXML private Label customerPhoneLabel;
    @FXML private Label customerEmailLabel;
    @FXML private Label staffNameLabel;
    @FXML private Label statusLabel;
    @FXML private Label totalAmountLabel;

    @FXML private TableView<OrderDetail> detailsTable;
    @FXML private TableColumn<OrderDetail, Integer> idColumn;
    @FXML private TableColumn<OrderDetail, String> serviceNameColumn;
    @FXML private TableColumn<OrderDetail, Integer> quantityColumn;
    @FXML private TableColumn<OrderDetail, String> priceColumn;
    @FXML private TableColumn<OrderDetail, String> subtotalColumn;

    @FXML private ComboBox<PaymentMethodEnum> paymentMethodComboBox;
    @FXML private TextArea notesTextArea;
    @FXML private VBox paymentBox;
    @FXML private TextField discountTextField;
    
    @FXML private Button saveButton;
    @FXML private Button printButton;
    @FXML private Button emailButton;
    @FXML private Button backButton;

    private Invoice currentInvoice;
    private Order currentOrder;
    private OrderDetailService orderDetailService;
    private OrderService orderService;
    private InvoiceService invoiceService;
    private ObservableList<OrderDetail> detailsList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Initialize services
        orderDetailService = new OrderDetailService();
        orderService = new OrderService();
        invoiceService = new InvoiceService();

        // Set up payment method combo box
        paymentMethodComboBox.getItems().setAll(PaymentMethodEnum.values());
        
        // Set up table columns
        setupTableColumns();
        
        // Add listener for discount field to recalculate total
        discountTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*(\\.\\d*)?")) {
                discountTextField.setText(oldValue);
            } else {
                updateTotalAmount();
            }
        });
    }

    /**
     * Load invoice details
     * @param invoice The invoice to load
     */
    public void setInvoice(Invoice invoice) {
        try {
            currentInvoice = invoice;
            if (currentInvoice == null) {
                showAlert(AlertType.ERROR, "Error", "Invoice not found", "Could not load invoice details");
                return;
            }
            
            currentOrder = orderService.getOrderById(currentInvoice.getOrder().getOrderId());
            
            // Load order details
            List<OrderDetail> details = orderDetailService.getDetailsByOrderId(currentOrder.getOrderId());
            detailsList.setAll(details);
            detailsTable.setItems(detailsList);
            
            // Set invoice and customer info
            updateLabels();
            
            // Set payment method if already selected
            if (currentInvoice.getPaymentMethod() != null) {
                paymentMethodComboBox.setValue(currentInvoice.getPaymentMethod());
            }
            
            // Update UI based on invoice status
            updateUIForStatus();
            
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Error", "Failed to load invoice", e.getMessage());
        }
    }

    /**
     * Set up table columns for invoice details
     */
    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("orderDetailId"));
        
        serviceNameColumn.setCellValueFactory(cellData -> {
            Service service = cellData.getValue().getService();
            return new SimpleStringProperty(service != null ? service.getName() : "");
        });
        
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        
        priceColumn.setCellValueFactory(cellData -> {
            BigDecimal price = cellData.getValue().getPrice();
            return new SimpleStringProperty(price != null ? String.format("%,.0f VND", price) : "");
        });
        
        subtotalColumn.setCellValueFactory(cellData -> {
            OrderDetail detail = cellData.getValue();
            BigDecimal subtotal = detail.getPrice().multiply(BigDecimal.valueOf(detail.getQuantity()));
            return new SimpleStringProperty(String.format("%,.0f VND", subtotal));
        });
    }

    /**
     * Update all the labels with current invoice and order data
     */
    private void updateLabels() {
        // Invoice info
        invoiceNumberLabel.setText("Invoice #" + currentInvoice.getInvoiceId());
        
        // Format date
        Timestamp timestamp = currentInvoice.getPaymentDate();
        if (timestamp != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            dateLabel.setText(timestamp.toLocalDateTime().format(formatter));
        } else {
            dateLabel.setText("N/A");
        }
        
        // Customer info
        Customer customer = currentOrder.getCustomer();
        if (customer != null) {
            customerNameLabel.setText(customer.getFullName());
            customerPhoneLabel.setText(customer.getPhone());
            customerEmailLabel.setText(customer.getEmail());
        }
        
        // Staff info
        Staff staff = currentInvoice.getStaff();
        if (staff != null) {
            staffNameLabel.setText(staff.getFullName());
        } else {
            Staff currentStaff = Session.getCurrentStaff();
            if (currentStaff != null) {
                staffNameLabel.setText(currentStaff.getFullName());
            }
        }
        
        // Status
        StatusEnum status = currentInvoice.getStatus();
        if (status != null) {
            statusLabel.setText(status.name());
        }
        
        // Update total amount
        updateTotalAmount();
    }
    
    /**
     * Update the total amount display, accounting for possible discount
     */
    private void updateTotalAmount() {
        if (currentInvoice != null) {
            BigDecimal total = currentInvoice.getTotal();
            
            // Apply discount if present
            try {
                if (!discountTextField.getText().isEmpty()) {
                    double discountPercent = Double.parseDouble(discountTextField.getText());
                    if (discountPercent > 0 && discountPercent <= 100) {
                        BigDecimal discount = total.multiply(BigDecimal.valueOf(discountPercent / 100.0));
                        total = total.subtract(discount);
                    }
                }
            } catch (NumberFormatException e) {
                // Ignore invalid input
            }
            
            totalAmountLabel.setText(String.format("%,.0f VND", total));
        }
    }
    
    /**
     * Update UI elements based on invoice status
     */
    private void updateUIForStatus() {
        boolean isCompleted = currentInvoice.getStatus() == StatusEnum.COMPLETED;
        boolean isPending = currentInvoice.getStatus() == StatusEnum.PENDING;
        
        paymentMethodComboBox.setDisable(isCompleted);
        discountTextField.setDisable(isCompleted);
        saveButton.setDisable(isCompleted);
        
        printButton.setDisable(!isCompleted);
        emailButton.setDisable(!isCompleted);
        
        if (isPending) {
            // Set default values for new invoice
            if (currentInvoice.getPaymentMethod() == null) {
                paymentMethodComboBox.setValue(PaymentMethodEnum.CASH);
            }
        }
    }

    /**
     * Save invoice changes
     */
    @FXML
    private void handleSave(ActionEvent event) {
        try {
            if (paymentMethodComboBox.getValue() == null) {
                showAlert(AlertType.WARNING, "Warning", "Missing Information", "Please select a payment method");
                return;
            }
            
            // Update invoice data
            currentInvoice.setPaymentMethod(paymentMethodComboBox.getValue());
            currentInvoice.setStatus(StatusEnum.COMPLETED);
            
            // Get current timestamp for payment date
            if (currentInvoice.getPaymentDate() == null) {
                currentInvoice.setPaymentDate(new Timestamp(System.currentTimeMillis()));
            }
            
            // Get staff from session if not set
            if (currentInvoice.getStaff() == null) {
                Staff currentStaff = Session.getCurrentStaff();
                if (currentStaff != null) {
                    currentInvoice.setStaff(currentStaff);
                } else {
                    showAlert(AlertType.ERROR, "Error", "Staff not found", "No staff is currently logged in");
                    return;
                }
            }
            
            // Apply discount if present
            if (!discountTextField.getText().isEmpty()) {
                try {
                    double discountPercent = Double.parseDouble(discountTextField.getText());
                    if (discountPercent > 0 && discountPercent <= 100) {
                        // Calculate new total after discount
                        BigDecimal originalTotal = currentInvoice.getTotal();
                        BigDecimal discount = originalTotal.multiply(BigDecimal.valueOf(discountPercent / 100.0));
                        BigDecimal newTotal = originalTotal.subtract(discount);
                        currentInvoice.setTotal(newTotal);
                    }
                } catch (NumberFormatException e) {
                    // Ignore invalid input
                }
            }
            
            // Save invoice
            boolean success = invoiceService.updateInvoice(currentInvoice);
            
            if (success) {
                showAlert(AlertType.INFORMATION, "Success", "Invoice Saved", "Invoice has been successfully processed");
                
                // Update UI
                updateLabels();
                updateUIForStatus();
            } else {
                showAlert(AlertType.ERROR, "Error", "Failed to save invoice", "Could not save invoice changes");
            }
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Error", "Failed to save invoice", e.getMessage());
        }
    }

    /**
     * Print invoice
     */
    @FXML
    private void handlePrint(ActionEvent event) {
        try {
            if (currentInvoice.getStatus() != StatusEnum.COMPLETED) {
                showAlert(AlertType.WARNING, "Warning", "Invoice not completed", 
                        "Please complete the invoice before printing");
                return;
            }
            
            invoiceService.printInvoice(currentInvoice.getInvoiceId());
            showAlert(AlertType.INFORMATION, "Success", "Print job sent", 
                    "Invoice has been sent to the printer");
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Error", "Print failed", e.getMessage());
        }
    }

    /**
     * Email invoice to customer
     */
    @FXML
    private void handleEmail(ActionEvent event) {
        try {
            if (currentInvoice.getStatus() != StatusEnum.COMPLETED) {
                showAlert(AlertType.WARNING, "Warning", "Invoice not completed", 
                        "Please complete the invoice before sending email");
                return;
            }
            
            Customer customer = currentOrder.getCustomer();
            if (customer == null || customer.getEmail() == null || customer.getEmail().isEmpty()) {
                showAlert(AlertType.WARNING, "Warning", "Missing email", 
                        "Customer does not have an email address");
                return;
            }
            
            invoiceService.sendInvoiceByEmail(currentInvoice.getInvoiceId());
            showAlert(AlertType.INFORMATION, "Success", "Email sent", 
                    "Invoice has been sent to " + customer.getEmail());
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Error", "Email failed", e.getMessage());
        }
    }

    /**
     * Go back to previous screen
     */
    @FXML
    private void handleBack(ActionEvent event) {
        // Close the current window or switch to previous scene
        Stage stage = (Stage) backButton.getScene().getWindow();
        stage.close();
    }
    
    /**
     * Show alert dialog
     */
    private void showAlert(AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}