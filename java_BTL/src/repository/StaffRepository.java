package repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import enums.GenderEnum;
import model.Account;
import model.Role;
import model.Staff;
import utils.DatabaseConnection;

public class StaffRepository implements IRepository<Staff> {
	private static final Logger LOGGER = Logger.getLogger(StaffRepository.class.getName());

	private static StaffRepository instance;

    public static StaffRepository getInstance() {
        if (instance == null) {
            synchronized (StaffRepository.class) {
                if (instance == null) {
                    instance = new StaffRepository();
                }
            }
        }
        return instance;
    }
    
	@Override
	public int insert(Staff staff) {
		String insertPersonSql = "INSERT INTO person (full_name, gender, phone, address, email) "
				+ "VALUES (?, ?, ?, ?, ?)";
		String insertStaffSql = "INSERT INTO staff (staff_id, dob, salary, hire_date, account_id, role_id) "
				+ "VALUES (?, ?, ?, ?, ?, ?)";

		try (Connection con = DatabaseConnection.getConnection();
				PreparedStatement personStmt = con.prepareStatement(insertPersonSql, Statement.RETURN_GENERATED_KEYS);
				PreparedStatement staffStmt = con.prepareStatement(insertStaffSql)) {

			// Insert vào bảng person
			personStmt.setString(1, staff.getFullName());
			personStmt.setString(2, staff.getGender().getDescription());
			personStmt.setString(3, staff.getPhone());
			personStmt.setString(4, staff.getAddress());
			personStmt.setString(5, staff.getEmail());

			int personAffectedRows = personStmt.executeUpdate();

			if (personAffectedRows > 0) {
				try (ResultSet rs = personStmt.getGeneratedKeys()) {
					if (rs.next()) {
						int personID = rs.getInt(1); // Lấy PersonID sau khi insert thành công

						// Insert vào bảng staff
						staffStmt.setInt(1, personID);
						staffStmt.setDate(1, new java.sql.Date(staff.getDob().getTime()));
						staffStmt.setDouble(3, staff.getSalary());
						staffStmt.setDate(1, new java.sql.Date(staff.getHire_date().getTime()));
						staffStmt.setInt(5, staff.getAccount().getAccountID());
						staffStmt.setInt(6, staff.getRole().getRoleID());

						return staffStmt.executeUpdate();
					}
				}
			}
		} catch (SQLException e) {
			LOGGER.severe("Insert staff failed: " + e.getMessage());
		}
		return 0;
	}

	@Override
	public int update(Staff staff) {
		String updatePersonSql = "UPDATE person SET full_name=?, gender=?, phone=?, address=?, email=? WHERE person_id=?";
		String updateStaffSql = "UPDATE staff SET dob=?, salary=?, hire_date=?, account_id=?, role_id=? WHERE staff_id=?";

		try (Connection con = DatabaseConnection.getConnection();
				PreparedStatement personStmt = con.prepareStatement(updatePersonSql);
				PreparedStatement staffStmt = con.prepareStatement(updateStaffSql)) {

			// Cập nhật thông tin trong bảng person
			personStmt.setString(1, staff.getFullName());
			personStmt.setString(2, staff.getGender().getDescription());
			personStmt.setString(3, staff.getPhone());
			personStmt.setString(4, staff.getAddress());
			personStmt.setString(5, staff.getEmail());
			personStmt.setInt(6, staff.getId());

			int personAffectedRows = personStmt.executeUpdate();

			// Nếu cập nhật bảng person thành công, tiếp tục cập nhật bảng staff
			if (personAffectedRows > 0) {
				staffStmt.setDate(1, new java.sql.Date(staff.getDob().getTime()));
				staffStmt.setDouble(2, staff.getSalary());
				staffStmt.setDate(1, new java.sql.Date(staff.getHire_date().getTime()));
				staffStmt.setInt(4, staff.getAccount().getAccountID());
                staffStmt.setInt(5, staff.getRole().getRoleID());
				staffStmt.setInt(6, staff.getId());

				return staffStmt.executeUpdate(); 
			}
		} catch (SQLException e) {
			LOGGER.severe("Update staff failed: " + e.getMessage());
		}
		return 0; 
	}

	@Override
    public int delete(Staff staff) {
        String deleteStaffSql = "DELETE FROM staff WHERE staff_id=?";
        String deletePersonSql = "DELETE FROM person WHERE person_id=?";
        
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement staffStmt = con.prepareStatement(deleteStaffSql);
             PreparedStatement personStmt = con.prepareStatement(deletePersonSql)) {

            // Xóa nhân viên từ bảng staff
            staffStmt.setInt(1, staff.getId());
            int staffAffectedRows = staffStmt.executeUpdate();
            
            // Nếu xóa bảng staff thành công, tiếp tục xóa bảng person
            if (staffAffectedRows > 0) {
                personStmt.setInt(1, staff.getId());
                return personStmt.executeUpdate(); 
            }
        } catch (SQLException e) {
            LOGGER.severe("Delete staff failed: " + e.getMessage());
        }
        return 0; 
	}
	@Override
	public List<Staff> selectAll() {
		String sql = "SELECT p.person_id, p.full_name, p.gender, p.phone, p.address, p.email, "
	               + "a.account_id, a.username, r.role_id, r.role_name, s.dob, s.hire_date, s.salary "
	               + "FROM person p "
	               + "JOIN staff s ON p.person_id = s.staff_id "
	               + "JOIN role r ON s.role_id = r.role_id "
	               + "LEFT JOIN account a ON s.account_id = a.account_id";
	    return executeQuery(sql);
	}

	@Override
	public Staff selectById(Staff staff) {
		return selectById(staff.getId());
	}

	public Staff selectById(int personID) {
		String sql = "SELECT p.person_id, p.full_name, p.gender, p.phone, p.address, p.email, "
	               + "a.account_id, a.username, r.role_id, r.role_name, s.dob, s.hire_date, s.salary "
	               + "FROM person p "
	               + "JOIN staff s ON p.person_id = s.staff_id "
	               + "JOIN role r ON s.role_id = r.role_id "
	               + "LEFT JOIN account a ON s.account_id = a.account_id "
	               + "WHERE p.person_id = ?";
	    List<Staff> result = executeQuery(sql, personID);
	    return result.isEmpty() ? null : result.get(0);
	}

	public List<Staff> selectByCondition(String whereClause, Object... params) {
		String sql = "SELECT p.person_id, p.full_name, p.gender, p.phone, p.address, p.email, "
	               + "a.account_id, a.username, r.role_id, r.role_name, s.dob, s.hire_date, s.salary "
	               + "FROM person p "
	               + "JOIN staff s ON p.person_id = s.staff_id "
	               + "JOIN role r ON s.role_id = r.role_id "
	               + "LEFT JOIN account a ON s.account_id = a.account_id";

	    if (whereClause != null && !whereClause.trim().isEmpty()) {
	        sql += " WHERE " + whereClause;
	    }

	    return executeQuery(sql, params);
	}

	private List<Staff> executeQuery(String sql, Object... params) {
		List<Staff> list = new ArrayList<>();
		try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {

			for (int i = 0; i < params.length; i++) {
				pstmt.setObject(i + 1, params[i]);
			}

			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					list.add(mapResultSetToStaff(rs));
				}
			}
		} catch (SQLException e) {
			LOGGER.severe("Query failed: " + e.getMessage());
		}
		return list;
	}
	private Staff mapResultSetToStaff(ResultSet rs) throws SQLException {
	    // Lấy các giá trị từ ResultSet
	    int personID = rs.getInt("person_id");
	    String fullName = rs.getString("full_name");
	    GenderEnum gender = GenderEnum.fromCode(rs.getInt("gender"));
	    String phoneNumber = rs.getString("phone");
	    String address = rs.getString("address");
	    String email = rs.getString("email"); // Đảm bảo lấy được email
	    int accountID = rs.getInt("account_id");
	    Role role = new Role(rs.getInt("role_id"), rs.getString("role_name"));
	    
	    Date dob = rs.getDate("dob");

	    // Lấy các giá trị khác
	    double salary = rs.getDouble("salary");
	    Date hire_date = rs.getDate("hire_date");
	    
	    
	    // Kiểm tra tài khoản
	    Account account = null;
	    if (accountID > 0) { // Kiểm tra nếu accountID hợp lệ
	        String userName = rs.getString("username");
	        account = new Account(accountID, userName, null, role);
	    }
	    
	    // Trả về đối tượng Staff với tất cả các tham số đã lấy từ ResultSet
	    return new Staff(personID, fullName, gender, phoneNumber, address, email, dob, salary, hire_date, account, role);
	}


}