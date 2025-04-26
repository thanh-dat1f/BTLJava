package repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
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

			// Set auto-commit to false for transaction
			con.setAutoCommit(false);
			
			// Insert into person table
			personStmt.setString(1, staff.getFullName());
			personStmt.setString(2, staff.getGender().name());
			personStmt.setString(3, staff.getPhone());
			personStmt.setString(4, staff.getAddress());
			personStmt.setString(5, staff.getEmail());

			int personAffectedRows = personStmt.executeUpdate();

			if (personAffectedRows > 0) {
				try (ResultSet rs = personStmt.getGeneratedKeys()) {
					if (rs.next()) {
						int personID = rs.getInt(1); // Get the auto-generated PersonID
						staff.setId(personID);

						// Insert into staff table
						staffStmt.setInt(1, personID);
						
						// Handle dob (can be null)
						if (staff.getDob() != null) {
						    staffStmt.setDate(2, new java.sql.Date(staff.getDob().getTime()));
						} else {
						    staffStmt.setNull(2, java.sql.Types.DATE);
						}
						
						staffStmt.setDouble(3, staff.getSalary());
						
						// Use start date for hire_date if available
						if (staff.getStartDate() != null) {
						    staffStmt.setDate(4, Date.valueOf(staff.getStartDate()));
						} else if (staff.getHire_date() != null) {
						    staffStmt.setDate(4, new java.sql.Date(staff.getHire_date().getTime()));
						} else {
						    staffStmt.setNull(4, java.sql.Types.DATE);
						}
						
						// Account ID can be null
						if (staff.getAccount() != null) {
						    staffStmt.setInt(5, staff.getAccount().getAccountID());
						} else {
						    staffStmt.setNull(5, java.sql.Types.INTEGER);
						}
						
						staffStmt.setInt(6, staff.getRole().getRoleID());

						int staffAffectedRows = staffStmt.executeUpdate();
						
						// Commit transaction
						con.commit();
						
						return staffAffectedRows;
					}
				}
			}
			
			// Rollback if we get here (something went wrong)
			con.rollback();
			
		} catch (SQLException e) {
			LOGGER.severe("Insert staff failed: " + e.getMessage());
			e.printStackTrace();
		}
		return 0;
	}

	@Override
	public int update(Staff staff) {
		String updatePersonSql = "UPDATE person SET full_name=?, gender=?, phone=?, address=?, email=? WHERE person_id=?";
		String updateStaffSql = "UPDATE staff SET dob=?, salary=?, hire_date=?, account_id=?, role_id=? WHERE staff_id=?";

		try (Connection con = DatabaseConnection.getConnection()) {
		    con.setAutoCommit(false);
		    
		    try (PreparedStatement personStmt = con.prepareStatement(updatePersonSql);
		         PreparedStatement staffStmt = con.prepareStatement(updateStaffSql)) {

    			// Update person information
    			personStmt.setString(1, staff.getFullName());
    			personStmt.setString(2, staff.getGender().name());
    			personStmt.setString(3, staff.getPhone());
    			personStmt.setString(4, staff.getAddress());
    			personStmt.setString(5, staff.getEmail());
    			personStmt.setInt(6, staff.getId());
    
    			int personUpdated = personStmt.executeUpdate();
    
    			// Update staff information
    			if (staff.getDob() != null) {
    			    staffStmt.setDate(1, new java.sql.Date(staff.getDob().getTime()));
    			} else {
    			    staffStmt.setNull(1, java.sql.Types.DATE);
    			}
    			
    			staffStmt.setDouble(2, staff.getSalary());
    			
    			// Use start date for hire_date if available
    			if (staff.getStartDate() != null) {
    			    staffStmt.setDate(3, Date.valueOf(staff.getStartDate()));
    			} else if (staff.getHire_date() != null) {
    			    staffStmt.setDate(3, new java.sql.Date(staff.getHire_date().getTime()));
    			} else {
    			    staffStmt.setNull(3, java.sql.Types.DATE);
    			}
    			
    			if (staff.getAccount() != null) {
    			    staffStmt.setInt(4, staff.getAccount().getAccountID());
    			} else {
    			    staffStmt.setNull(4, java.sql.Types.INTEGER);
    			}
    			
    			staffStmt.setInt(5, staff.getRole().getRoleID());
    			staffStmt.setInt(6, staff.getId());
    
    			int staffUpdated = staffStmt.executeUpdate();
    			
    			con.commit();
    			
    			return (personUpdated > 0 && staffUpdated > 0) ? 1 : 0;
		    } catch (SQLException e) {
		        con.rollback();
		        throw e;
		    }
		} catch (SQLException e) {
			LOGGER.severe("Update staff failed: " + e.getMessage());
			e.printStackTrace();
		}
		return 0;
	}

	@Override
    public int delete(Staff staff) {
        String deleteStaffSql = "DELETE FROM staff WHERE staff_id=?";
        String deletePersonSql = "DELETE FROM person WHERE person_id=?";
        
        try (Connection con = DatabaseConnection.getConnection()) {
            con.setAutoCommit(false);
            
            try (PreparedStatement staffStmt = con.prepareStatement(deleteStaffSql);
                 PreparedStatement personStmt = con.prepareStatement(deletePersonSql)) {

                // Delete from staff table first (foreign key)
                staffStmt.setInt(1, staff.getId());
                int staffDeleted = staffStmt.executeUpdate();
                
                // If staff record is deleted, proceed to delete person record
                if (staffDeleted > 0) {
                    personStmt.setInt(1, staff.getId());
                    int personDeleted = personStmt.executeUpdate();
                    
                    con.commit();
                    return personDeleted;
                }
                
                con.rollback();
                return 0;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        } catch (SQLException e) {
            LOGGER.severe("Delete staff failed: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
	}

	@Override
	public List<Staff> selectAll() {
		String sql = "SELECT p.person_id, p.full_name, p.gender, p.phone, p.address, p.email, "
	               + "s.dob, s.salary, s.hire_date, s.account_id, s.role_id, "
	               + "r.role_name, a.username "
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
	               + "s.dob, s.salary, s.hire_date, s.account_id, s.role_id, "
	               + "r.role_name, a.username "
	               + "FROM person p "
	               + "JOIN staff s ON p.person_id = s.staff_id "
	               + "JOIN role r ON s.role_id = r.role_id "
	               + "LEFT JOIN account a ON s.account_id = a.account_id "
	               + "WHERE p.person_id = ?";
	    List<Staff> result = executeQuery(sql, personID);
	    return result.isEmpty() ? null : result.get(0);
	}

	@Override
	public List<Staff> selectByCondition(String whereClause, Object... params) {
		String sql = "SELECT p.person_id, p.full_name, p.gender, p.phone, p.address, p.email, "
	               + "s.dob, s.salary, s.hire_date, s.account_id, s.role_id, "
	               + "r.role_name, a.username "
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
		try (Connection con = DatabaseConnection.getConnection(); 
		     PreparedStatement pstmt = con.prepareStatement(sql)) {

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
			e.printStackTrace();
		}
		return list;
	}

	private Staff mapResultSetToStaff(ResultSet rs) throws SQLException {
	    // Extract basic person information
	    int personID = rs.getInt("person_id");
	    String fullName = rs.getString("full_name");
	    GenderEnum gender = GenderEnum.valueOf(rs.getString("gender"));
	    String phone = rs.getString("phone");
	    String address = rs.getString("address");
	    String email = rs.getString("email");
	    
	    // Extract staff-specific information
	    java.sql.Date dobSql = rs.getDate("dob");
	    java.util.Date dob = dobSql != null ? new java.util.Date(dobSql.getTime()) : null;
	    
	    double salary = rs.getDouble("salary");
	    
	    java.sql.Date hireDateSql = rs.getDate("hire_date");
	    java.util.Date hireDate = hireDateSql != null ? new java.util.Date(hireDateSql.getTime()) : null;
	    LocalDate startDate = hireDateSql != null ? hireDateSql.toLocalDate() : null;
	    
	    // Extract account information if available
	    int accountId = rs.getInt("account_id");
	    String username = rs.getString("username");
	    Account account = accountId > 0 ? new Account(accountId, username, null, null) : null;
	    
	    // Extract role information
	    int roleId = rs.getInt("role_id");
	    String roleName = rs.getString("role_name");
	    Role role = new Role(roleId, roleName);
	    
	    // Create and return the Staff object with both Person and Staff attributes
	    Staff staff = new Staff();
	    staff.setId(personID);
	    staff.setFullName(fullName);
	    staff.setGender(gender);
	    staff.setPhone(phone);
	    staff.setAddress(address);
	    staff.setEmail(email);
	    staff.setDob(dob);
	    staff.setSalary(salary);
	    staff.setHire_date(hireDate);
	    staff.setStartDate(startDate);
	    staff.setAccount(account);
	    staff.setRole(role);
	    
	    // Return position and workshift as empty strings since they aren't in the database
	    staff.setPosition("");
	    staff.setWorkShift("");
	    
	    return staff;
	}
}