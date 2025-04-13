package enums;

public enum Shift {
	MORNING(0), AFTERNOON(1), EVENING(2);

	private final int code;

	Shift(int code) { this.code = code; }

    public int getCode() { return code; }

    public static StatusEnum fromCode(int code) {
        for (StatusEnum status : StatusEnum.values()) {
            if (status.getCode() == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid status code: " + code);
    }
}
