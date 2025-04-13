package utils;

import model.Account;
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

    public static Account getCurrentAccount() {
        return getInstance().currentAccount;
    }

    public static Staff getCurrentStaff() {
        return getInstance().currentStaff;
    }

    public static void clearSession() {
        getInstance().currentAccount = null;
        getInstance().currentStaff = null;
    }
    
    // Add the logout method as an alias for clearSession
    public static void logout() {
        clearSession();
    }
}