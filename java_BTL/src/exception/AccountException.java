package exception;

@SuppressWarnings("serial")
public class AccountException extends RuntimeException {
    public AccountException(String message) {
        super(message);
    }
}
