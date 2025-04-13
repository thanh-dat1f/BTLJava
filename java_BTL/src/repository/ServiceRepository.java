package repository;

import utils.DatabaseConnection;
import model.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceRepository implements IRepository<Service> {
    
    public static ServiceRepository getInstance() {
        return new ServiceRepository();
    }

    @Override
    public int insert(Service service) {
        String sql = "INSERT INTO service (name, description, price, duration_minutes, active) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, service.getName());
            pstmt.setString(2, service.getDescription());
            pstmt.setDouble(3, service.getPrice());
            pstmt.setInt(4, service.getDurationMinutes());
            pstmt.setBoolean(5, service.isActive());
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        service.setServiceId(rs.getInt(1));
                    }
                }
            }
            return affectedRows;
        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm dịch vụ: " + e.getMessage());
            return 0;
        }
    }

    @Override
    public int update(Service service) {
        String sql = "UPDATE service SET name=?, description=?, price=?, duration_minutes=?, active=? WHERE service_id=?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            
        	pstmt.setString(1, service.getName());
            pstmt.setString(2, service.getDescription());
            pstmt.setDouble(3, service.getPrice());
            pstmt.setInt(4, service.getDurationMinutes());
            pstmt.setBoolean(5, service.isActive());
            pstmt.setInt(6, service.getServiceId());
            
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật dịch vụ: " + e.getMessage());
            return 0;
        }
    }

    @Override
    public int delete(Service service) {
        String sql = "DELETE FROM service WHERE service_id=?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            
            pstmt.setInt(1, service.getServiceId());
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Lỗi khi xóa dịch vụ: " + e.getMessage());
            return 0;
        }
    }

    @Override
    public List<Service> selectAll() {
        List<Service> list = new ArrayList<>();
        String sql = "SELECT * FROM service";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                list.add(mapResultSetToService(rs));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy danh sách dịch vụ: " + e.getMessage());
        }
        return list;
    }

    @Override
    public Service selectById(Service service) {
        return selectById(service.getServiceId());
    }
    
    public Service selectById(int serviceID) {
        String sql = "SELECT * FROM service WHERE service_id = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            
            pstmt.setInt(1, serviceID);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToService(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm dịch vụ theo ID: " + e.getMessage());
        }
        return null;
    }
    

    @Override
    public List<Service> selectByCondition(String condition, Object... params) {
        List<Service> list = new ArrayList<>();
        String sql = "SELECT * FROM service WHERE " + condition;
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToService(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm kiếm dịch vụ theo điều kiện: " + e.getMessage());
        }
        return list;
    }

    private Service mapResultSetToService(ResultSet rs) throws SQLException {
    	int serviceID = rs.getInt("service_id");
        String serviceName = rs.getString("name");
        String description = rs.getString("description");
        double costPrice = rs.getDouble("price");
        int duration_minutes = rs.getInt("duration_minutes");
        Boolean active = rs.getBoolean("avtive");

        return new Service(serviceID, serviceName, description, costPrice, duration_minutes, active);
    }

}