package repository;

import utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import enums.StatusEnum;
import model.Customer;
import model.Order;
import model.Promotion;
import model.Staff;

public class OrderRepository implements IRepository<Order> {
    
    public static OrderRepository getInstance() {
        return new OrderRepository();
    }

    public void updateTotal(int orderId, double total) {
        String sql = "UPDATE `order` SET total_amount = ? WHERE order_id = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setDouble(1, total);
            pstmt.setInt(2, orderId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật tổng tiền: " + e.getMessage());
        }
    }
    public void updateTotalPrice(int orderId) {
        String sql = "SELECT SUM(price * quantity) FROM order_detail WHERE order_id = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setInt(1, orderId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    double total = rs.getDouble(1);
                    updateTotal(orderId, total);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật tổng tiền: " + e.getMessage());
        }
    }


    public int insert(Order order) {
        String sql = "INSERT INTO `order` (customer_id, staff_id, order_date, voucher_code, total_amount, status) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, order.getCustomer().getId());
            if (order.getStaff() != null)
                pstmt.setInt(2, order.getStaff().getId());
            else
                pstmt.setNull(2, Types.INTEGER);

            pstmt.setTimestamp(3, order.getOrderDate());
            if (order.getVoucher() != null)
                pstmt.setString(4, order.getVoucher().getDescription());
            else
                pstmt.setNull(4, Types.VARCHAR);
            pstmt.setDouble(5, 0.0); // total_amount ban đầu
            pstmt.setString(6, order.getStatus().name());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        order.setOrderId(id);
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm order: " + e.getMessage());
        }
        return 0;
    }




    @Override
    public int update(Order order) {
        String sql = "UPDATE `order` SET customer_id=?, staff_id=?, order_date=?, voucher_code=?, total_amount=?, status=? WHERE order_id=?";
        
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            
			 pstmt.setInt(1, order.getCustomer().getId());
			 if (order.getStaff() != null)
			     pstmt.setInt(2, order.getStaff().getId());
			 else
			     pstmt.setNull(2, Types.INTEGER);
			
			 pstmt.setTimestamp(3, order.getOrderDate());
			 if (order.getVoucher() != null)
				    pstmt.setString(4, order.getVoucher().getDescription());
				else
				    pstmt.setNull(4, Types.VARCHAR);
			 pstmt.setDouble(5, 0.0); // total_amount ban đầu
			 pstmt.setString(6, order.getStatus().name());
             pstmt.setInt(7, order.getOrderId());
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println(" Cập nhật đơn hàng thành công! OrderID = " + order.getOrderId());
                
                // Cập nhật lại tổng tiền của đơn hàng sau khi update
                updateTotalPrice(order.getOrderId());
            }
            return affectedRows;
        } catch (SQLException e) {
            System.err.println(" Lỗi khi cập nhật đơn hàng: " + e.getMessage());
            return 0;
        }
    }


    @Override
    public int delete(Order order) {
    	String sql = "DELETE FROM `order` WHERE order_id=?";
        
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            
            pstmt.setInt(1, order.getOrderId());
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Lỗi khi xóa đơn hàng: " + e.getMessage());
            return 0;
        }
    }

    @Override
    public List<Order> selectAll() {
        List<Order> list = new ArrayList<>();
        String sql = "SELECT * FROM `order`";
        
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                list.add(mapResultSetToOrder(rs));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy danh sách đơn hàng: " + e.getMessage());
        }
        return list;
    }

    @Override
    public Order selectById(Order order) {
        return selectById(order.getOrderId());
    }
    
    public Order selectById(int orderId) {
        String sql = "SELECT * FROM `order` WHERE order_id = ?";
        
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            
            pstmt.setInt(1, orderId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToOrder(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm đơn hàng theo ID: " + e.getMessage());
        }
        return null;
    }
    
    @Override
    public List<Order> selectByCondition(String condition, Object... params) {
        List<Order> list = new ArrayList<>();
        String sql = "SELECT * FROM `order` WHERE " + condition;
        
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToOrder(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi truy vấn theo điều kiện: " + e.getMessage());
        }
        return list;
    }

    private Order mapResultSetToOrder(ResultSet rs) throws SQLException {
        int orderId = rs.getInt("order_id");
        Timestamp orderDate = rs.getTimestamp("order_date");
        double total = rs.getDouble("total_amount");
        

        Customer customer = new Customer();
        customer.setId(rs.getInt("customer_id"));

        Staff staff = null;
        if (rs.getObject("staff_id") != null) {
            staff = new Staff();
            staff.setId(rs.getInt("staff_id"));
        }
        
        String voucherCode = rs.getString("voucher_code");
        Promotion voucher = new Promotion();
        voucher.setDescription(voucherCode); 


        String statusStr = rs.getString("status");
        StatusEnum status = StatusEnum.valueOf(statusStr);

        return new Order(orderId, customer, staff, orderDate, voucher, total, status);
    }
}