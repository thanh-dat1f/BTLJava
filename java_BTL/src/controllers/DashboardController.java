package controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import model.Account;
import model.Role;
import utils.Session;

/**
 * Controller cho màn hình Dashboard chính
 */
public class DashboardController {

    @FXML
    private Label lblWelcome;
    
    @FXML
    private Button btnAdminPanel;
    
    @FXML
    private Button btnEmployeePanel;
    
    @FXML
    private Button btnLogout;

    /**
     * Phương thức khởi tạo, được gọi khi FXML được load
     */
    @FXML
    public void initialize() {
        System.out.println("Initializing Dashboard Controller");
        
        // Lấy thông tin người dùng hiện tại
        Account currentUser = Session.getCurrentUser();
        System.out.println("Current User: " + currentUser);
        
        if (currentUser != null) {
            Role role = currentUser.getRole();
            lblWelcome.setText("🐾 Xin chào, " + currentUser.getUserName());
            
            // Sử dụng Platform.runLater để đảm bảo giao diện được cập nhật sau khi đăng nhập thành công
            Platform.runLater(() -> {
                // Ẩn tất cả các nút mặc định
                btnAdminPanel.setVisible(false);
                btnEmployeePanel.setVisible(false);
                
                if (role != null) {
                    switch (role.getRoleID()) {
                        case 1: // Admin
                            btnAdminPanel.setVisible(true); // Hiển thị nút quản trị viên
                            break;
                        case 2: // Nhân viên chăm sóc
                        case 3: // Nhân viên thu ngân
                        case 4: // Nhân viên lễ tân
                            btnEmployeePanel.setVisible(true); // Hiển thị nút nhân viên
                            break;
                        default:
                            lblWelcome.setText("Vai trò không xác định, vui lòng đăng nhập lại!");
                            break;
                    }
                } else {
                    lblWelcome.setText("Không xác định được vai trò, vui lòng đăng nhập lại!");
                }
            });
        } else {
            lblWelcome.setText("Vui lòng đăng nhập!");
            
            // Nếu chưa đăng nhập, chuyển về trang đăng nhập
            Platform.runLater(() -> {
                SceneSwitcher.switchScene("login.fxml");
            });
        }

        // Sự kiện đăng xuất
        btnLogout.setOnAction(event -> handleLogout());
    }

    /**
     * Chuyển đến trang Admin
     */
    @FXML
    private void handleAdminPanel() {
        SceneSwitcher.switchScene("admin/adminDashboard.fxml");
    }

    /**
     * Chuyển đến trang Employee
     */
    @FXML
    private void handleEmployeePanel() {
        SceneSwitcher.switchScene("staff/Staff.fxml");
    }

    /**
     * Đăng xuất
     */
    @FXML
    private void handleLogout() {
        Session.logout();
        SceneSwitcher.switchScene("login.fxml");
    }
}