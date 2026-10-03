package vn.edu.hcmute.fashion.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class GmailRegistrationOtpMailer implements RegistrationOtpMailer {

    private final JavaMailSender mailSender;
    private final String senderAddress;

    public GmailRegistrationOtpMailer(
            JavaMailSender mailSender,
            @Value("${spring.mail.username:}") String senderAddress
    ) {
        this.mailSender = mailSender;
        this.senderAddress = senderAddress;
    }

    @Override
    public void sendOtp(String recipient, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(senderAddress);
        message.setTo(recipient);
        message.setSubject("Xác minh tài khoản Fashion E-commerce");
        message.setText("Mã OTP đăng ký của bạn là: " + otp + "\nMã có hiệu lực trong 60 giây.");
        mailSender.send(message);
    }
}
