package com.eeum.domain.notification.service;

import com.eeum.domain.notification.dto.request.MailRequest;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;

    public void sendHtmlMail(MailRequest request) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
        helper.setFrom("no-reply <hazardous10@naver.com>");
        helper.setTo(request.to());
        helper.setReplyTo("noreply@naver.com");
        helper.setSubject(request.subject());
        helper.setText(request.body(), true);

        try {
            mailSender.send(mimeMessage);
            System.out.println("전송 성공");
        } catch (Exception e) {
            System.out.println("전송 실패");
        }
    }
}
