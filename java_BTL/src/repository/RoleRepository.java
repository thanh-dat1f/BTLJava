package repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import model.Role;
import utils.DatabaseConnection;

public class RoleRepository implements IRepository<Role> {
	public static RoleRepository getInstance() {
		return new RoleRepository();
	}

	@Override
	public int insert(Role role) {
		String sql = "INSERT INTO Role (roleName) VALUES (?)";
		try (Connection con = DatabaseConnection.getConnection();
				PreparedStatement pstmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

			pstmt.setString(1, role.getRoleName().toUpperCase());

			int affectedRows = pstmt.executeUpdate();
			if (affectedRows > 0) {
				try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
					if (generatedKeys.next()) {
						role.setRoleID(generatedKeys.getInt(1));
					}
				}
			}
			return affectedRows;
		} catch (SQLException e) {
			System.err.println("Lỗi khi chèn Role: " + e.getMessage());
		}
		return 0;
	}

	@Override
	public int update(Role role) {
		String sql = "UPDATE Role SET roleName = ? WHERE role_ID = ?";
		try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {

			pstmt.setString(1, role.getRoleName().toUpperCase());
			pstmt.setInt(2, role.getRoleID());

			return pstmt.executeUpdate();
		} catch (SQLException e) {
			System.err.println("Lỗi khi cập nhật Role: " + e.getMessage());
		}
		return 0;
	}

	@Override
	public int delete(Role role) {
		String sql = "DELETE FROM Role WHERE role_ID = ?";
		try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {

			pstmt.setInt(1, role.getRoleID());
			return pstmt.executeUpdate();
		} catch (SQLException e) {
			System.err.println("Lỗi khi xóa Role: " + e.getMessage());
		}
		return 0;
	}

	@Override
	public List<Role> selectAll() {
		List<Role> list = new ArrayList<>();
		String sql = "SELECT * FROM Role";
		try (Connection con = DatabaseConnection.getConnection();
				PreparedStatement pstmt = con.prepareStatement(sql);
				ResultSet rs = pstmt.executeQuery()) {

			while (rs.next()) {
				list.add(mapResultSetToRole(rs));
			}
		} catch (SQLException e) {
			System.err.println("Lỗi khi lấy danh sách Role: " + e.getMessage());
		}
		return list;
	}

	@Override
	public Role selectById(Role role) {
		return selectById(role.getRoleID());
	}

	public Role selectById(int id) {
		String sql = "SELECT * FROM Role WHERE role_ID = ?";
		try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {

			pstmt.setInt(1, id);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					return mapResultSetToRole(rs);
				}
			}
		} catch (SQLException e) {
			System.err.println("Lỗi khi tìm Role theo ID: " + e.getMessage());
		}
		return null;
	}

	@Override
	public List<Role> selectByCondition(String condition, Object... params) {
		List<Role> list = new ArrayList<>();
		String sql = "SELECT * FROM Role WHERE " + condition;

		try (Connection con = DatabaseConnection.getConnection(); PreparedStatement pstmt = con.prepareStatement(sql)) {

			for (int i = 0; i < params.length; i++) {
				pstmt.setObject(i + 1, params[i]);
			}

			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					list.add(mapResultSetToRole(rs));
				}
			}
		} catch (SQLException e) {
			System.err.println("Lỗi khi truy vấn Role theo điều kiện: " + e.getMessage());
		}
		return list;
	}

	// Hàm hỗ trợ map dữ liệu từ ResultSet sang Role
	private Role mapResultSetToRole(ResultSet rs) throws SQLException {
		int role_id = rs.getInt("role_id");
		String roleName = rs.getString("roleName");

		return new Role(role_id, roleName);
	}
}
