package enums;

public enum StatusEnum {
    PENDING(0), COMPLETED(1), CANCELLED(2), CONFIRMED(3), FAILED(4), PAID(5);

    private final int code;

    StatusEnum(int code) { this.code = code; }

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
