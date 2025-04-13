package model;

public class Permission {

	private String permissionCode;
    private String description;
	public Permission(String permissionCode, String description) {
		super();
		this.permissionCode = permissionCode;
		this.description = description;
	}
	public String getPermissionCode() {
		return permissionCode;
	}
	public void setPermissionCode(String permissionCode) {
		this.permissionCode = permissionCode;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	@Override
	public String toString() {
		return "Permission [permissionCode=" + permissionCode + ", description=" + description + "]";
	}
    
    
}
