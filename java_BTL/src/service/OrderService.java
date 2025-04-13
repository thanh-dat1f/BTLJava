package service;

import model.Customer;

//import model.HappenStatus;
import model.Order;
import model.Staff;
import repository.OrderRepository;
import utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import enums.TypeOrder;

public class OrderService {
	private final OrderRepository orderRepository;

    public OrderService() {
        this.orderRepository = OrderRepository.getInstance();
    }
	public List<Order> getTodayOrders() {
	    List<Order> orders = new ArrayList<>();
	    String sql = "SELECT o.orderID, o.orderDate, o.appointmentDate, o.orderType, o.Total, " +
	                 "o.Customer_ID, o.StaffID, o.HappenStatusID, p.PetName, s.serviceName " +
	                 "FROM `order` o " +
	                 "LEFT JOIN order_detail od ON o.orderID = od.OrderID " +
	                 "LEFT JOIN service s ON od.ServiceID = s.serviceID " +
	                 "LEFT JOIN pet p ON p.Customer_ID = o.Customer_ID " +
	                 "WHERE DATE(o.appointmentDate) = CURRENT_DATE AND o.orderType = 'Appointment'";
	    
	    try (Connection conn = DatabaseConnection.getConnection();
	         PreparedStatement stmt = conn.prepareStatement(sql);
	         ResultSet rs = stmt.executeQuery()) {
	        
	        while (rs.next()) {
	            Customer customer = new Customer();
	            customer.setId(rs.getInt("Customer_ID"));
	            
	            Staff staff = new Staff();
	            staff.setId(rs.getInt("StaffID"));
	            
	            HappenStatus status = new HappenStatus();
	            status.setHappenStatusID(rs.getInt("HappenStatusID"));
	            
	            Order order = new Order(
	                rs.getInt("orderID"),
	                rs.getTimestamp("orderDate"),
	                rs.getTimestamp("appointmentDate"),
	                TypeOrder.valueOf(rs.getString("orderType")),
	                rs.getDouble("Total"),
	                customer,
	                staff,
	                status
	            );
	            
	            orders.add(order);
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return orders;
	}
    public void createOrder(String customerPhone, int serviceID, int staffID, LocalDateTime appointmentDate) {
        // Tìm Customer_ID từ phoneNumber
        int customerID = getCustomerIDByPhone(customerPhone);
        if (customerID == 0) {
            // TODO: Tạo khách hàng mới nếu không tồn tại
            System.out.println("Khách hàng không tồn tại!");
            return;
        }

        // Tạo order
        String orderSql = "INSERT INTO `order` (orderDate, appointmentDate, orderType, Total, Customer_ID, StaffID, HappenStatusID) " +
                         "VALUES (NOW(), ?, 'Appointment', 0, ?, ?, 1)";
        String orderDetailSql = "INSERT INTO order_detail (OrderID, ServiceID, Quantity, UnitPrice) VALUES (?, ?, 1, ?)";
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement orderStmt = conn.prepareStatement(orderSql, PreparedStatement.RETURN_GENERATED_KEYS);
                 PreparedStatement detailStmt = conn.prepareStatement(orderDetailSql)) {
                // Tạo order
                orderStmt.setObject(1, appointmentDate);
                orderStmt.setInt(2, customerID);
                orderStmt.setInt(3, staffID);
                orderStmt.executeUpdate();

                ResultSet rs = orderStmt.getGeneratedKeys();
                int orderID = 0;
                if (rs.next()) {
                    orderID = rs.getInt(1);
                }

                // Tạo order_detail
                double unitPrice = getServicePrice(serviceID);
                detailStmt.setInt(1, orderID);
                detailStmt.setInt(2, serviceID);
                detailStmt.setDouble(3, unitPrice);
                detailStmt.executeUpdate();

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private int getCustomerIDByPhone(String phone) {
        String sql = "SELECT c.PersonID FROM customer c JOIN person p ON c.PersonID = p.PersonID WHERE p.phoneNumber = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, phone);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("PersonID");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private double getServicePrice(int serviceID) {
        String sql = "SELECT CostPrice FROM service WHERE serviceID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, serviceID);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("CostPrice");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }
    
 // Lấy tất cả các đơn hàng
    public List<Order> getAllOrders() {
        return orderRepository.selectAll();
    }

    // Lấy đơn hàng theo ID
    public Order getOrderById(int orderId) {
        return orderRepository.selectById(orderId);
    }

    // Lấy đơn hàng theo điều kiện
    public List<Order> getOrdersByCondition(String condition, Object... params) {
        return orderRepository.selectByCondition(condition, params);
    }

    // Thêm một đơn hàng mới
    public void addOrder(Order order) {
        int rowsAffected = orderRepository.insert(order);
        if (rowsAffected > 0) {
            System.out.println("Đơn hàng đã được thêm thành công với ID: " + order.getOrderId());
        }
    }

    // Cập nhật thông tin đơn hàng
    public void updateOrder(Order order) {
        int rowsAffected = orderRepository.update(order);
        if (rowsAffected > 0) {
            System.out.println("Đơn hàng đã được cập nhật thành công với ID: " + order.getOrderId());
        }
    }

    // Xóa đơn hàng
    public void deleteOrder(Order order) {
        int rowsAffected = orderRepository.delete(order);
        if (rowsAffected > 0) {
            System.out.println("Đơn hàng đã được xóa thành công với ID: " + order.getOrderId());
        }
    }

    // Cập nhật tổng tiền của đơn hàng sau khi thay đổi chi tiết đơn hàng
    public void updateOrderTotal(int orderId) {
        orderRepository.updateTotalPrice(orderId);
    }
}

