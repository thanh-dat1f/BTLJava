package repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import enums.GenderEnum;
import model.Customer;
import model.Pet;
import model.PetType;
import utils.DBUtil;
import utils.DatabaseConnection;

public class PetRepository implements IRepository<Pet> {

	public static PetRepository getInstance() {
		return new PetRepository();
	}

	@Override
	public int insert(Pet t) {
		int ketQua = 0;
		String sql = "INSERT INTO pet (name, pet_gender, dob, customer_id, type_id, weight) VALUES (?, ?, ?, ?, ?, ?)";

		Connection con = null;
		PreparedStatement pstmt = null;

		try {
			con = DatabaseConnection.getConnection();
			pstmt = con.prepareStatement(sql);

			pstmt.setString(1, t.getName());
			pstmt.setString(2, t.getGender().getDescription());
			pstmt.setDate(3, java.sql.Date.valueOf(t.getDob()));
			pstmt.setInt(4, t.getOwner().getId());
			pstmt.setInt(5, t.getTypePet().getTypePetID());
			pstmt.setDouble(6, t.getWeight());

			ketQua = pstmt.executeUpdate();
			System.out.println("INSERT thành công, " + ketQua + " dòng bị thay đổi.");

		} catch (SQLException e) {
			System.err.println("Lỗi khi thêm thú cưng: " + e.getMessage());
		} finally {
			DBUtil.closeResources(con, pstmt);
		}
		return ketQua;
	}

	@Override
	public int update(Pet t) {
		int ketQua = 0;
		String sql = "UPDATE pet SET name = ?, pet_gender = ?, dob = ?, customer_id = ?, type_id = ?, weight = ? WHERE pet_id = ?";

		Connection con = null;
		PreparedStatement pstmt = null;

		try {
			con = DatabaseConnection.getConnection();
			pstmt = con.prepareStatement(sql);

			pstmt.setString(1, t.getName());
			pstmt.setString(2, t.getGender().getDescription());
			pstmt.setDate(3, java.sql.Date.valueOf(t.getDob()));
			pstmt.setInt(4, t.getOwner().getId());
			pstmt.setInt(5, t.getTypePet().getTypePetID());
			pstmt.setDouble(6, t.getWeight());
			pstmt.setInt(7, t.getPetId());


			ketQua = pstmt.executeUpdate();
			System.out.println("UPDATE thành công, " + ketQua + " dòng bị thay đổi.");

		} catch (SQLException e) {
			System.err.println("Lỗi khi cập nhật thú cưng: " + e.getMessage());
		} finally {
			DBUtil.closeResources(con, pstmt);
		}
		return ketQua;
	}

	@Override
	public int delete(Pet t) {
		int ketQua = 0;
		String sql = "DELETE FROM pet WHERE pet_id = ?";

		Connection con = null;
		PreparedStatement pstmt = null;

		try {
			con = DatabaseConnection.getConnection();
			pstmt = con.prepareStatement(sql);
			pstmt.setInt(1, t.getPetId());

			ketQua = pstmt.executeUpdate();
			System.out.println("DELETE thành công, " + ketQua + " dòng bị thay đổi.");

		} catch (SQLException e) {
			System.err.println("Lỗi khi xóa thú cưng: " + e.getMessage());
		} finally {
			DBUtil.closeResources(con, pstmt);
		}
		return ketQua;
	}

	@Override
	public List<Pet> selectAll() {
		List<Pet> list = new ArrayList<>();
		String sql = "SELECT * FROM pet";

		Connection con = null;
		PreparedStatement pstmt = null;
		java.sql.ResultSet rs = null;

		try {
			con = DatabaseConnection.getConnection();
			pstmt = con.prepareStatement(sql);
			rs = pstmt.executeQuery();

			CustomerRepository customerRepository = new CustomerRepository();
			PetTypeRepository petTypeRepository = new PetTypeRepository();

			while (rs.next()) {
				// Lấy customer từ database dựa vào customerID
				
				Customer customer = customerRepository.selectById(rs.getInt("customer_id"));

				// Lấy typePet từ database dựa vào typePetID
				PetType petType = petTypeRepository.selectById(rs.getInt("type_id"));
				
			    GenderEnum gender = GenderEnum.valueOf(rs.getString("pet_gender"));
				// Tạo đối tượng Pet với đúng constructor
			    list.add(new Pet(
			    	    rs.getInt("pet_id"),
			    	    rs.getString("name"),
			    	    petType,
			    	    gender,
			    	    rs.getDate("dob").toLocalDate(),
			    	    rs.getDouble("weight"),
			    	    rs.getString("note"), // hoặc null
			    	    customer
			    	));

			}

		} catch (SQLException e) {
			System.err.println("Lỗi khi lấy danh sách thú cưng: " + e.getMessage());
		} finally {
			DBUtil.closeResources(con, pstmt, rs);
		}
		return list;
	}
	
	public Pet selectById(int petID) {
		Pet ketQua = null;
	    String sql = "SELECT p.*, c.*, t.* " 
				+ "FROM pet p " 
				+ "JOIN customer c ON p.customer_id = c.customer_id "
				+ "JOIN pet_type t ON p.type_id = t.type_id WHERE pet_id = ?"; 

	    Connection con = null;
	    PreparedStatement pstmt = null;
	    ResultSet rs = null;

	    try {
	        con = DatabaseConnection.getConnection();
	        pstmt = con.prepareStatement(sql);
	        pstmt.setInt(1, petID);  

	        rs = pstmt.executeQuery();
	        if (rs.next()) { // Kiểm tra có dữ liệu hay không
				// Lấy customer từ database dựa vào customerID
				CustomerRepository customerRepository = new CustomerRepository();
				Customer customer = customerRepository.selectById(rs.getInt("customer_id"));

				// Lấy typePet từ database dựa vào typePetID
				PetTypeRepository petTypeRepository = new PetTypeRepository();
				PetType petType = petTypeRepository.selectById(rs.getInt("type_id"));

			    GenderEnum gender = GenderEnum.valueOf(rs.getString("gender"));

				// Tạo đối tượng Pet với đúng constructor
				ketQua = new Pet(
					    rs.getInt("pet_id"),
					    rs.getString("name"),
					    petType,
					    gender,
					    rs.getDate("dob").toLocalDate(),
					    rs.getDouble("weight"),
					    rs.getString("note"), // Hoặc null nếu không có
					    customer
					);
			} 

	    } catch (SQLException e) {
	        System.err.println("Lỗi khi tìm kpet theo ID: " + e.getMessage());
	    } finally {
	        DBUtil.closeResources(con, pstmt, rs);
	    }
	    return ketQua;
	}


	@Override
	public Pet selectById(Pet t) {
		return selectById(t.getPetId());	
	}

	@Override
	public List<Pet> selectByCondition(String condition, Object... params) {
		List<Pet> list = new ArrayList<>();

		// Sử dụng JOIN để lấy thông tin Customer và TypePet trực tiếp
		String sql = "SELECT p.*, c.*, t.* " 
					+ "FROM pet p " 
					+ "JOIN customer c ON p.customer_id = c.customer_id "
					+ "JOIN pet_type t ON p.type_id = t.type_id " 
					+ "WHERE " + condition;

		try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {

			// Truyền tham số vào PreparedStatement
			for (int i = 0; i < params.length; i++) {
				pstmt.setObject(i + 1, params[i]);
			}

			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					Customer customer = new Customer(rs.getInt("person_id"), rs.getString("full_name"), GenderEnum.fromCode(rs.getInt("gender")), rs.getString("phone"), rs.getString("address"), rs.getString("email"), rs.getInt("point"), rs.getTimestamp("created_at"));

				    GenderEnum gender = GenderEnum.valueOf(rs.getString("pet_gender"));

					// Lấy thông tin TypePet
					PetType petType = new PetType(
	                        rs.getInt("type_id"),
	                        rs.getString("species"),
	                        rs.getString("breed")
	                    );

					// Tạo đối tượng Pet
					list.add(new Pet(
						    rs.getInt("pet_id"),
						    rs.getString("name"),
						    petType,
						    gender,
						    rs.getDate("dob").toLocalDate(),
						    rs.getDouble("weight"),
						    rs.getString("note"), 
						    customer
						));
				}
			}
		} catch (SQLException e) {
			System.err.println("Lỗi khi tìm thú cưng: " + e.getMessage());
		}

		return list;
	}

}
