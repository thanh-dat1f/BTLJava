package enums;

public enum PaymentMethodEnum {
	CASH(0), CARD(1), MOMO(2), BANKING(3);

	private final int code;

	PaymentMethodEnum(int code) { this.code = code; }

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
