package com.otobus.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Doğrulama kodu içeren mail gönderir
     */
    public void sendVerificationEmail(String toEmail, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("OtoBilet - E-posta Doğrulama Kodu");
        message.setText(
            "Merhaba,\n\n" +
            "OtoBilet hesabınız için doğrulama kodunuz:\n\n" +
            "    " + code + "\n\n" +
            "Bu kod 10 dakika geçerlidir.\n\n" +
            "Eğer bu işlemi siz yapmadıysanız, bu e-postayı görmezden gelebilirsiniz.\n\n" +
            "İyi yolculuklar!\nOtoBilet Ekibi"
        );
        message.setFrom("arda008000@gmail.com");

        mailSender.send(message);
    }
}
