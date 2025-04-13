package repository;

import model.Booking;
import model.BookingDetail;
import model.Service;
import utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookingDetailRepository implements IRepository<BookingDetail> {

    @Override
    public int insert(BookingDetail detail) {
        String sql = "INSERT INTO booking_detail (booking_id, service_id, quantity, price) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, detail.getBooking().getBookingId());
            stmt.setInt(2, detail.getService().getServiceId());
            stmt.setInt(3, detail.getQuantity());
            stmt.setDouble(4, detail.getPrice());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        detail.setBookingDetailId(rs.getInt(1));
                    }
                }
            }
            return affectedRows;

        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi thêm booking_detail: " + e.getMessage(), e);
        }
    }

    @Override
    public int update(BookingDetail detail) {
        String sql = "UPDATE booking_detail SET booking_id=?, service_id=?, quantity=?, price=? WHERE booking_detail_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

        	stmt.setInt(1, detail.getBooking().getBookingId());
            stmt.setInt(2, detail.getService().getServiceId());
            stmt.setInt(3, detail.getQuantity());
            stmt.setDouble(4, detail.getPrice());
            stmt.setInt(5, detail.getBookingDetailId());

            return stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi cập nhật booking_detail: " + e.getMessage(), e);
        }
    }

    @Override
    public int delete(BookingDetail detail) {
        String sql = "DELETE FROM booking_detail WHERE booking_detail_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, detail.getBookingDetailId());
            return stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi xóa booking_detail: " + e.getMessage(), e);
        }
    }

    @Override
    public List<BookingDetail> selectAll() {
        List<BookingDetail> list = new ArrayList<>();
        String sql = "SELECT * FROM booking_detail";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(getFromResultSet(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi lấy booking_detail: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public BookingDetail selectById(BookingDetail t) {
        return selectById(t.getBookingDetailId());
    }

    public BookingDetail selectById(int id) {
        String sql = "SELECT * FROM booking_detail WHERE booking_detail_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return getFromResultSet(rs);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi tìm booking_detail theo ID: " + e.getMessage(), e);
        }
        return null;
    }

    @Override
    public List<BookingDetail> selectByCondition(String condition, Object... params) {
        List<BookingDetail> list = new ArrayList<>();
        String sql = "SELECT * FROM booking_detail WHERE " + condition;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(getFromResultSet(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi truy vấn booking_detail: " + e.getMessage(), e);
        }
        return list;
    }

    private BookingDetail getFromResultSet(ResultSet rs) throws SQLException {
        BookingDetail detail = new BookingDetail();
        detail.setBookingDetailId(rs.getInt("booking_detail_id"));
     // Gán booking
        Booking booking = new Booking();
        booking.setBookingId(rs.getInt("booking_id"));
        detail.setBooking(booking);

        // Gán service
        Service service = new Service();
        service.setServiceId(rs.getInt("service_id"));
        detail.setService(service);
        
        detail.setQuantity(rs.getInt("quantity"));
        detail.setPrice(rs.getDouble("price"));
        return detail;
    }
}
