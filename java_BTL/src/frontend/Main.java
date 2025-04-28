package frontend;

import controllers.SceneSwitcher;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        try {
            SceneSwitcher.setMainStage(primaryStage);
            
            // Bạn có thể chọn một trong các dòng sau để hiển thị màn hình mong muốn khi khởi động
            SceneSwitcher.switchScene("home.fxml"); // Màn hình mặc định
             //SceneSwitcher.switchScene("staff/booking_view.fxml"); // Màn hình đặt lịch
            // SceneSwitcher.switchScene("staff/my_schedule.fxml"); // Màn hình lịch làm việc
             //SceneSwitcher.switchScene("staff/invoice_view.fxml"); // Màn hình hóa đơn
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
   
    public static void main(String[] args) {
        launch(args);
    }
}