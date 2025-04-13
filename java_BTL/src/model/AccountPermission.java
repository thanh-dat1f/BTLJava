package model;

public class AccountPermission {
	private int permissionId;
    private Account account;
    private Permission permission;
	public AccountPermission(int permissionId, Account account, Permission permission) {
		super();
		this.permissionId = permissionId;
		this.account = account;
		this.permission = permission;
	}
	public int getPermissionId() {
		return permissionId;
	}
	public void setPermissionId(int permissionId) {
		this.permissionId = permissionId;
	}
	public Account getAccount() {
		return account;
	}
	public void setAccount(Account account) {
		this.account = account;
	}
	public Permission getPermission() {
		return permission;
	}
	public void setPermission(Permission permission) {
		this.permission = permission;
	}
	@Override
	public String toString() {
		return "AccountPermission [permissionId=" + permissionId + ", account=" + account + ", permission=" + permission
				+ "]";
	}
    
}
