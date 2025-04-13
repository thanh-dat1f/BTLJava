package controllers.admin;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import model.Account;
import model.Staff;
import service.AccountService;
import service.StaffService;
import utils.Session;
import org.mindrot.jbcrypt.BCrypt;
import exception.AccountException;

public class AccountController {

    @FXML private TextField usernameField, fullNameField, emailField, phoneField, roleField;
    @FXML private Button saveButton, cancelButton, changePasswordButton;
    @FXML private PasswordField currentPasswordField, newPasswordField, confirmPasswordField;
    @FXML
    private VBox  changePasswordBox;
    


    private Account currentAccount;
    private Staff currentStaff;
    private AccountService accountService = new AccountService();
    private final StaffService staffService = new StaffService();

    public void initialize() {
        currentAccount = Session.getCurrentUser();
        if (currentAccount != null) {
            currentStaff = staffService.getStaffByAccountID(currentAccount.getAccountID());
            loadAccountInfo();
        } else {
            showError("Không tìm thấy thông tin người dùng. Vui lòng đăng nhập lại!");
        }
    }

    private void loadAccountInfo() {
        fullNameField.setText(currentStaff.getFullName());
        usernameField.setText(currentAccount.getUserName());
        emailField.setText(currentStaff.getEmail());
        phoneField.setText(currentStaff.getPhone());
        roleField.setText(currentAccount.getRole().getRoleName());
        
        // Không cho chỉnh sửa các trường này
        usernameField.setEditable(false);
        emailField.setEditable(false);
        phoneField.setEditable(false);
        roleField.setDisable(true);  // Không cho thay đổi role
    }

    @FXML private void handleEdit() {
        usernameField.setEditable(true); // Cho phép chỉnh sửa username
        emailField.setEditable(true); // Cho phép chỉnh sửa email
        saveButton.setDisable(false);
        cancelButton.setDisable(false);
        roleField.setDisable(false);  // Cho phép chỉnh sửa role
    }

    @FXML private void handleSave() {
        if (currentAccount != null) {
            String username = usernameField.getText().trim();

            try {
              
                // Cập nhật thông tin tài khoản
                currentAccount.setUserName(username);

                boolean updatedAccount = accountService.updateAccount(currentAccount.getAccountID(), username, null, currentAccount.getRole());

                if (updatedAccount) {
                    showInfo("Cập nhật thông tin thành công!");
                } else {
                    showError("Cập nhật thất bại!");
                }
            } catch (IllegalArgumentException | AccountException e) {
                showError("Lỗi: " + e.getMessage());
            }

            resetEditMode();
        }
    }

    @FXML private void handleCancel() {
        loadAccountInfo();
        resetEditMode();
    }

    @FXML private void handleChangePassword() {
        // Khi nhấn vào nút thay đổi mật khẩu, kiểm tra thông tin mật khẩu
        String currentPassword = currentPasswordField.getText().trim();
        String newPassword = newPasswordField.getText().trim();
        String confirmPassword = confirmPasswordField.getText().trim();

        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showError("Vui lòng điền đầy đủ thông tin!");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            showError("Mật khẩu mới và xác nhận mật khẩu không khớp!");
            return;
        }

        try {
            boolean passwordMatch = BCrypt.checkpw(currentPassword, currentAccount.getPassword());
            if (!passwordMatch) {
                showError("Mật khẩu cũ không đúng!");
                return;
            }

            if (newPassword != null && !newPassword.trim().isEmpty()) {
                // Mã hóa mật khẩu mới trước khi lưu
                currentAccount.setPassword(BCrypt.hashpw(newPassword, BCrypt.gensalt()));
            }

            // Cập nhật mật khẩu mới vào tài khoản
            boolean passwordUpdated = accountService.updatePassword(currentAccount.getAccountID(), currentAccount.getPassword());
            if (passwordUpdated) {
                showInfo("Đổi mật khẩu thành công!");
            } else {
                showError("Đổi mật khẩu thất bại!");
            }
        } catch (Exception e) {
            showError("Lỗi: " + e.getMessage());
        }

        resetPasswordFields();
    }

    private void resetPasswordFields() {
        currentPasswordField.clear();
        newPasswordField.clear();
        confirmPasswordField.clear();
    }

    private void resetEditMode() {
        usernameField.setEditable(false); // Không cho phép chỉnh sửa khi không cần thiết
        emailField.setEditable(false);
        phoneField.setEditable(false);
        roleField.setDisable(true);

        saveButton.setDisable(true);
        cancelButton.setDisable(true);
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message);
        alert.showAndWait();
    }
    
    @FXML
    private void handleShowChangePassword() {
        // Khi người dùng nhấn nút "Đổi mật khẩu", hiển thị phần đổi mật khẩu
        changePasswordBox.setVisible(true);
    }

}
