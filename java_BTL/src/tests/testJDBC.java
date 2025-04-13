package tests;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import utils.DatabaseConnection;

public class testJDBC {
	public static void main(String[] args) {
		try {
			// tạo kết nối
			Connection connection = DatabaseConnection.getConnection();
//		JDBC_Util.printInfo(connection);
//		
//		JDBC_Util.closeConnection(connection);
//		System.out.println(connection);
			
			// tạo ra đối tương statement
			Statement st = connection.createStatement();
			
			//thực thi 1 câu lệnh sql
			String sql = "INSERT INTO role(RoleName)"
					+ "VALUES (\"Nhân viênnnn\")";
			
			int check = st.executeUpdate(sql);
			
			
			System.out.println("Số dòng thay đổi: " + check);
			if(check>0) {
				System.out.println("Thêm dữ liệu thành công");
				
			}
			else {
				System.out.println("Thêm dữ liệu thất bại");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
	}
}
