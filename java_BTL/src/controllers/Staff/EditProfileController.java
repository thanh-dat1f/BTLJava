package	controllers.Staff;


import java.net.URL;

import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import model.Account;
import model.Staff;
import service.AuthService;
import service.StaffService;
import utils.Session;

import model.Staff;
import enums.GenderEnum;

public class EditProfileController implements Initializable {

    @FXML
    private TextField fullNameField;
    
    @FXML
    private TextField emailField;
    
    @FXML
    private TextField phoneField;
    
    @FXML
    private PasswordField passwordField;
    
    @FXML
    private PasswordField confirmPasswordField;
    
    @FXML
    private Button updateProfileBtn;
    
    @FXML
    private Button changePasswordBtn;
    
    private StaffService staffService;
    private AuthService authService;
    
    public EditProfileController() {
        staffService = new StaffService();
        authService = new AuthService();
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadProfile();
    }
    
    private void loadProfile() {
        Account account = Session.getInstance().getCurrentAccount();
        if (account != null) {
            Staff staff = staffService.getStaffByAccountID(account.getAccountID());
            if (staff != null) {
                fullNameField.setText(staff.getFullName());
                emailField.setText(staff.getEmail());
                phoneField.setText(staff.getPhone());
            }
        }
    }

    // Update handleUpdateProfile() method to properly set fields
    @FXML
    private void handleUpdateProfile(ActionEvent event) {
        try {
            Account account = Session.getInstance().getCurrentAccount();
            Staff staff = staffService.getStaffByAccountID(account.getAccountID());
            
            // Set updated values
            staff.setFullName(fullNameField.getText());
            staff.setEmail(emailField.getText());
            staff.setPhone(phoneField.getText());
            
            // Validate input before updating
            staffService.validatePerson(staff);
            
            // Update staff
            boolean success = staffService.updateStaff(staff);
            
            if (success) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Thành công");
                alert.setContentText("Cập nhật hồ sơ thành công!");
                alert.showAndWait();
            } else {
                throw new Exception("Cập nhật không thành công");
            }
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Lỗi");
            alert.setContentText("Cập nhật thất bại: " + e.getMessage());
            alert.showAndWait();
        }
    }
    
    @FXML
    private void handleChangePassword(ActionEvent event) {
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        
        if (!password.equals(confirmPassword)) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Lỗi");
            alert.setContentText("Mật khẩu xác nhận không khớp!");
            alert.showAndWait();
            return;
        }
        
        try {
            Account account = Session.getInstance().getCurrentAccount();
            authService.changePassword(account.getAccountID(), password);
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Thành công");
            alert.setContentText("Đổi mật khẩu thành công!");
            alert.showAndWait();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Lỗi");
            alert.setContentText("Đổi mật khẩu thất bại: " + e.getMessage());
            alert.showAndWait();
        }
    }
}