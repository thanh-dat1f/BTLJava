package utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Random;
import javax.mail.*;
import javax.mail.internet.*;
import javax.mail.Session;

public class EmailUtil {
    private static final Properties config = new Properties();

    static {
        try (InputStream input = new FileInputStream("resources/email.properties")) {
            if (input == null) {
                throw new RuntimeException("Không tìm thấy file cấu hình email.properties!");
            }
            config.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi đọc file email.properties: " + e.getMessage());
        }
    }

    public static String sendVerificationCode(String recipientEmail) {
        String verificationCode = generateCode();

        // Cấu hình các thông tin SMTP
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", config.getProperty("email.host"));
        props.put("mail.smtp.port", config.getProperty("email.port"));

        // Tạo phiên làm việc email với đối tượng Authenticator
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(
                        config.getProperty("email.sender"),
                        config.getProperty("email.password")
                );
            }
        });

        try {
            // Tạo đối tượng email và gửi đi
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(config.getProperty("email.sender")));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject("Mã xác nhận đặt lại mật khẩu");
            message.setText("Mã xác nhận của bạn là: " + verificationCode);

            // Gửi email
            Transport.send(message);
            return verificationCode;
        } catch (MessagingException e) {
            throw new RuntimeException("Lỗi gửi email: " + e.getMessage());
        }
    }

    private static String generateCode() {
        return String.valueOf(100000 + new Random().nextInt(900000));
    }
}
