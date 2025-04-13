package utils;

import model.Staff;
import java.util.HashMap;
import java.util.Map;

public class RoleChecker {
    private static final Map<String, String[]> rolePermissions = new HashMap<>();

    static {
        // Khởi tạo quyền cho từng vai trò
        rolePermissions.put("ADMIN", new String[]{
            "VIEW_SCHEDULE", "VIEW_ALL_BOOKINGS", "CREATE_BOOKING", "VIEW_INVOICE",
            "MANAGE_PAYMENT", "PRINT_RECEIPT", "APPLY_PROMOTION", "MARK_SERVICE_DONE"
        });

        rolePermissions.put("STAFF_RECEPTION", new String[]{
            "VIEW_SCHEDULE", "CREATE_BOOKING", "VIEW_INVOICE", "APPLY_PROMOTION"
        });

        rolePermissions.put("STAFF_CARE", new String[]{
            "VIEW_SCHEDULE", "VIEW_BOOKING_ASSIGNED", "MARK_SERVICE_DONE"
        });

        rolePermissions.put("STAFF_CASHIER", new String[]{
            "VIEW_SCHEDULE", "VIEW_INVOICE", "MANAGE_PAYMENT", "PRINT_RECEIPT"
        });
    }

    /**
     * Kiểm tra xem người dùng hiện tại có quyền được chỉ định hay không
     * @param permission Tên quyền cần kiểm tra
     * @return true nếu người dùng có quyền, false nếu không
     */
    public static boolean hasPermission(String permission) {
        // Lấy nhân viên hiện tại từ Session
        Staff staff = Session.getCurrentStaff();
        if (staff == null) return false;

        // Lấy vai trò của nhân viên
        String role = staff.getRole();
        if (role == null) return false;

        // Lấy danh sách quyền của vai trò
        String[] permissions = rolePermissions.get(role);
        if (permissions == null) return false;

        // Kiểm tra xem quyền được yêu cầu có trong danh sách không
        for (String perm : permissions) {
            if (perm.equals(permission)) {
                return true;
            }
        }

        return false;
    }
}