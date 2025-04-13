// src/frontend1/controller/CashierDashboardController.java
package controllers;

import javafx.fxml.FXML;

import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import service.OrderService;
import model.Order;

public class CashierDashboardController {

    @FXML private Label cashierNameLabel;
    @FXML private TableView<Order> bookingTable;
    @FXML private TableColumn<Order, String> petColumn;
    @FXML private TableColumn<Order, String> serviceColumn;
    //@FXML private TableColumn<Order, LocalDateTime> timeColumn;
    @FXML private TableColumn<Order, Integer> statusColumn;

    private OrderService orderService;

    @FXML
    public void initialize() {
        // Khởi tạo dịch vụ
        orderService = new OrderService();

        // Thiết lập cột cho bảng
        petColumn.setCellValueFactory(new PropertyValueFactory<>("petName"));
        serviceColumn.setCellValueFactory(new PropertyValueFactory<>("serviceName"));
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("appointmentDate"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("happenStatusID"));

        // Tải danh sách lịch hẹn
        loadBookings();

        // Hiển thị tên thu ngân (giả định từ thông tin đăng nhập)
        cashierNameLabel.setText("Thu Ngân: " + getLoggedInCashierName());
    }

    private void loadBookings() {
        bookingTable.getItems().setAll(orderService.getTodayOrders());
    }

    @FXML
    private void handleSearch() {
        // Mở popup tra cứu khách hàng
        // Để trống vì không tập trung vào tra cứu
        System.out.println("Mở tra cứu khách hàng...");
    }

    @FXML
    private void handleBook() {
        // Mở popup đặt lịch
        QuickBookingDialogg dialog = new QuickBookingDialogg();
        dialog.show();
        // Tải lại danh sách lịch hẹn sau khi đặt lịch
        loadBookings();
    }

    @FXML
    private void handlePay() {
        // Mở màn hình thanh toán
        // Để trống vì không tập trung vào thanh toán
        System.out.println("Mở thanh toán...");
    }

    @FXML
    private void handlePrint() {
        // In hóa đơn
        // Để trống vì không tập trung vào in hóa đơn
        System.out.println("In hóa đơn...");
    }

    @FXML
    private void handleLogout() {
        // Đăng xuất và quay lại màn hình đăng nhập
        Stage stage = (Stage) cashierNameLabel.getScene().getWindow();
        stage.close();
        // TODO: Chuyển hướng về màn hình đăng nhập
    }

    private String getLoggedInCashierName() {
        // Giả định lấy từ thông tin đăng nhập
        return "Nguyen Van A";
    }
}