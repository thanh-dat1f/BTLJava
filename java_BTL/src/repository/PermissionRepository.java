package repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import model.Permission;
import utils.DatabaseConnection;

public class PermissionRepository implements IRepository<Permission> {

	private static PermissionRepository instance;

	public static PermissionRepository getInstance() {
	    if (instance == null) {
	        synchronized (PermissionRepository.class) {
	            if (instance == null) {
	                instance = new PermissionRepository();
	            }
	        }
	    }
	    return instance;
	}


    @Override
    public int insert(Permission t) {
        int result = 0;
        String sql = "INSERT INTO permission (permission_code, description) VALUES (?, ?)";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, t.getPermissionCode());
            pstmt.setString(2, t.getDescription());
            result = pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public int update(Permission t) {
        int result = 0;
        String sql = "UPDATE permission SET permission_code = ?, description = ? WHERE permission_code = ?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setString(1, t.getPermissionCode());
            pstmt.setString(2, t.getDescription());
            pstmt.setString(3, t.getPermissionCode());

            result = pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public int delete(Permission t) {
        int result = 0;
        String sql = "DELETE FROM permission WHERE permission_code = ?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setString(1, t.getPermissionCode());
            result = pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public List<Permission> selectAll() {
        List<Permission> list = new ArrayList<>();
        String sql = "SELECT * FROM permission";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                list.add(new Permission(rs.getString("permission_code"), rs.getString("description")));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Permission selectById(String permission_code) {
    	Permission per = null;
        String sql = "SELECT * FROM permission WHERE permission_code = ?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setString(1, permission_code);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
            	per = new Permission(rs.getString("permission_code"), rs.getString("description"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return per;
    }
    
    @Override
    public Permission selectById(Permission t) {
    	return selectById(t.getPermissionCode()); // Gọi lại phương thức nhận int
    }

    @Override
    public List<Permission> selectByCondition(String condition, Object... params) {
        List<Permission> list = new ArrayList<>();
        
        // Tránh nối chuỗi trực tiếp, sử dụng tham số hóa
        String sql = "SELECT * FROM permission WHERE " + condition;
        
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            
            // Truyền tham số vào câu lệnh SQL
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new Permission(rs.getString("permission_code"), rs.getString("description")));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi truy vấn permission: " + e.getMessage());
        }
        
        return list;
    }

}

