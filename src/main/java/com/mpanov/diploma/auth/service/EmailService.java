package com.mpanov.diploma.auth.service;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.IOException;

@Slf4j
@Service
public class EmailService {

    @Value("${sendgrid.from-email}")
    private String fromEmail;

    @Value("${sendgrid.templates.invitation}")
    private String templateInvitation;

    @Value("${sendgrid.templates.password-recovery}")
    private String templatePasswordRecovery;

    @Value("${shortener.base-url}")
    private String baseUrl;

    @Value("${platform.is-test}")
    private boolean isTest;

    @Autowired
    private TemplateEngine templateEngine;

    private final SendGrid sendGrid;

    public EmailService(@Value("${sendgrid.api-key}") String sendGridApiKey) {
        this.sendGrid = sendGridApiKey == null ? null : new SendGrid(sendGridApiKey);
    }

    public void sendInvitationEmail(String toEmail, String inviteeName, String inviterName, String organizationName, String email, String password) {
        Context context = new Context();
        context.setVariable("baseUrl", baseUrl);
        context.setVariable("inviteeName", inviteeName);
        context.setVariable("inviterName", inviterName);
        context.setVariable("organizationName", organizationName);
        context.setVariable("email", email);
        if (StringUtils.isNotBlank(password)) {
            context.setVariable("password", password);
        }

        String htmlContent = templateEngine.process(templateInvitation, context);
        sendEmail(toEmail, "You're Invited!", htmlContent);
    }

    public void sendPasswordRecoveryEmail(String toEmail, String userName, String recoveryCode) {
        Context context = new Context();
        String recoveryLink = baseUrl + "/password-reset/" + recoveryCode;
        context.setVariable("userName", userName);
        context.setVariable("recoveryLink", recoveryLink);

        String htmlContent = templateEngine.process(templatePasswordRecovery, context);
        sendEmail(toEmail, "Password Recovery", htmlContent);
    }


    private void sendEmail(String toEmail, String subject, String htmlContent) {
        if (isTest) {
            log.info("Skipping sendEmail for {}", toEmail);
            return;
        }
        Email from = new Email(fromEmail);
        Email to = new Email(toEmail);
        Content content = new Content("text/html", htmlContent);
        Mail mail = new Mail(from, subject, to, content);

        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            sendGrid.api(request);
        } catch (IOException ex) {
            log.error(ex.getMessage());
        }
    }
}
