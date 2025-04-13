package controllers.admin;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import java.io.IOException;

public class AdminDashboardController {

    @FXML private Button btnManageStaff;
    @FXML private Button btnManageCustomers;
    @FXML private Button btnManageServices;
    @FXML private Button btnManageAccount;
    @FXML private VBox mainContent;

    @FXML
    private void initialize() {
        // Thêm sự kiện cho các nút
        btnManageStaff.setOnAction(e -> loadContent("admin/ManageStaff.fxml"));
        btnManageCustomers.setOnAction(e -> loadContent("admin/ManageCustomer.fxml"));
        btnManageServices.setOnAction(e -> loadContent("admin/ManageService.fxml"));
        btnManageAccount.setOnAction(e -> loadContent("admin/ManageAccount.fxml"));
    }

    // Phương thức load nội dung vào mainContent
    private void loadContent(String fxmlPath) {
        try {
            Node node = FXMLLoader.load(getClass().getResource("/view/" + fxmlPath));
            mainContent.getChildren().setAll(node);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
