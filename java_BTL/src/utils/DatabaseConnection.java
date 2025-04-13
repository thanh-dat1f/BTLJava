package utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {


	private static final Properties properties = new Properties();

    private static String URL;
    private static String USERNAME;
    private static String PASSWORD;
    private static String DRIVER;
    
    static {

        try (FileInputStream fis = new FileInputStream("resources\\database.properties")) {
            properties.load(fis);
            URL = properties.getProperty("url");
            USERNAME = properties.getProperty("username");
            PASSWORD = properties.getProperty("password");
            DRIVER = properties.getProperty("driver");

        } catch (IOException e) {
            System.err.println("Lỗi khi đọc file cấu hình database: " + e.getMessage());

            e.printStackTrace();
        }
    }


    // Kết nối đến database
    public static Connection getConnection() {
        Connection conn = null;
        try {
            Class.forName(DRIVER);
            // Tạo kết nối
            conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        } catch (ClassNotFoundException e) {
            System.err.println("Lỗi: Không tìm thấy MySQL JDBC Driver!");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Lỗi kết nối DB: " + e.getMessage());

        }
        return conn;
    }


    // Đóng kết nối

    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();


    // Print database info

                System.out.println("Đã đóng kết nối thành công.");
            } catch (SQLException e) {
                System.err.println("Lỗi khi đóng kết nối: " + e.getMessage());
            }
        }
    }

    // In thông tin database

    public static void printInfo(Connection conn) {
        if (conn != null) {
            try {
                DatabaseMetaData metaData = conn.getMetaData();
                System.out.println("Database: " + metaData.getDatabaseProductName());
                System.out.println("Version: " + metaData.getDatabaseProductVersion());
            } catch (SQLException e) {

                System.err.println("Lỗi khi lấy thông tin DB: " + e.getMessage());
            }
        }
    }
}

