package controllers.admin;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import model.Customer;
import service.CustomerService;
import enums.GenderEnum;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CustomerController {

    @FXML private TextField searchTextField;
    @FXML private TableView<Customer> customerTable;
    @FXML private TableColumn<Customer, String> fullNameColumn;
    @FXML private TableColumn<Customer, String> genderColumn;
    @FXML private TableColumn<Customer, String> phoneColumn;
    @FXML private TableColumn<Customer, String> emailColumn;
    @FXML private TableColumn<Customer, String> registrationDateColumn;
    @FXML private TableColumn<Customer, String> loyaltyPointsColumn;

    @FXML private VBox formBox;
    @FXML private TextField txtFullName;
    @FXML private ComboBox<String> cmbGender;
    @FXML private TextField txtPhone;
    @FXML private TextField txtAddress;
    @FXML private TextField txtEmail;
    @FXML private DatePicker dpRegistrationDate;
    @FXML private TextField txtLoyaltyPoints;
    @FXML private Button saveButton;

    private final CustomerService customerService = new CustomerService();
    private ObservableList<Customer> customerList = FXCollections.observableArrayList();
    private Customer selectedCustomer = null;

    @FXML
    public void initialize() {
        cmbGender.setItems(FXCollections.observableArrayList("Male", "Female", "Other"));
        setupTable();
        loadCustomerData();
    }

    private void setupTable() {
        fullNameColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getFullName()));
        genderColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getGender().toString()));
        phoneColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getPhone()));
        emailColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getEmail()));
        registrationDateColumn.setCellValueFactory(cell -> {
            Date date = cell.getValue().getCreated_at();
            return new SimpleStringProperty(date != null ? new SimpleDateFormat("yyyy-MM-dd").format(date) : "");
        });
        loyaltyPointsColumn.setCellValueFactory(cell -> new SimpleStringProperty(String.valueOf(cell.getValue().getPoint())));

        customerTable.setItems(customerList);
        customerTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) showCustomerInForm(newVal);
        });
    }

    @FXML
    private void loadCustomerData() {
        customerList.setAll(customerService.getAllCustomers());
    }

    @FXML
    private void handleSearchCustomer() {
        String keyword = searchTextField.getText().trim();
        if (keyword.isEmpty()) {
            loadCustomerData();
        } else {
            try {
                Customer found = customerService.findCustomerByPhoneNumber(keyword);
                customerList.setAll(found);
            } catch (Exception e) {
                showAlert(Alert.AlertType.INFORMATION, "Không tìm thấy", e.getMessage());
            }
        }
    }

    @FXML
    private void handleAddCustomer() {
        selectedCustomer = null;
        clearForm();
        formBox.setVisible(true);
        formBox.setManaged(true);
    }

    @FXML
    private void handleEditCustomer() {
        selectedCustomer = customerTable.getSelectionModel().getSelectedItem();
        if (selectedCustomer == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn khách hàng để sửa.");
            return;
        }
        formBox.setVisible(true);
        formBox.setManaged(true);
    }

    @FXML
    private void handleDeleteCustomer() {
        Customer customer = customerTable.getSelectionModel().getSelectedItem();
        if (customer == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn khách hàng để xóa.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Bạn có chắc muốn xóa khách hàng này?", ButtonType.OK, ButtonType.CANCEL);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                customerService.deleteCustomer(customer.getId());
                loadCustomerData();
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã xóa khách hàng.");
            }
        });
    }

    @FXML
    private void handleSaveCustomer() {
        try {
            String fullName = txtFullName.getText();
            String phone = txtPhone.getText();
            String email = txtEmail.getText();
            String genderStr = cmbGender.getValue();
            String address = txtAddress.getText();
            int loyaltyPoints = Integer.parseInt(txtLoyaltyPoints.getText());
            Timestamp regDate = dpRegistrationDate.getValue() != null
            	    ? Timestamp.valueOf(dpRegistrationDate.getValue().atStartOfDay())
            	    : null;

            GenderEnum gender = GenderEnum.valueOf(genderStr.toUpperCase());

            if (selectedCustomer == null) {
                Customer newCustomer = new Customer(0, fullName, gender, phone, address, email, loyaltyPoints, regDate);
                customerService.addCustomer(newCustomer);
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Thêm khách hàng thành công.");
            } else {
                selectedCustomer.setFullName(fullName);
                selectedCustomer.setPhone(phone);
                selectedCustomer.setEmail(email);
                selectedCustomer.setGender(gender);
                selectedCustomer.setAddress(address);
                selectedCustomer.setPoint(loyaltyPoints);
                selectedCustomer.setCreated_at(regDate);

                customerService.updateCustomer(selectedCustomer);
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Cập nhật khách hàng thành công.");
            }

            formBox.setVisible(false);
            formBox.setManaged(false);
            loadCustomerData();
            clearForm();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", e.getMessage());
        }
    }

    private void clearForm() {
        txtFullName.clear();
        txtPhone.clear();
        txtEmail.clear();
        cmbGender.setValue(null);
        txtAddress.clear();
        txtLoyaltyPoints.clear();
        dpRegistrationDate.setValue(null);
    }

    private void showCustomerInForm(Customer customer) {
        selectedCustomer = customer;
        txtFullName.setText(customer.getFullName());
        txtPhone.setText(customer.getPhone());
        txtEmail.setText(customer.getEmail());
        cmbGender.setValue(customer.getGender().toString());
        txtAddress.setText(customer.getAddress());
        txtLoyaltyPoints.setText(String.valueOf(customer.getPoint()));
        if (customer.getCreated_at() != null) {
            dpRegistrationDate.setValue(new java.sql.Date(customer.getCreated_at().getTime()).toLocalDate());
        } else {
            dpRegistrationDate.setValue(null);
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
