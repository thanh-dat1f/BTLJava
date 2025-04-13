package repository;

import model.Promotion;
import utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PromotionRepository implements IRepository<Promotion> {

    private static PromotionRepository instance;

    public static PromotionRepository getInstance() {
        if (instance == null) {
            synchronized (PromotionRepository.class) {
                if (instance == null) {
                    instance = new PromotionRepository();
                }
            }
        }
        return instance;
    }

    @Override
    public int insert(Promotion promotion) {
        String sql = "INSERT INTO promotion (code, description, discount_percent, start_date, end_date, active) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, promotion.getCode());
            pstmt.setString(2, promotion.getDescription());
            pstmt.setInt(3, promotion.getDiscountPercent());
            pstmt.setDate(4, java.sql.Date.valueOf(promotion.getStartDate()));
            pstmt.setDate(5, java.sql.Date.valueOf(promotion.getEndDate()));
            pstmt.setBoolean(6, promotion.isActive());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        promotion.setPromotionId(rs.getInt(1));
                    }
                }
            }
            return affectedRows;
        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm khuyến mãi: " + e.getMessage());
            return 0;
        }
    }

    @Override
    public int update(Promotion promotion) {
        String sql = "UPDATE promotion SET code=?, description=?, discount_percent=?, start_date=?, end_date=?, active=? WHERE promotion_id=?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {

        	pstmt.setString(1, promotion.getCode());
            pstmt.setString(2, promotion.getDescription());
            pstmt.setInt(3, promotion.getDiscountPercent());
            pstmt.setDate(4, java.sql.Date.valueOf(promotion.getStartDate()));
            pstmt.setDate(5, java.sql.Date.valueOf(promotion.getEndDate()));
            pstmt.setBoolean(6, promotion.isActive());
            pstmt.setInt(7, promotion.getPromotionId());

            return pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật khuyến mãi: " + e.getMessage());
            return 0;
        }
    }

    @Override
    public int delete(Promotion promotion) {
        String sql = "DELETE FROM promotion WHERE promotion_id=?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setInt(1, promotion.getPromotionId());
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Lỗi khi xóa khuyến mãi: " + e.getMessage());
            return 0;
        }
    }

    @Override
    public List<Promotion> selectAll() {
        List<Promotion> list = new ArrayList<>();
        String sql = "SELECT * FROM promotion";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToPromotion(rs));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy danh sách khuyến mãi: " + e.getMessage());
        }
        return list;
    }

    @Override
    public Promotion selectById(Promotion promotion) {
        return selectById(promotion.getPromotionId());
    }

    public Promotion selectById(int promotionID) {
        String sql = "SELECT * FROM promotion WHERE promotion_id=?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setInt(1, promotionID);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPromotion(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm khuyến mãi theo ID: " + e.getMessage());
        }
        return null;
    }

    private Promotion mapResultSetToPromotion(ResultSet rs) throws SQLException {
        return new Promotion(
            rs.getInt("promotion_id"),
            rs.getString("code"),
            rs.getString("description"),
            rs.getInt("discount_percent"),
            rs.getDate("start_date").toLocalDate(), 
            rs.getDate("end_date").toLocalDate(),
            rs.getBoolean("active")
        );
    }


    @Override
    public List<Promotion> selectByCondition(String condition, Object... params) {
        List<Promotion> list = new ArrayList<>();
        String sql = "SELECT * FROM promotion WHERE " + condition;

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {

            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToPromotion(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm khuyến mãi theo điều kiện: " + e.getMessage());
        }
        return list;
    }

}
