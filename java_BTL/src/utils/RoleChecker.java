package utils;

import model.Account;
import model.Staff;
import java.util.HashMap;
import java.util.Map;

public class RoleChecker {
    private static final Map<String, String[]> rolePermissions = new HashMap<>();

    static {
        // Khởi tạo quyền cho từng vai trò
        rolePermissions.put("ADMIN", new String[]{
            "VIEW_SCHEDULE", "VIEW_ALL_BOOKINGS", "CREATE_BOOKING", "VIEW_INVOICE",
            "MANAGE_PAYMENT", "PRINT_RECEIPT", "APPLY_PROMOTION", "MARK_SERVICE_DONE",
            "MANAGE_ACCOUNT", "ASSIGN_PERMISSION", "MANAGE_SCHEDULE", "ADD_EMPLOYEE",
            "MANAGE_SERVICE", "VIEW_FINANCE", "VIEW_DASHBOARD", "UPDATE_PROFILE"
        });

        rolePermissions.put("STAFF_RECEPTION", new String[]{
            "CREATE_BOOKING", "MODIFY_BOOKING", "VIEW_CUSTOMER_INFO", "REGISTER_NEW_CUSTOMER",  
            "REGISTER_NEW_PET", "CHECK_IN_CUSTOMER", "VIEW_SCHEDULE", "VIEW_INVOICE", 
            "APPLY_PROMOTION", "VIEW_CUSTOMER", "UPDATE_PROFILE"
        });

        rolePermissions.put("STAFF_CARE", new String[]{
            "VIEW_SCHEDULE", "VIEW_BOOKING_ASSIGNED", "MARK_SERVICE_DONE", "VIEW_PET_DETAILS",  
            "ADD_PET_HEALTH_NOTE", "VIEW_CUSTOMER_PETS", "VIEW_CUSTOMER", "UPDATE_PROFILE"
        });

        rolePermissions.put("STAFF_CASHIER", new String[]{
            "VIEW_SCHEDULE", "CREATE_INVOICE", "PROCESS_PAYMENT", "PRINT_RECEIPT",
            "VIEW_DAILY_REVENUE", "APPLY_DISCOUNT", "VIEW_INVOICE", "MANAGE_PAYMENT", 
            "VIEW_CUSTOMER", "UPDATE_PROFILE"
        });
    }

    /**
     * Kiểm tra xem người dùng hiện tại có quyền được chỉ định hay không
     * @param permission Tên quyền cần kiểm tra
     * @return true nếu người dùng có quyền, false nếu không
     */
    public static boolean hasPermission(String permission) {
        // Kiểm tra nếu không có Session hoặc User
        Account currentUser = Session.getCurrentUser();
        if (currentUser == null || currentUser.getRole() == null) {
            return false;
        }
        
        // Lấy role name từ user
        String roleName = currentUser.getRole().getRoleName();
        if (roleName == null) {
            return false;
        }
        
        // Admin có tất cả các quyền
        if ("ADMIN".equals(roleName)) {
            return true;
        }
        
        // Kiểm tra quyền dựa trên role
        String[] permissions = rolePermissions.get(roleName);
        if (permissions == null) {
            return false;
        }
        
        // Kiểm tra xem quyền được yêu cầu có trong danh sách không
        for (String perm : permissions) {
            if (perm.equals(permission)) {
                return true;
            }
        }

        return false;
    }
}