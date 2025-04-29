
package repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import enums.GenderEnum;
import exception.BusinessException;
import model.Customer;
import utils.DBUtil;
import utils.DatabaseConnection;

public class CustomerRepository implements IRepository<Customer> {

    public static CustomerRepository getInstance() {
        return new CustomerRepository();
    }

    @Override
    public int insert(Customer t) {
        int ketQua = 0;

        String insertPersonSql = "INSERT INTO person (full_name, gender, phone, address, email) VALUES (?, ?, ?, ?, ?)";
        String customerSql = "INSERT INTO customer (customer_id, point, created_at) VALUES (?, ?, ?)";

        Connection con = null;
        PreparedStatement personPstmt = null;
        PreparedStatement customerPstmt = null;
        ResultSet personRs = null;

        try {
            con = DatabaseConnection.getConnection();
            con.setAutoCommit(false);

            personPstmt = con.prepareStatement(insertPersonSql, Statement.RETURN_GENERATED_KEYS);
            personPstmt.setString(1, t.getFullName());
            personPstmt.setString(2, t.getGender().name());
            personPstmt.setString(3, t.getPhone());
            personPstmt.setString(4, t.getAddress());
            personPstmt.setString(5, t.getEmail());

            int personRowsAffected = personPstmt.executeUpdate();
            if (personRowsAffected > 0) {
                personRs = personPstmt.getGeneratedKeys();
                if (personRs.next()) {
                    int personID = personRs.getInt(1);

                    customerPstmt = con.prepareStatement(customerSql);
                    customerPstmt.setInt(1, personID);
                    customerPstmt.setInt(2, t.getPoint());
                    customerPstmt.setTimestamp(3, t.getCreated_at());

                    ketQua = customerPstmt.executeUpdate();

                    if (ketQua > 0) {
                        t.setId(personID);
                    }

                    con.commit();
                }
            }

        } catch (SQLException e) {
            try {
                if (con != null) con.rollback();
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
            throw new BusinessException("Lỗi khi thêm khách hàng: " + e.getMessage());

        } finally {
            DBUtil.closeResources(null, customerPstmt, null);
            DBUtil.closeResources(null, personPstmt, personRs);
            DatabaseConnection.closeConnection(con);
        }

        return ketQua;
    }

    @Override
    public int update(Customer t) {
        int ketQua = 0;
        String updatePersonSql = "UPDATE person SET full_name=?, gender=?, phone=?, address=?, email=? WHERE person_id=?";
        String updateCustomerSql = "UPDATE customer SET point=?, created_at=? WHERE customer_id=?";

        Connection con = null;

        try {
            con = DatabaseConnection.getConnection();
            con.setAutoCommit(false);

            try (PreparedStatement pstmt = con.prepareStatement(updatePersonSql)) {
                pstmt.setString(1, t.getFullName());
                pstmt.setString(2, t.getGender().name());
                pstmt.setString(3, t.getPhone());
                pstmt.setString(4, t.getAddress());
                pstmt.setString(5, t.getEmail());
                pstmt.setInt(6, t.getId());

                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Person update thành công.");
                }
            }

            try (PreparedStatement pstmt = con.prepareStatement(updateCustomerSql)) {
                pstmt.setInt(1, t.getPoint());
                pstmt.setTimestamp(2, t.getCreated_at());
                pstmt.setInt(3, t.getId());

                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Customer update thành công.");
                }
            }

            con.commit();
            ketQua = 1;
        } catch (SQLException e) {
            try {
                if (con != null) {
                    con.rollback();
                }
            } catch (SQLException rollbackEx) {
                throw new BusinessException("Lỗi khi rollback giao dịch: " + rollbackEx.getMessage());
            }
            if (e.getMessage().contains("Duplicate entry")) {
                throw new BusinessException("Email hoặc số điện thoại đã tồn tại.");
            }
            throw new BusinessException("Lỗi SQL khi cập nhật khách hàng: " + e.getMessage());
        } finally {
            try {
                if (con != null) {
                    con.setAutoCommit(true);
                }
            } catch (SQLException e) {
                throw new BusinessException("Lỗi khi reset auto-commit: " + e.getMessage());
            }
            DatabaseConnection.closeConnection(con);
        }

        return ketQua;
    }

    @Override
    public int delete(Customer t) {
        int ketQua = 0;
        String deleteCustomerSql = "DELETE FROM customer WHERE customer_id=?";
        String deletePersonSql = "DELETE FROM person WHERE person_id=?";

        Connection con = null;

        try {
            con = DatabaseConnection.getConnection();
            con.setAutoCommit(false);

            try (PreparedStatement pstmt = con.prepareStatement(deleteCustomerSql)) {
                pstmt.setInt(1, t.getId());
                ketQua = pstmt.executeUpdate();
                System.out.println("DELETE from customer thành công, " + ketQua + " dòng bị thay đổi.");
            }

            try (PreparedStatement pstmt = con.prepareStatement(deletePersonSql)) {
                pstmt.setInt(1, t.getId());
                int personDeleteCount = pstmt.executeUpdate();
                System.out.println("DELETE from person thành công, " + personDeleteCount + " dòng bị thay đổi.");
            }

            con.commit();
        } catch (SQLException e) {
            try {
                if (con != null) {
                    con.rollback();
                }
            } catch (SQLException rollbackEx) {
                System.err.println("Lỗi khi rollback giao dịch: " + rollbackEx.getMessage());
            }
            System.err.println("Lỗi khi xóa khách hàng: " + e.getMessage());
        } finally {
            try {
                if (con != null) {
                    con.setAutoCommit(true);
                }
            } catch (SQLException e) {
                System.err.println("Lỗi khi reset auto-commit: " + e.getMessage());
            }
            DatabaseConnection.closeConnection(con);
        }

        return ketQua;
    }

    @Override
    public List<Customer> selectAll() {
        List<Customer> ketQua = new ArrayList<>();
        String sql = "SELECT p.*, c.point, c.created_at " +
                    "FROM customer c " +
                    "JOIN person p ON c.customer_id = p.person_id";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                ketQua.add(getCustomerFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy danh sách khách hàng: " + e.getMessage());
        }
        return ketQua;
    }

    public Customer selectById(int customerID) {
        Customer ketQua = null;
        String sql = "SELECT p.*, c.point, c.created_at " +
                    "FROM customer c " +
                    "JOIN person p ON c.customer_id = p.person_id " +
                    "WHERE c.customer_id = ?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setInt(1, customerID);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    ketQua = getCustomerFromResultSet(rs);
                }
            }

        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm khách hàng theo ID: " + e.getMessage());
        }
        return ketQua;
    }

    @Override
    public Customer selectById(Customer t) {
        return selectById(t.getId());
    }

    @Override
    public List<Customer> selectByCondition(String condition, Object... params) {
        List<Customer> customers = new ArrayList<>();

        if (condition == null || condition.trim().isEmpty()) {
            throw new IllegalArgumentException("Điều kiện truy vấn không hợp lệ.");
        }

        String sql = "SELECT p.*, c.point, c.created_at " +
                    "FROM customer c " +
                    "JOIN person p ON c.customer_id = p.person_id " +
                    "WHERE " + condition;

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {

            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    customers.add(getCustomerFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi truy vấn Customer theo điều kiện: " + e.getMessage());
        }
        return customers;
    }

    public Customer findByPhone(String phone) throws SQLException {
        String sql = "SELECT p.*, c.point, c.created_at " +
                    "FROM customer c " +
                    "JOIN person p ON c.customer_id = p.person_id " +
                    "WHERE p.phone = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, phone);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return getCustomerFromResultSet(rs);
                }
            }
        }
        return null;
    }

    public void deleteAll(Connection conn) throws SQLException {
        String sql = "DELETE FROM customer";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        }
    }

    public void resetAutoIncrement(Connection conn) throws SQLException {
        String sql = "ALTER TABLE customer AUTO_INCREMENT = 1";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.executeUpdate();
        }
    }

    private Customer getCustomerFromResultSet(ResultSet rs) throws SQLException {
        int personID = rs.getInt("person_id");
        String fullName = rs.getString("full_name");
        GenderEnum gender = GenderEnum.valueOf(rs.getString("gender"));
        String phoneNumber = rs.getString("phone");
        String address = rs.getString("address");
        String email = rs.getString("email");
        int point = rs.getInt("point");
        Timestamp created_at = rs.getTimestamp("created_at");

        return new Customer(personID, fullName, gender, phoneNumber, address, email, point, created_at);
    }
}
