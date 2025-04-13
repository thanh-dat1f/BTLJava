package repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import enums.StatusEnum;
import model.Customer;
import model.Order;
import model.Staff;
import utils.DBUtil;
import utils.DatabaseConnection;

public class OrderRepository implements IRepository<Order> {
    // Singleton pattern
    private static OrderRepository instance;

    public static OrderRepository getInstance() {
        if (instance == null) {
            synchronized (OrderRepository.class) {
                if (instance == null) {
                    instance = new OrderRepository();
                }
            }
        }
        return instance;
    }

    @Override
    public int insert(Order order) {
        String sql = "INSERT INTO `order` (customer_id, staff_id, order_date, status, total_amount, note) VALUES (?, ?, ?, ?, ?, ?)";
        
        Connection con = null;
        PreparedStatement pstmt = null;
        
        try {
            con = DatabaseConnection.getConnection();
            pstmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
            pstmt.setInt(1, order.getCustomer().getId());
            pstmt.setInt(2, order.getStaff() != null ? order.getStaff().getId() : 0);
            pstmt.setTimestamp(3, Timestamp.valueOf(order.getOrderDate()));
            pstmt.setString(4, order.getStatus().name());
            pstmt.setBigDecimal(5, order.getTotalAmount());
            pstmt.setString(6, order.getNote());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        order.setOrderId(rs.getInt(1));
                    }
                }
            }
            
            return affectedRows;
        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm đơn hàng: " + e.getMessage());
            return 0;
        } finally {
            DBUtil.closeResources(con, pstmt);
        }
    }

    @Override
    public int update(Order order) {
        String sql = "UPDATE `order` SET customer_id=?, staff_id=?, order_date=?, status=?, total_amount=?, note=? WHERE order_id=?";
        
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            
            pstmt.setInt(1, order.getCustomer().getId());
            pstmt.setInt(2, order.getStaff() != null ? order.getStaff().getId() : 0);
            pstmt.setTimestamp(3, Timestamp.valueOf(order.getOrderDate()));
            pstmt.setString(4, order.getStatus().name());
            pstmt.setBigDecimal(5, order.getTotalAmount());
            pstmt.setString(6, order.getNote());
            pstmt.setInt(7, order.getOrderId());
            
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật đơn hàng: " + e.getMessage());
            return 0;
        }
    }

    // Cập nhật tổng tiền của đơn hàng
    public boolean updateTotalPrice(int orderId) {
        String sql = "UPDATE `order` o " +
                     "SET total_amount = (" +
                     "    SELECT COALESCE(SUM(od.quantity * od.price), 0) " +
                     "    FROM order_detail od " +
                     "    WHERE od.order_id = o.order_id" +
                     ") " +
                     "WHERE o.order_id = ?";
        
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            
            pstmt.setInt(1, orderId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật tổng tiền đơn hàng: " + e.getMessage());
            return false;
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
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT o.*, c.*, s.*, st.* " +
                     "FROM `order` o " +
                     "JOIN customer c ON o.customer_id = c.customer_id " +
                     "LEFT JOIN staff s ON o.staff_id = s.staff_id " +
                     "LEFT JOIN person st ON s.staff_id = st.person_id";
        
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                orders.add(mapResultSetToOrder(rs));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy danh sách đơn hàng: " + e.getMessage());
        }
        
        return orders;
    }

    @Override
    public Order selectById(Order order) {
        return selectById(order.getOrderId());
    }

    public Order selectById(int orderId) {
        String sql = "SELECT o.*, c.*, s.*, st.* " +
                     "FROM `order` o " +
                     "JOIN customer c ON o.customer_id = c.customer_id " +
                     "LEFT JOIN staff s ON o.staff_id = s.staff_id " +
                     "LEFT JOIN person st ON s.staff_id = st.person_id " +
                     "WHERE o.order_id = ?";
        
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
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT o.*, c.*, s.*, st.* " +
                     "FROM `order` o " +
                     "JOIN customer c ON o.customer_id = c.customer_id " +
                     "LEFT JOIN staff s ON o.staff_id = s.staff_id " +
                     "LEFT JOIN person st ON s.staff_id = st.person_id " +
                     "WHERE " + condition;
        
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    orders.add(mapResultSetToOrder(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi truy vấn đơn hàng theo điều kiện: " + e.getMessage());
        }
        
        return orders;
    }

    // Ánh xạ dữ liệu từ ResultSet sang đối tượng Order
    private Order mapResultSetToOrder(ResultSet rs) throws SQLException {
        // Tạo khách hàng
        Customer customer = new CustomerRepository().selectById(rs.getInt("customer_id"));
        
        // Tạo nhân viên (nếu có)
        Staff staff = null;
        if (rs.getObject("staff_id") != null) {
            staff = new StaffRepository().selectById(rs.getInt("staff_id"));
        }
        
        // Tạo đối tượng Order
        return new Order(
            rs.getInt("order_id"),
            customer,
            staff,
            rs.getTimestamp("order_date").toLocalDateTime(),
            StatusEnum.valueOf(rs.getString("status")),
            rs.getBigDecimal("total_amount"),
            rs.getString("note")
        );
    }
}