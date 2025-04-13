package controllers.admin;

import javafx.fxml.FXML;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import model.Account;
import model.Role;
import model.Staff;
import repository.StaffRepository;
import enums.GenderEnum;

import java.time.LocalDate;

public class StaffController {

    @FXML private TextField txtFullName;
    @FXML private ComboBox<String> cmbGender;
    @FXML private TextField txtPhone;
    @FXML private TextField txtCitizenNumber;
    @FXML private TextField txtAddress;
    @FXML private TextField txtEmail;
    @FXML private ComboBox<String> cmbRole;
    @FXML private DatePicker dpStartDate;
    @FXML private DatePicker dpEndDate;
    @FXML private TextField txtSalary;
    @FXML private ComboBox<String> cmbWorkShift;
    @FXML private TextField txtPosition;
    
    @FXML private TableView<Staff> staffTable;
    @FXML private TableColumn<Staff, String> fullNameColumn;
    @FXML private TableColumn<Staff, String> genderColumn;
    @FXML private TableColumn<Staff, String> phoneColumn;
    @FXML private TableColumn<Staff, String> emailColumn;
    @FXML private TableColumn<Staff, String> roleColumn;
    @FXML private TableColumn<Staff, LocalDate> startDateColumn;
    @FXML private TableColumn<Staff, LocalDate> endDateColumn;
    @FXML private TableColumn<Staff, Double> salaryColumn;
    @FXML private TableColumn<Staff, String> positionColumn;

    private StaffRepository staffRepo = new StaffRepository();

    public void initialize() {
        // Khởi tạo các ComboBox với dữ liệu
        cmbGender.setItems(FXCollections.observableArrayList("Male", "Female", "Other"));
        cmbRole.setItems(FXCollections.observableArrayList("Admin", "Employee"));
        cmbWorkShift.setItems(FXCollections.observableArrayList("Ca sáng", "Ca chiều", "Ca tối"));

        // Load dữ liệu nhân viên ban đầu
        loadStaffData();

        // Bind các cột với thuộc tính tương ứng trong Staff
        fullNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFullName()));
        genderColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getGender().toString()));
        phoneColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPhone()));
        emailColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEmail()));
        roleColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getRole().getRoleName()));
        
        // Sử dụng SimpleObjectProperty cho các cột kiểu LocalDate
        startDateColumn.setCellValueFactory(cellData -> 
            new SimpleObjectProperty<>(cellData.getValue().getHire_date()));

        salaryColumn.setCellValueFactory(cellData -> 
        new SimpleObjectProperty<>(cellData.getValue().getSalary()));        
        positionColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPosition()));
    }

    private void loadStaffData() {
        // Giả sử có phương thức lấy danh sách nhân viên từ repository
        staffTable.getItems().setAll(staffRepo.selectAll());
    }

    @FXML
    private void handleAddStaff() {
        // Kiểm tra đầu vào
        if ( txtFullName.getText().isEmpty()) {
            showAlert("Error", "First Name and Last Name are required.");
            return;
        }
        
        if (cmbGender.getValue() == null) {
            showAlert("Error", "Please select gender.");
            return;
        }
        
        if (cmbRole.getValue() == null) {
            showAlert("Error", "Please select role.");
            return;
        }

        if (txtSalary.getText().isEmpty()) {
            showAlert("Error", "Salary is required.");
            return;
        }
        
        double salary = 0;
        try {
            salary = Double.parseDouble(txtSalary.getText());
        } catch (NumberFormatException e) {
            showAlert("Error", "Invalid salary input.");
            return;
        }

        // Lấy dữ liệu từ các trường nhập liệu
        String fullName = txtFullName.getText();
        GenderEnum gender = "Male".equalsIgnoreCase(cmbGender.getValue()) ? GenderEnum.MALE
                          : "Female".equalsIgnoreCase(cmbGender.getValue()) ? GenderEnum.FEMALE
                          : GenderEnum.OTHER;
        String phone = txtPhone.getText();
        String citizenNumber = txtCitizenNumber.getText();
        String address = txtAddress.getText();
        String email = txtEmail.getText();
        Account account = null;  // Giả sử không cần Account trong ví dụ này
        Role role = "Admin".equalsIgnoreCase(cmbRole.getValue()) ? new Role(1, "Admin")
                    : new Role(2, "Employee");
        LocalDate startDate = dpStartDate.getValue();
        LocalDate endDate = dpEndDate.getValue();
        String workShift = cmbWorkShift.getValue();
        String position = txtPosition.getText();

        // Tạo đối tượng Staff mới
        Staff newStaff = new Staff(
            0,
            fullName,
            gender,
            phone,
            address,
            email,
            account,
            role,
            startDate,
            endDate,
            salary,
            workShift,
            position
        );

        int result = staffRepo.insert(newStaff);
        if (result > 0) {
            loadStaffData();
        } else {
            showAlert("Error", "Failed to add staff!");
        }
    }

    @FXML
    private void handleEditStaff() {
        Staff selectedStaff = staffTable.getSelectionModel().getSelectedItem();
        if (selectedStaff != null) {
            // Xử lý sửa thông tin nhân viên
            System.out.println("Editing staff: " + selectedStaff.getFullName());
            // Chuyển đến màn hình sửa (nếu có)
        } else {
            showAlert("Error", "No staff selected for editing.");
        }
    }

    @FXML
    private void handleDeleteStaff() {
        Staff selectedStaff = staffTable.getSelectionModel().getSelectedItem();
        if (selectedStaff != null) {
            int result = staffRepo.delete(selectedStaff);
            if (result > 0) {
                loadStaffData();
            } else {
                showAlert("Error", "Failed to delete staff.");
            }
        } else {
            showAlert("Error", "No staff selected for deletion.");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
