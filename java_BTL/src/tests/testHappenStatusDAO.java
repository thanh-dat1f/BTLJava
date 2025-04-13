package tests;

import java.util.List;


import enums.StatusEnum;
import model.HappenStatus;
import repository.HappenStatusRepository;

public class testHappenStatusDAO {
    public static void main(String[] args) {
        HappenStatusRepository happenStatusDAO = new HappenStatusRepository();
        
        // 1. Thêm mới HappenStatus với kiểm tra trùng lặp
        System.out.println("INSERT");
        StatusEnum newStatusCode = StatusEnum.COMPLETED;
        
        try {
            // Kiểm tra xem status code đã tồn tại chưa
            List<HappenStatus> existing = happenStatusDAO.selectByCondition(
                "UN_StatusCode = ?", 
                newStatusCode.ordinal()
            );
            
            if (!existing.isEmpty()) {
                System.out.println("Status code " + newStatusCode + " đã tồn tại. Sử dụng bản ghi hiện có.");
                HappenStatus newStatus  = existing.get(0);
            } else {
                HappenStatus newStatus = new HappenStatus(
                    0, // ID tự động tăng
                    newStatusCode,
                    "Đang xử lý"
                );
                
                int insertResult = happenStatusDAO.insert(newStatus);
                System.out.println("Insert Result: " + insertResult + " row(s) affected");
                System.out.println("Inserted Status ID: " + newStatus.getHappenStatusID());
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi thêm mới: " + e.getMessage());
        }
        System.out.println("\n");

        // 2. Lấy danh sách tất cả HappenStatus
        System.out.println("SELECT ALL");
        List<HappenStatus> statusList = happenStatusDAO.selectAll();
        System.out.println("Total statuses: " + statusList.size());
        for (HappenStatus status : statusList) {
            System.out.println(status);
        }
        System.out.println("\n");

        // 3. Cập nhật HappenStatus với kiểm tra trùng lặp
        if (!statusList.isEmpty()) {
            System.out.println("UPDATE");
            HappenStatus updateStatus = statusList.get(0);
            System.out.println("Before update:");
            System.out.println(updateStatus);
            
            StatusEnum newCode = StatusEnum.COMPLETED;
            
            try {
                // Kiểm tra xem status code mới đã tồn tại chưa (trừ bản ghi hiện tại)
                List<HappenStatus> checkDup = happenStatusDAO.selectByCondition(
                    "UN_StatusCode = ? AND HappenStatusID != ?", 
                    newCode.ordinal(),
                    updateStatus.getHappenStatusID()
                );
                
                if (!checkDup.isEmpty()) {
                    System.out.println("Không thể cập nhật - Status code " + newCode + " đã tồn tại");
                } else {
                    updateStatus.setStatusName("Đang xử lý (đã cập nhật)");
                    updateStatus.setStatusCode(newCode);
                    
                    int updateResult = happenStatusDAO.update(updateStatus);
                    System.out.println("Update Result: " + updateResult + " row(s) affected");
                    
                    // In ra thông tin sau khi cập nhật
                    HappenStatus updatedStatus = happenStatusDAO.selectById(updateStatus.getHappenStatusID());
                    System.out.println("After update:");
                    System.out.println(updatedStatus);
                }
            } catch (Exception e) {
                System.err.println("Lỗi khi cập nhật: " + e.getMessage());
            }
            System.out.println("\n");
        }

        // 4. Tìm HappenStatus theo ID
        if (!statusList.isEmpty()) {
            System.out.println("SELECT BY ID");
            int testId = statusList.get(0).getHappenStatusID();
            HappenStatus foundStatus = happenStatusDAO.selectById(testId);
            System.out.println("Status found by ID " + testId + ":");
            System.out.println(foundStatus);
            System.out.println("\n");
        }

        // 5. Tìm HappenStatus theo điều kiện
        System.out.println("SELECT BY CONDITION");
        StatusEnum searchCode = StatusEnum.COMPLETED;
        List<HappenStatus> filteredStatuses = happenStatusDAO.selectByCondition(
            "UN_StatusCode = ?", 
            searchCode.ordinal()
        );
        System.out.println("Found " + filteredStatuses.size() + " status(es) with " + searchCode + " status:");
        for (HappenStatus status : filteredStatuses) {
            System.out.println(status);
        }
        System.out.println("\n");

        // 6. Xóa HappenStatus với kiểm tra trước khi tạo bản ghi tạm
        System.out.println("DELETE");
        StatusEnum tempCode = StatusEnum.PENDING;
        
        try {
            // Kiểm tra xem status code tạm đã tồn tại chưa
            List<HappenStatus> checkTemp = happenStatusDAO.selectByCondition(
                "UN_StatusCode = ?", 
                tempCode.ordinal()
            );
            
            HappenStatus tempStatus;
            if (!checkTemp.isEmpty()) {
                System.out.println("Sử dụng bản ghi có sẵn để test xóa");
                tempStatus = checkTemp.get(0);
            } else {
                tempStatus = new HappenStatus();
                tempStatus.setStatusCode(tempCode);
                tempStatus.setStatusName("Tạm để xóa");
                happenStatusDAO.insert(tempStatus);
                System.out.println("Đã tạo bản ghi tạm với ID: " + tempStatus.getHappenStatusID());
            }
            
            int deleteResult = happenStatusDAO.delete(tempStatus);
            System.out.println("Delete Result: " + deleteResult + " row(s) affected");
            
            // Kiểm tra lại sau khi xóa
            HappenStatus deletedStatus = happenStatusDAO.selectById(tempStatus.getHappenStatusID());
            System.out.println("Status after deletion: " + (deletedStatus == null ? "Not found (deleted successfully)" : "Still exists"));
        } catch (Exception e) {
            System.err.println("Lỗi trong quá trình xóa: " + e.getMessage());
        }
        System.out.println("\n");
    }
}