package model;

import java.security.Timestamp;

public class Account {
    private int accountID;
    private String userName;
    private String password; // Đã mã hóa bằng bcrypt
    private Role role; // CUSTOMER, STAFF
    private boolean active;
    private Timestamp created_at;
    private Timestamp updated_at;

    public Account() {
        super();
    }

    public Account(int accountID, String userName, String password, Role role) {
        this.accountID = accountID;
        this.userName = userName;
        this.setPassword(password); // Mã hóa mật khẩu; // Kiểm tra định dạng email
        this.role = role;
    }
    

    public Account(int accountID, String userName, String password, Role role, boolean active, Timestamp created_at,
			Timestamp updated_at) {
		super();
		this.accountID = accountID;
		this.userName = userName;
		this.password = password;
		this.role = role;
		this.active = active;
		this.created_at = created_at;
		this.updated_at = updated_at;
	}

	public int getAccountID() {
        return accountID;
    }

    public void setAccountID(int accountID) {
        this.accountID = accountID;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    // Mã hóa mật khẩu bằng bcrypt
    public void setPassword(String password) {
//        this.password = BCrypt.hashpw(password, BCrypt.gensalt(12));
    	this.password = password;
    }


    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
    
    

    public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public Timestamp getCreated_at() {
		return created_at;
	}

	public void setCreated_at(Timestamp created_at) {
		this.created_at = created_at;
	}

	public Timestamp getUpdated_at() {
		return updated_at;
	}

	public void setUpdated_at(Timestamp updated_at) {
		this.updated_at = updated_at;
	}

	@Override
	public String toString() {
		return "Account [accountID=" + accountID + ", userName=" + userName + ", password=" + password + ", role="
				+ role + ", active=" + active + ", created_at=" + created_at + ", updated_at=" + updated_at + "]";
	}

	
}
