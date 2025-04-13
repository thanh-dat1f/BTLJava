package repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import enums.PaymentMethodEnum;
import enums.StatusEnum;
import model.Invoice;
import model.Order;
import model.Staff;
import utils.DatabaseConnection;

public class InvoiceRepository implements IRepository<Invoice> {

	private static InvoiceRepository instance;

	public static InvoiceRepository getInstance() {
	    if (instance == null) {
	        instance = new InvoiceRepository();
	    }
	    return instance;
	}


    @Override
    public int insert(Invoice invoice) {
        String sql = "INSERT INTO invoice (order_id, payment_date, total, status, payment_method, staff_id) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, invoice.getOrder().getOrderId());
            pstmt.setTimestamp(2, invoice.getPaymentDate());
            pstmt.setBigDecimal(3, invoice.getTotal());
            pstmt.setString(4, invoice.getStatus().name());
            pstmt.setString(5, invoice.getPaymentMethod().name());
            pstmt.setInt(6, invoice.getStaff().getId());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        invoice.setInvoiceId(rs.getInt(1));
                    }
                }
            }
            return affectedRows;
        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm hóa đơn: " + e.getMessage());
            return 0;
        }
    }

    @Override
    public int update(Invoice invoice) {
        String sql = "UPDATE invoice SET order_id=?, payment_date=?, total=?, status=?, payment_method=?, staff_id=? WHERE invoice_id=?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {

        	pstmt.setInt(1, invoice.getOrder().getOrderId());
            pstmt.setTimestamp(2, invoice.getPaymentDate());
            pstmt.setBigDecimal(3, invoice.getTotal());
            pstmt.setString(4, invoice.getStatus().name());
            pstmt.setString(5, invoice.getPaymentMethod().name());
            pstmt.setInt(6, invoice.getStaff().getId());
            pstmt.setInt(7, invoice.getInvoiceId());

            
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật hóa đơn: " + e.getMessage());
            return 0;
        }
    }

    @Override
    public int delete(Invoice invoice) {
        String sql = "DELETE FROM invoice WHERE invoice_id=?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setInt(1, invoice.getInvoiceId());
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Lỗi khi xóa hóa đơn: " + e.getMessage());
            return 0;
        }
    }

    @Override
    public List<Invoice> selectAll() {
        List<Invoice> list = new ArrayList<>();
        String sql = "SELECT * FROM invoice";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToInvoice(rs));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy danh sách hóa đơn: " + e.getMessage());
        }
        return list;
    }

    public Invoice selectById(int invoiceId) {
        String sql = "SELECT * FROM invoice WHERE invoice_id = ?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setInt(1, invoiceId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToInvoice(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm hóa đơn theo ID: " + e.getMessage());
        }
        return null;
    }
    @Override
    public Invoice selectById(Invoice invoice) {
        return selectById(invoice.getInvoiceId());
    }

    @Override
    public List<Invoice> selectByCondition(String whereClause, Object... params) {
        List<Invoice> list = new ArrayList<>();
        String sql = "SELECT * FROM invoice WHERE " + whereClause;

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {

            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToInvoice(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi truy vấn hóa đơn theo điều kiện: " + e.getMessage());
        }
        return list;
    }

    private Invoice mapResultSetToInvoice(ResultSet rs) throws SQLException {
        Order order = new Order();
        order.setOrderId(rs.getInt("order_id"));

        Staff staff = new Staff();
        staff.setId(rs.getInt("staff_id"));

        return new Invoice(
            rs.getInt("invoice_id"),
            order,
            rs.getTimestamp("payment_date"),
            rs.getBigDecimal("total"),
            PaymentMethodEnum.valueOf(rs.getString("payment_method")),
            StatusEnum.valueOf(rs.getString("status")),
            staff
        );
    }

}
