package com.blogapp.otp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @org.springframework.beans.factory.annotation.Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * Send an HTML email.
     */
    public void sendEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true); // true = HTML
            helper.setFrom(fromEmail);

            mailSender.send(message);
            log.info("Email sent to {} — subject: {}", to, subject);
        } catch (MessagingException e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
            log.warn("Email delivery failed but operation continues.");
        }
    }

    /**
     * Send answer approval notification email.
     */
    public void sendAnswerApprovalEmail(String to) {
        String subject = "Your answer was approved!";
        String body = """
                <html>
                <body style="font-family: Arial, sans-serif; padding: 20px;">
                    <h2>🎉 Congratulations!</h2>
                    <p>Your answer has been reviewed and approved by an administrator.</p>
                    <p>It is now visible on the question page to help others!</p>
                </body>
                </html>
                """;
        sendEmail(to, subject, body);
    }

    /**
     * Send answer rejection notification email.
     */
    public void sendAnswerRejectionEmail(String to, String reason) {
        String subject = "Update on your submitted answer";
        String body = """
                <html>
                <body style="font-family: Arial, sans-serif; padding: 20px;">
                    <h2>Answer Submission Update</h2>
                    <p>We've reviewed your submitted answer.</p>
                    <p>Unfortunately, it was not approved for the following reason:</p>
                    <blockquote style="border-left: 4px solid #E74C3C; padding: 10px; color: #555;">%s</blockquote>
                </body>
                </html>
                """.formatted(reason != null ? reason : "Does not meet guidelines");
        sendEmail(to, subject, body);
    }
    /**
     * Send blog approval notification email.
     */
    public void sendBlogApprovalEmail(String to, String blogTitle, String blogLink) {
        String subject = "Your Blog Post is Published!";
        String body = """
                <html>
                <body style="font-family: Arial, sans-serif; padding: 20px;">
                    <h2>🎉 Congratulations!</h2>
                    <p>Your blog post <strong>%s</strong> has been reviewed and approved by an administrator.</p>
                    <p>It is now live on our platform! You can view it here: <a href="%s">%s</a></p>
                </body>
                </html>
                """.formatted(blogTitle, blogLink, blogLink);
        sendEmail(to, subject, body);
    }

    /**
     * Send blog rejection notification email.
     */
    public void sendBlogRejectionEmail(String to, String blogTitle, String reason) {
        String subject = "Update on your submitted blog post";
        String body = """
                <html>
                <body style="font-family: Arial, sans-serif; padding: 20px;">
                    <h2>Blog Submission Update</h2>
                    <p>We've reviewed your submitted blog post: <strong>%s</strong>.</p>
                    <p>Unfortunately, it was not approved for the following reason:</p>
                    <blockquote style="border-left: 4px solid #E74C3C; padding: 10px; color: #555;">%s</blockquote>
                    <p>Please revise your content and submit again.</p>
                </body>
                </html>
                """.formatted(blogTitle, reason != null ? reason : "Does not meet our content guidelines.");
        sendEmail(to, subject, body);
    }
    
    /**
     * Send new blog post notification to subscribers.
     */
    public void sendNewBlogPostNotification(String to, String blogTitle, String blogLink) {
        String subject = "New Blog Post: " + blogTitle;
        String body = """
                <html>
                <body style="font-family: Arial, sans-serif; padding: 20px; background-color: #f9f9f9;">
                    <div style="background-color: white; padding: 20px; border-radius: 8px; max-width: 600px; margin: auto;">
                        <h2 style="color: #333;">A New Blog Post is Live!</h2>
                        <p style="color: #555;">Hi there,</p>
                        <p style="color: #555;">We just published a brand new post that you might find interesting: <strong>%s</strong>.</p>
                        <a href="%s" style="display: inline-block; background-color: #4CAF50; color: white; padding: 10px 20px; text-decoration: none; border-radius: 4px; margin-top: 15px;">Read the Post</a>
                        <p style="color: #999; font-size: 12px; margin-top: 30px;">You are receiving this because you subscribed to our blog updates.</p>
                    </div>
                </body>
                </html>
                """.formatted(blogTitle, blogLink);
        sendEmail(to, subject, body);
    }
}
