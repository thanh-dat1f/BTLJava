package service;

import java.util.List;
import repository.PermissionRepository;
import model.Permission;

public class PermissionService {
    private final PermissionRepository permissionRepository;

    public PermissionService() {
        this.permissionRepository = PermissionRepository.getInstance();
    }

    /**
     * Kiểm tra xem một vai trò có quyền cụ thể không
     * @param roleId ID của vai trò
     * @param permissionCode Mã quyền cần kiểm tra
     * @return true nếu vai trò có quyền, false nếu không
     */
    public boolean checkRolePermission(int roleId, String permissionCode) {
        try {
            // Trong tương lai, bạn có thể mở rộng để thực hiện truy vấn từ database
            // Hiện tại sẽ giả định một số quyền mặc định
            return checkDefaultPermissions(roleId, permissionCode);
        } catch (Exception e) {
            System.err.println("Lỗi kiểm tra quyền: " + e.getMessage());
            return false;
        }
    }

    /**
     * Kiểm tra quyền mặc định dựa trên vai trò
     * @param roleId ID của vai trò
     * @param permissionCode Mã quyền cần kiểm tra
     * @return true nếu có quyền, false nếu không
     */
    private boolean checkDefaultPermissions(int roleId, String permissionCode) {
        // Danh sách quyền mặc định theo vai trò
        // Bạn có thể mở rộng hoặc thay thế bằng cơ chế kiểm tra quyền từ database
        switch(roleId) {
            case 1: // ADMIN
                return true;
            case 2: // MANAGER
                return checkManagerPermissions(permissionCode);
            case 3: // STAFF
                return checkStaffPermissions(permissionCode);
            default:
                return false;
        }
    }

    /**
     * Kiểm tra quyền cho vai trò Quản lý
     */
    private boolean checkManagerPermissions(String permissionCode) {
        String[] managerPermissions = {
            "VIEW_INVOICE", "MANAGE_PAYMENT", "VIEW_SCHEDULE", 
            "CREATE_BOOKING", "VIEW_BOOKING_ASSIGNED", 
            "APPLY_PROMOTION", "PRINT_RECEIPT"
        };

        for (String permission : managerPermissions) {
            if (permission.equals(permissionCode)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Kiểm tra quyền cho vai trò Nhân viên
     */
    private boolean checkStaffPermissions(String permissionCode) {
        String[] staffPermissions = {
            "VIEW_BOOKING_ASSIGNED", "MARK_SERVICE_DONE", 
            "CREATE_BOOKING", "PRINT_RECEIPT"
        };

        for (String permission : staffPermissions) {
            if (permission.equals(permissionCode)) {
                return true;
            }
        }
        return false;
    }
}