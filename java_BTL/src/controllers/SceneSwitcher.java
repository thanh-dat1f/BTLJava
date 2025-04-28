package controllers;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

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
        mainStage.setTitle("BestPets");
        
        // Thiết lập icon cho ứng dụng
        try {
            mainStage.getIcons().add(new Image(SceneSwitcher.class.getResourceAsStream("/images/logo.png")));
        } catch (Exception e) {
            System.err.println("Không thể tải logo: " + e.getMessage());
        }
    }

    /**
     * Chuyển đổi màn hình theo file FXML
     * @param fxmlFile Đường dẫn file FXML (tương đối từ thư mục view)
     */
    public static void switchScene(String fxmlFile) {
        try {
            // Sử dụng getResource để đảm bảo đường dẫn chính xác
            URL fxmlUrl = SceneSwitcher.class.getResource("/view/" + fxmlFile);
            
            if (fxmlUrl == null) {
                throw new IOException("Không tìm thấy file FXML: " + fxmlFile);
            }
            
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            
            // Thiết lập kích thước tối thiểu cho cửa sổ
            double minWidth = 800; // Chiều rộng tối thiểu
            double minHeight = 600; // Chiều cao tối thiểu

            // Thiết lập scene mới
            Scene scene = new Scene(root);
            mainStage.setMinWidth(minWidth);
            mainStage.setMinHeight(minHeight);
            mainStage.setResizable(true);
            mainStage.setScene(scene);
            mainStage.show();
        } catch (IOException e) {
            System.err.println("Lỗi khi chuyển màn hình: " + e.getMessage());
            e.printStackTrace();
            
            // Hiển thị thông báo lỗi chi tiết
            showErrorAlert("Lỗi Chuyển Màn Hình", "Không thể tải giao diện: " + fxmlFile);
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
}