package repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import model.PetType;
import utils.DatabaseConnection;

public class PetTypeRepository implements IRepository<PetType> {

    public static PetTypeRepository getInstance() {
        return new PetTypeRepository();
    }

    @Override
    public int insert(PetType t) {
        int result = 0;
        String sql = "INSERT INTO pet_type (species, breed) VALUES (?, ?)";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, t.getSpecies());
            pstmt.setString(2, t.getBreed());
            result = pstmt.executeUpdate();

            // Lấy ID tự động tăng
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                t.setTypePetID(rs.getInt(1));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public int update(PetType t) {
        int result = 0;
        String sql = "UPDATE pet_type SET species = ?, breed = ? WHERE type_id = ?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setString(1, t.getSpecies());
            pstmt.setString(2, t.getBreed());
            pstmt.setInt(3, t.getTypePetID());

            result = pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public int delete(PetType t) {
        int result = 0;
        String sql = "DELETE FROM pet_type WHERE type_id = ?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setInt(1, t.getTypePetID());
            result = pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public List<PetType> selectAll() {
        List<PetType> list = new ArrayList<>();
        String sql = "SELECT * FROM pet_type";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                list.add(new PetType(rs.getInt("type_id"), rs.getString("species"), rs.getString("breed")));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public PetType selectById(int typePetID) {
        PetType petType = null;
        String sql = "SELECT * FROM pet_type WHERE TypePetID = ?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setInt(1, typePetID);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                petType = new PetType(rs.getInt("type_id"), rs.getString("species"), rs.getString("breed"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return petType;
    }
    
    @Override
    public PetType selectById(PetType t) {
    	return selectById(t.getTypePetID()); // Gọi lại phương thức nhận int
    }

    @Override
    public List<PetType> selectByCondition(String condition, Object... params) {
        List<PetType> list = new ArrayList<>();
        
        // Tránh nối chuỗi trực tiếp, sử dụng tham số hóa
        String sql = "SELECT * FROM pet_type WHERE " + condition;
        
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            
            // Truyền tham số vào câu lệnh SQL
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new PetType(
                        rs.getInt("type_id"),
                        rs.getString("species"),
                        rs.getString("breed")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi truy vấn pet_type: " + e.getMessage());
        }
        
        return list;
    }

}

