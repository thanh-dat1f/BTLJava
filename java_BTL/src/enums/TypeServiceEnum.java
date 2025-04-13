package enums;

public enum TypeServiceEnum {
	VIP(1, "VIP"), BASIC(2, "Cơ bản");



    private final int id;
    private final String description;
    
    TypeServiceEnum(int id, String description) {
        this.id = id;
        this.description = description;
    }  
    public int getId() {
        return id;
    }
    public String getDescription() {
        return description;
    }
    // Tìm TypeServiceEnum theo ID
    public static TypeServiceEnum fromId(int id) {
        for (TypeServiceEnum type : values()) {
            if (type.getId() == id) {
                return type;
            }
        }
        throw new IllegalArgumentException("Không tìm thấy loại dịch vụ với ID: " + id);
    }

	private final int id;
	private final String description;

	TypeServiceEnum(int id, String description) {
		this.id = id;
		this.description = description;
	}

	public int getId() {
		return id;
	}

	public String getDescription() {
		return description;
	}

	// Thêm phương thức tìm enum theo mô tả
	public static TypeServiceEnum fromDescription(String description) {
		for (TypeServiceEnum type : values()) {
			if (type.getDescription().equalsIgnoreCase(description)) {
				return type;
			}
		}
		throw new IllegalArgumentException("Không tìm thấy loại dịch vụ với mô tả: " + description);
	}

	// Tìm TypeServiceEnum theo ID
	public static TypeServiceEnum fromId(int id) {
		for (TypeServiceEnum type : values()) {
			if (type.getId() == id) {
				return type;
			}
		}
		throw new IllegalArgumentException("Không tìm thấy loại dịch vụ với ID: " + id);
	}


}
