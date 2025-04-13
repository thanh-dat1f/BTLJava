package service;

import model.Account;
import repository.AccountRepository;
import org.mindrot.jbcrypt.BCrypt;
import java.util.Optional;

public class AuthService {
    private final AccountRepository accountRepository;
    
    public AuthService() {
        this.accountRepository = AccountRepository.getInstance();
    }
    
    public Optional<Account> login(String username, String password) {
        Account account = accountRepository.getAccountByUsername(username);
        
        if (account == null) {
            System.out.println("Không tìm thấy tài khoản!");
            return Optional.empty();
        }
        
        boolean passwordMatch = BCrypt.checkpw(password, account.getPassword());
        
        if (!passwordMatch) {
            System.out.println("Mật khẩu không đúng!");
            return Optional.empty();
        }
        
        System.out.println("Đăng nhập thành công!");
        return Optional.of(account);
    }
    
    public boolean changePassword(int accountId, String newPassword) {
        // Validate password (could be more complex)
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Mật khẩu không được để trống");
        }
        
        // Hash the password before storing
        String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
        
        // Update the password
        return accountRepository.updatePassword(accountId, hashedPassword);
    }
    
    public boolean logout() {
        // Any logout-specific operations
        return true;
    }
}