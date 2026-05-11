package com.otobus.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * E-posta servisi.
 * Doğrulama kodları ve şifre sıfırlama mailleri gönderir.
 */
@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Doğrulama kodu içeren mail gönderir.
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
        message.setFrom(fromEmail);
        mailSender.send(message);
    }

    /**
     * Şifre sıfırlama doğrulama kodu içeren mail gönderir.
     */
    public void sendPasswordResetEmail(String toEmail, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("OtoBilet - Şifre Sıfırlama Kodu");
        message.setText(
            "Merhaba,\n\n" +
            "Şifre sıfırlama talebiniz alınmıştır.\n" +
            "Doğrulama kodunuz:\n\n" +
            "    " + code + "\n\n" +
            "Bu kod 10 dakika geçerlidir.\n\n" +
            "Eğer bu talebi siz yapmadıysanız, lütfen bu e-postayı görmezden gelin.\n" +
            "Hesabınız güvende ve herhangi bir değişiklik yapılmamıştır.\n\n" +
            "OtoBilet Ekibi"
        );
        message.setFrom(fromEmail);
        mailSender.send(message);
    }

    /**
     * Bilet satın alma bildirim maili gönderir.
     */
    public void sendTicketConfirmation(String toEmail, String ticketDetails) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("OtoBilet - Bilet Onayı");
        message.setText(
            "Merhaba,\n\n" +
            "Biletiniz başarıyla satın alınmıştır.\n\n" +
            ticketDetails + "\n\n" +
            "İyi yolculuklar!\nOtoBilet Ekibi"
        );
        message.setFrom(fromEmail);
        mailSender.send(message);
    }
}
