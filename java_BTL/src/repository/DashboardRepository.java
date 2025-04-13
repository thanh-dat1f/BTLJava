package repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import model.DashboardSummary;
import utils.DatabaseConnection;

public class DashboardRepository {
	private static DashboardRepository instance;

	public static DashboardRepository getInstance() {
	    if (instance == null) {
	        synchronized (PermissionRepository.class) {
	            if (instance == null) {
	                instance = new DashboardRepository();
	            }
	        }
	    }
	    return instance;
	}


    public DashboardSummary getSummary() throws SQLException {
        String sql = "SELECT * FROM dashboard_summary";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

        	if (rs.next()) {
                int totalCustomers = rs.getInt("total_customers");
                int totalBookings = rs.getInt("total_bookings");
                int totalInvoices = rs.getInt("total_invoices");
                double totalRevenue = rs.getDouble("total_revenue");

                return new DashboardSummary(totalCustomers, totalBookings, totalInvoices, totalRevenue);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
