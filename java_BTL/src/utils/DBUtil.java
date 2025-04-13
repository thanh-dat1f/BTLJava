package utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DBUtil {
    
    // Đóng tài nguyên sau khi truy vấn DB
    public static void closeResources(Connection conn, PreparedStatement ps, ResultSet rs) {
        try {
            if (rs != null) rs.close();
            if (ps != null) ps.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    // Đóng tài nguyên khi không có ResultSet
    public static void closeResources(Connection conn, PreparedStatement ps) {
        closeResources(conn, ps, null);
    }
}
