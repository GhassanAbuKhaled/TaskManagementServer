package com.dev.ghassan.taskmanager.service;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
public class EmailService {
    
    @Value("${TASKFLOW_SENDGRID_API_KEY}")
    private String sendGridApiKey;
    
    private static final String FROM_EMAIL = "noreply@ghassanabukhaled.com";
    private static final String FROM_NAME = "TaskFlow";
    
    public void sendPasswordResetEmail(String toEmail, String resetToken) {
        try {
            Email from = new Email(FROM_EMAIL, FROM_NAME);
            Email to = new Email(toEmail);
            String subject = "Reset Your TaskFlow Password";
            
            String resetUrl = "https://taskflow.ghassanabukhaled.com/reset-password?token=" + resetToken;
            
            String htmlContent = buildPasswordResetEmailContent(resetUrl);
            Content content = new Content("text/html", htmlContent);
            
            Mail mail = new Mail(from, subject, to, content);
            
            SendGrid sg = new SendGrid(sendGridApiKey);
            Request request = new Request();
            
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            
            Response response = sg.api(request);
            
            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                log.info("Password reset email sent successfully to: {}", toEmail);
            } else {
                log.error("Failed to send password reset email. Status: {}, Body: {}", 
                         response.getStatusCode(), response.getBody());
                throw new RuntimeException("Failed to send password reset email");
            }
            
        } catch (IOException e) {
            log.error("Error sending password reset email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }
    
    private String buildPasswordResetEmailContent(String resetUrl) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>Reset Your Password</title>
            </head>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                    <h2 style="color: #2c3e50;">Reset Your TaskFlow Password</h2>
                    
                    <p>Hello,</p>
                    
                    <p>We received a request to reset your password for your TaskFlow account. If you didn't make this request, you can safely ignore this email.</p>
                    
                    <p>To reset your password, click the button below:</p>
                    
                    <div style="text-align: center; margin: 30px 0;">
                        <a href="%s" 
                           style="background-color: #3498db; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; display: inline-block;">
                            Reset Password
                        </a>
                    </div>
                    
                    <p>Or copy and paste this link into your browser:</p>
                    <p style="word-break: break-all; color: #3498db;">%s</p>
                    
                    <p><strong>This link will expire in 1 hour for security reasons.</strong></p>
                    
                    <hr style="border: none; border-top: 1px solid #eee; margin: 30px 0;">
                    
                    <p style="font-size: 12px; color: #666;">
                        If you're having trouble clicking the button, copy and paste the URL above into your web browser.
                    </p>
                    
                    <p style="font-size: 12px; color: #666;">
                        Best regards,<br>
                        The TaskFlow Team
                    </p>
                </div>
            </body>
            </html>
            """.formatted(resetUrl, resetUrl);
    }
}