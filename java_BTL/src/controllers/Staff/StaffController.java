package controllers.Staff;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import model.Account;
import model.Staff;
import service.StaffService;
import controllers.SceneSwitcher;
import utils.RoleChecker; // Create this utility class if it doesn't exist
import service.AuthService; // Add this if missing
import utils.Session;
public class StaffController implements Initializable {

    @FXML
    private BorderPane mainContainer;
    
    @FXML
    private Label staffNameLabel;
    
    @FXML
    private Label staffRoleLabel;
    
    @FXML
    private Button scheduleButton;
    
    @FXML
    private Button bookingButton;
    
    @FXML
    private Button invoiceButton;
    
    @FXML
    private Button promotionButton;
    
    @FXML
    private Button profileButton;
    
    @FXML
    private Button logoutButton;
    
    private Staff currentStaff;
    private StaffService staffService;
    private AuthService authService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Khởi tạo các service
        staffService = new StaffService();
        authService = new AuthService();
        
        // Lấy thông tin nhân viên hiện tại
        Account currentAccount = Session.getInstance().getCurrentAccount();
        currentStaff = staffService.getStaffByAccountID(currentAccount.getAccountID());
        
        // Hiển thị thông tin nhân viên
        if (currentStaff != null) {
            staffNameLabel.setText(currentStaff.getFullName());
            staffRoleLabel.setText(currentStaff.getRole().getRoleName());
        }
        
        // Thiết lập hiển thị nút theo quyền
        setupButtonVisibility();
        
        // Tải màn hình chính
        loadHomeView();
    }
    
    /**
     * Thiết lập hiển thị các nút dựa trên quyền
     */
    private void setupButtonVisibility() {
        // Nút hồ sơ luôn hiển thị
        profileButton.setVisible(true);
        
        // Kiểm tra quyền và hiển thị/ẩn các nút
        scheduleButton.setVisible(RoleChecker.hasPermission("VIEW_SCHEDULE"));
        bookingButton.setVisible(
            RoleChecker.hasPermission("VIEW_BOOKING_ASSIGNED") || 
            RoleChecker.hasPermission("CREATE_BOOKING")
        );
        invoiceButton.setVisible(
            RoleChecker.hasPermission("VIEW_INVOICE") || 
            RoleChecker.hasPermission("MANAGE_PAYMENT")
        );
        promotionButton.setVisible(RoleChecker.hasPermission("APPLY_PROMOTION"));
    }
    
    /**
     * Tải màn hình chính của nhân viên
     */
    private void loadHomeView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/staff/staff_home.fxml"));
            AnchorPane view = loader.load();
            mainContainer.setCenter(view);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải màn hình chính", e.getMessage());
        }
    }
    
    /**
     * Xem lịch làm việc
     */
    @FXML
    private void viewSchedule(ActionEvent event) {
        if (!RoleChecker.hasPermission("VIEW_SCHEDULE")) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Không có quyền", 
                    "Bạn không có quyền xem lịch làm việc.");
            return;
        }
        
        loadView("/view/staff/my_schedule.fxml");
    }
    
    /**
     * Xem booking
     */
    @FXML
    private void viewBooking(ActionEvent event) {
        if (!RoleChecker.hasPermission("VIEW_BOOKING_ASSIGNED") && 
            !RoleChecker.hasPermission("CREATE_BOOKING")) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Không có quyền", 
                    "Bạn không có quyền xem danh sách đặt lịch.");
            return;
        }
        
        loadView("/view/staff/booking_view.fxml");
    }
    
    /**
     * Xem hóa đơn
     */
    @FXML
    private void viewInvoice(ActionEvent event) {
        if (!RoleChecker.hasPermission("VIEW_INVOICE") && 
            !RoleChecker.hasPermission("MANAGE_PAYMENT")) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Không có quyền", 
                    "Bạn không có quyền xem hóa đơn.");
            return;
        }
        
        loadView("/view/staff/invoice_view.fxml");
    }
    
    /**
     * Áp dụng khuyến mãi
     */
    @FXML
    private void applyPromotion(ActionEvent event) {
        if (!RoleChecker.hasPermission("APPLY_PROMOTION")) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Không có quyền", 
                    "Bạn không có quyền áp dụng khuyến mãi.");
            return;
        }
        
        loadView("/view/staff/promotion_apply.fxml");
    }
    
    /**
     * Chỉnh sửa hồ sơ
     */
    @FXML
    private void editProfile(ActionEvent event) {
        loadView("/view/staff/edit_profile.fxml");
    }
    
    /**
     * Tải và hiển thị view
     * @param fxmlPath Đường dẫn tới file FXML
     */
    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            AnchorPane view = loader.load();
            mainContainer.setCenter(view);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải màn hình", e.getMessage());
        }
    }
    
    /**
     * Đăng xuất
     */
    @FXML
    private void logout(ActionEvent event) {
        try {
            // Clear session
            Session.getInstance().clearSession();
            authService.logout();
            
            // Switch to login scene
            Stage currentStage = (Stage) logoutButton.getScene().getWindow();
            SceneSwitcher.switchToLoginScene(currentStage);
            
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể đăng xuất", e.getMessage());
        }
    }
    
    /**
     * Hiển thị thông báo
     */
    private void showAlert(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}