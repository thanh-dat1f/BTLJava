package repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import enums.StatusEnum;
import model.Booking;
import model.Customer;
import model.Pet;
import model.Staff;
import utils.DBUtil;
import utils.DatabaseConnection;

public class BookingRepository implements IRepository<Booking> {

	private static BookingRepository instance;

	public static BookingRepository getInstance() {
	    if (instance == null) {
	        instance = new BookingRepository();
	    }
	    return instance;
	}

	@Override
	public int insert(Booking t) {
		int ketQua = 0;
		String sql = "INSERT INTO booking (customer_id, pet_id, staff_id, booking_time, status, note) VALUES (?, ?, ?, ?, ?, ?)";

		Connection con = null;
		PreparedStatement pstmt = null;

		try {
			con = DatabaseConnection.getConnection();
			pstmt = con.prepareStatement(sql);

			pstmt.setInt(1, t.getCustomer().getId());
			pstmt.setInt(2, t.getPet().getPetId());
            pstmt.setInt(3, t.getStaff() != null ? t.getStaff().getId() : Types.NULL);  // Cho phép NULL
            pstmt.setTimestamp(4, Timestamp.valueOf(t.getBookingTime())); 
			pstmt.setString(5, t.getStatus().name());
			pstmt.setString(6, t.getNote());

			ketQua = pstmt.executeUpdate();
			System.out.println("INSERT thành công, " + ketQua + " dòng bị thay đổi.");

		} catch (SQLException e) {
			System.err.println("Lỗi khi thêm booking: " + e.getMessage());
		} finally {
			DBUtil.closeResources(con, pstmt);
		}
		return ketQua;
	}

	@Override
	public int update(Booking t) {
		int ketQua = 0;
        String sql = "UPDATE booking SET staff_id = ?, booking_time = ?, status = ?, note = ? WHERE booking_id = ?";

		Connection con = null;
		PreparedStatement pstmt = null;

		try {
			con = DatabaseConnection.getConnection();
			pstmt = con.prepareStatement(sql);

			pstmt.setInt(1, t.getStaff() != null ? t.getStaff().getId() : Types.NULL);
            pstmt.setTimestamp(2, Timestamp.valueOf(t.getBookingTime()));
            pstmt.setString(3, t.getStatus().name());
            pstmt.setString(4, t.getNote());
            pstmt.setInt(5, t.getBookingId());

			ketQua = pstmt.executeUpdate();
			System.out.println("UPDATE thành công, " + ketQua + " dòng bị thay đổi.");

		} catch (SQLException e) {
			System.err.println("Lỗi khi cập nhật booking: " + e.getMessage());
		} finally {
			DBUtil.closeResources(con, pstmt);
		}
		return ketQua;
	}

	@Override
	public int delete(Booking t) {
		int ketQua = 0;
        String sql = "DELETE FROM booking WHERE booking_id = ?";

		Connection con = null;
		PreparedStatement pstmt = null;

		try {
			con = DatabaseConnection.getConnection();
			pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, t.getBookingId());

			ketQua = pstmt.executeUpdate();
			System.out.println("DELETE thành công, " + ketQua + " dòng bị thay đổi.");

		} catch (SQLException e) {
			System.err.println("Lỗi khi xóa booking: " + e.getMessage());
		} finally {
			DBUtil.closeResources(con, pstmt);
		}
		return ketQua;
	}

	@Override
	public List<Booking> selectAll() {
		List<Booking> list = new ArrayList<>();
        String sql = "SELECT * FROM booking";

		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try {
            con = DatabaseConnection.getConnection();
            pstmt = con.prepareStatement(sql);
            rs = pstmt.executeQuery();

            CustomerRepository customerRepo = new CustomerRepository();
            PetRepository petRepo = new PetRepository();
            StaffRepository staffRepo = new StaffRepository();

            while (rs.next()) {
                Customer customer = customerRepo.selectById(rs.getInt("customer_id"));
                Pet pet = petRepo.selectById(rs.getInt("pet_id"));
                Staff staff = rs.getObject("staff_id") != null ? staffRepo.selectById(rs.getInt("staff_id")) : null;

                Booking booking = new Booking(
                        rs.getInt("booking_id"),
                        customer,
                        pet,
                        staff,
                        rs.getTimestamp("booking_time").toLocalDateTime(),
                        StatusEnum.valueOf(rs.getString("status")),
                        rs.getString("note")
                );

                list.add(booking);
            }

        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy danh sách booking: " + e.getMessage());
        } finally {
            DBUtil.closeResources(con, pstmt, rs);
        }

        return list;
    }

    public Booking selectById(int bookingID) {
        Booking ketQua = null;
        String sql = "SELECT b.booking_id, b.booking_time, b.status, b.note, "
                + "c.customer_id, c.name AS customer_name, c.phone AS customer_phone, c.email AS customer_email, "
                + "p.pet_id, p.name AS pet_name, p.species AS pet_species, p.breed AS pet_breed, "
                + "s.staff_id, s.name AS staff_name, s.phone AS staff_phone "
                + "FROM booking b "
                + "JOIN customer c ON b.customer_id = c.customer_id "
                + "JOIN pet p ON b.pet_id = p.pet_id "
                + "LEFT JOIN staff s ON b.staff_id = s.staff_id "
                + "WHERE b.booking_id = ?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setInt(1, bookingID);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Customer customer = new CustomerRepository().selectById(rs.getInt("customer_id"));
                    Pet pet = new PetRepository().selectById(rs.getInt("pet_id"));
                    Staff staff = rs.getObject("staff_id") != null ? new StaffRepository().selectById(rs.getInt("staff_id")) : null;

                    ketQua = new Booking(
                            rs.getInt("booking_id"),
                            customer,
                            pet,
                            staff,
                            rs.getTimestamp("booking_time").toLocalDateTime(),
                            StatusEnum.valueOf(rs.getString("status")),
                            rs.getString("note")
                    );
                }
            }

        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm booking: " + e.getMessage());
        }

        return ketQua;
    }
    @Override
    public Booking selectById(Booking t) {
		return selectById(t.getBookingId());	
	}
    
    @Override
    public List<Booking> selectByCondition(String condition, Object... params) {
    	List<Booking> bookings = new ArrayList<>();

	    if (condition == null || condition.trim().isEmpty()) {
	        throw new IllegalArgumentException("Điều kiện truy vấn không hợp lệ.");
	    }

	    String sql = "SELECT b.booking_id, b.booking_time, b.status, b.note, "
                + "c.customer_id, c.name AS customer_name, c.phone AS customer_phone, c.email AS customer_email, "
                + "p.pet_id, p.name AS pet_name, p.species AS pet_species, p.breed AS pet_breed, "
                + "s.staff_id, s.name AS staff_name, s.phone AS staff_phone "
                + "FROM booking b "
                + "JOIN customer c ON b.customer_id = c.customer_id "
                + "JOIN pet p ON b.pet_id = p.pet_id "
                + "LEFT JOIN staff s ON b.staff_id = s.staff_id "
	            + "WHERE " + condition;

	    try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {

	        for (int i = 0; i < params.length; i++) {
	            pstmt.setObject(i + 1, params[i]);
	        }

	        try (ResultSet rs = pstmt.executeQuery()) {
	            while (rs.next()) {
	            	Customer customer = new CustomerRepository().selectById(rs.getInt("customer_id"));
                    Pet pet = new PetRepository().selectById(rs.getInt("pet_id"));
                    Staff staff = rs.getObject("staff_id") != null ? new StaffRepository().selectById(rs.getInt("staff_id")) : null;

                    bookings.add( new Booking(
                            rs.getInt("booking_id"),
                            customer,
                            pet,
                            staff,
                            rs.getTimestamp("booking_time").toLocalDateTime(),
                            StatusEnum.valueOf(rs.getString("status")),
                            rs.getString("note")
                    ));
	            }
	        }
	    } catch (SQLException e) {
	        System.err.println("Lỗi khi truy vấn booking theo điều kiện: " + e.getMessage());
	    }
	    return bookings;
    }
}
