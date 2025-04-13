package utils;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class FileExporter {
    // Xuất danh sách dữ liệu thành file CSV
    public static void exportToCSV(String filePath, List<String[]> data) {
        try (FileWriter writer = new FileWriter(filePath)) {
            for (String[] row : data) {
                writer.append(String.join(",", row)).append("\n");
            }
            System.out.println("Xuất file CSV thành công: " + filePath);
        } catch (IOException e) {
            System.err.println("Lỗi khi xuất CSV: " + e.getMessage());
        }
    }
}
