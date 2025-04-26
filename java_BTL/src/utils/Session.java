package utils;

import model.Account;
import model.Role;
import model.Staff;
import service.StaffService;

public class Session {
    private static Session instance;
    private Account currentAccount;
    private Staff currentStaff;

    private Session() {
        // Private constructor for singleton
    }

    public static Session getInstance() {
        if (instance == null) {
            instance = new Session();
        }
        return instance;
    }

    public void setCurrentAccount(Account account) {
        this.currentAccount = account;

        // If an account is set, also try to find the associated staff
        if (account != null) {
            StaffService staffService = new StaffService();
            this.currentStaff = staffService.getStaffByAccountID(account.getAccountID());
        } else {
            this.currentStaff = null;
        }
    }

    public Account getCurrentAccount() {
        return currentAccount;
    }

    public Staff getCurrentStaff() {
        return currentStaff;
    }

    public void clearSession() {
        currentAccount = null;
        currentStaff = null;
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
}