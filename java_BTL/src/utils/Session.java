package utils;

import model.Account;
import model.Role;
import model.Staff;
import service.StaffService;

import java.util.HashMap;
import java.util.Map;

/**
 * Lớp quản lý phiên làm việc (Session)
 */
public class Session {
    private static Session instance;
    private Account currentAccount;
    private Staff currentStaff;
    private Map<String, Object> attributes; // Thêm Map để lưu trữ thuộc tính

    private Session() {
        // Private constructor for singleton
        attributes = new HashMap<>();
    }

    /**
     * Lấy instance duy nhất của Session
     * @return instance của Session
     */
    public static Session getInstance() {
        if (instance == null) {
            instance = new Session();
        }
        return instance;
    }

    /**
     * Thiết lập tài khoản hiện tại
     * @param account Tài khoản
     */
    public void setCurrentAccount(Account account) {
        this.currentAccount = account;

        // Nếu tài khoản được thiết lập, lấy thông tin nhân viên liên quan
        if (account != null) {
            StaffService staffService = new StaffService();
            this.currentStaff = staffService.getStaffByAccountID(account.getAccountID());
        } else {
            this.currentStaff = null;
        }
    }

    /**
     * Lấy tài khoản hiện tại
     * @return Tài khoản hiện tại
     */
    public Account getCurrentAccount() {
        return currentAccount;
    }

    /**
     * Xóa toàn bộ session
     */
    public void clearSession() {
        currentAccount = null;
        currentStaff = null;
        attributes.clear();
    }

    /**
     * Lưu trữ một thuộc tính vào session
     * @param key Tên thuộc tính
     * @param value Giá trị thuộc tính
     */
    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }

    /**
     * Lấy giá trị của một thuộc tính từ session
     * @param key Tên thuộc tính
     * @return Giá trị thuộc tính, hoặc null nếu không tồn tại
     */
    public Object getAttribute(String key) {
        return attributes.get(key);
    }

    /**
     * Xóa một thuộc tính khỏi session
     * @param key Tên thuộc tính
     */
    public void removeAttribute(String key) {
        attributes.remove(key);
    }

    // Static convenience methods
    public static Account getCurrentUser() {
        return getInstance().getCurrentAccount();
    }

    public static Role getUserRole() {
        Account account = getCurrentUser();
        return account != null ? account.getRole() : null;
    }

    public static void setCurrentUser(Account account) {
        getInstance().setCurrentAccount(account);
    }

    public static void logout() {
        getInstance().clearSession();
    }

    /**
     * Phương thức tiện ích để lấy nhân viên hiện tại
     * @return Nhân viên hiện tại
     */
    public static Staff getCurrentStaff() {
        return getInstance().currentStaff;
    }
}