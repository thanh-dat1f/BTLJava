package controllers;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Lớp tiện ích để chuyển đổi giữa các màn hình
 */
public class SceneSwitcher {
    private static Stage mainStage;

    /**
     * Thiết lập stage chính cho ứng dụng
     * @param stage Stage chính
     */
    public static void setMainStage(Stage stage) {
        mainStage = stage;
    }

    /**
     * Chuyển đổi màn hình theo file FXML
     * @param fxmlFile Đường dẫn file FXML (tương đối với thư mục view)
     */
    public static void switchScene(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneSwitcher.class.getResource("/view/" + fxmlFile));
            Parent root = loader.load();
            
            // Thiết lập tiêu đề và biểu tượng
            mainStage.setTitle("BESTPETS");
            try {
                mainStage.getIcons().add(new Image(SceneSwitcher.class.getResourceAsStream("/images/logo.png")));
            } catch (Exception e) {
                System.err.println("Không thể tải logo: " + e.getMessage());
            }
            
            // Thiết lập kích thước tối thiểu cho cửa sổ
            double minWidth = 800; // Chiều rộng tối thiểu
            double minHeight = 600; // Chiều cao tối thiểu

            // Thiết lập kích thước
            mainStage.setMinWidth(minWidth);
            mainStage.setMinHeight(minHeight);
            mainStage.setResizable(true);
            
            // Thiết lập scene mới
            Scene scene = new Scene(root);
            mainStage.setScene(scene);
            mainStage.show();
        } catch (IOException e) {
            showErrorAlert("Lỗi khi chuyển màn hình", e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            showErrorAlert("Lỗi không xác định", e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Chuyển đến màn hình đăng nhập
     * @param currentStage Stage hiện tại
     */
    public static void switchToLoginScene(Stage currentStage) {
        if (mainStage != null) {
            switchScene("login.fxml");
        } else if (currentStage != null) {
            // Nếu chưa có mainStage, sử dụng currentStage
            mainStage = currentStage;
            switchScene("login.fxml");
        } else {
            showErrorAlert("Lỗi", "Không thể chuyển đến màn hình đăng nhập do không có stage hợp lệ.");
        }
    }
    
    /**
     * Chuyển đến màn hình chi tiết đặt lịch
     * @param currentStage Stage hiện tại
     * @param bookingId ID của lịch đặt cần xem chi tiết
     */
    public static void switchToBookingDetailScene(Stage currentStage, int bookingId) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneSwitcher.class.getResource("/view/staff/booking_detail.fxml"));
            Parent root = loader.load();
            
            // Truyền ID booking vào controller
            // BookingDetailController controller = loader.getController();
            // controller.loadBookingDetails(bookingId);
            
            // Hiện tại chỉ hiển thị thông báo
            showInfoAlert("Chức năng đang phát triển", 
                "Chức năng xem chi tiết đặt lịch đang được phát triển. ID đặt lịch: " + bookingId);
            
        } catch (IOException e) {
            showErrorAlert("Lỗi khi mở chi tiết đặt lịch", e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Chuyển đến màn hình xử lý hóa đơn
     * @param currentStage Stage hiện tại
     * @param bookingId ID của lịch đặt cần xử lý hóa đơn
     */
    public static void switchToInvoiceScene(Stage currentStage, int bookingId) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneSwitcher.class.getResource("/view/staff/create_invoice.fxml"));
            Parent root = loader.load();
            
            // Truyền ID booking vào controller
            // CreateInvoiceController controller = loader.getController();
            // controller.loadBookingData(bookingId);
            
            // Hiện tại chỉ hiển thị thông báo
            showInfoAlert("Chức năng đang phát triển", 
                "Chức năng tạo hóa đơn đang được phát triển. ID đặt lịch: " + bookingId);
            
        } catch (IOException e) {
            showErrorAlert("Lỗi khi mở màn hình hóa đơn", e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Chuyển đến màn hình chi tiết hóa đơn
     * @param currentStage Stage hiện tại
     * @param invoiceId ID của hóa đơn cần xem chi tiết
     */
    public static void switchToInvoiceDetailScene(Stage currentStage, int invoiceId) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneSwitcher.class.getResource("/view/staff/invoice_detail.fxml"));
            Parent root = loader.load();
            
            // Truyền ID hóa đơn vào controller
            // InvoiceDetailController controller = loader.getController();
            // controller.loadInvoiceDetails(invoiceId);
            
            // Hiện tại chỉ hiển thị thông báo
            showInfoAlert("Chức năng đang phát triển", 
                "Chức năng xem chi tiết hóa đơn đang được phát triển. ID hóa đơn: " + invoiceId);
            
        } catch (IOException e) {
            showErrorAlert("Lỗi khi mở chi tiết hóa đơn", e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Hiển thị thông báo lỗi
     * @param title Tiêu đề thông báo
     * @param message Nội dung thông báo
     */
    private static void showErrorAlert(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
            javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Hiển thị thông báo thông tin
     * @param title Tiêu đề thông báo
     * @param message Nội dung thông báo
     */
    private static void showInfoAlert(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
            javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}