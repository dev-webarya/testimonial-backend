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

  @org.springframework.beans.factory.annotation.Value("${app.admin.username:admin}")
  private String adminEmail;

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
      log.error("Failed to send email to {} (subject: {}): {}", to, subject, e.getMessage(), e);
      throw new RuntimeException("Failed to send email to " + to + ": " + e.getMessage(), e);
    } catch (Exception e) {
      log.error("Unexpected error sending email to {} (subject: {}): {}", to, subject, e.getMessage(), e);
      throw new RuntimeException("Unexpected error sending email to " + to + ": " + e.getMessage(), e);
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
            <p>It is now live on our platform!</p>
        </body>
        </html>
        """.formatted(blogTitle);
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
        """
        .formatted(blogTitle, blogLink);
    sendEmail(to, subject, body);
  }

  /**
   * Send blog subscription confirmation email to the user.
   */
  public void sendBlogSubscriptionConfirmation(String to) {
    String subject = "You're subscribed! — A Star Classes Blog";
    String body = """
        <html>
        <body style="font-family: Arial, sans-serif; padding: 20px; background-color: #f9f9f9;">
            <div style="background-color: white; padding: 20px; border-radius: 8px; max-width: 600px; margin: auto;">
                <h2 style="color: #333;">Welcome to the A Star Classes Blog!</h2>
                <p style="color: #555;">Hi there,</p>
                <p style="color: #555;">Thank you for subscribing! You will now receive notifications whenever we publish a new blog post.</p>
                <p style="color: #999; font-size: 12px; margin-top: 30px;">If you didn't request this subscription, please ignore this email.</p>
            </div>
        </body>
        </html>
        """;
    sendEmail(to, subject, body);
  }

  /**
   * Send Contact Us request notification to the administrator.
   */
  public void sendContactUsAdminNotification(String fullName, String emailAddress, String phoneNumber,
      String subjectName, String messageText) {
    String subject = "A Star Classes Contact Us Request: from " + fullName;
    String htmlBody = """
        <!DOCTYPE html>
        <html>
        <head>
          <meta charset="utf-8">
          <title>A Star Classes - Contact Us Request</title>
          <style>
            body {
              font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
              background-color: #f8fafc;
              margin: 0;
              padding: 0;
              -webkit-font-smoothing: antialiased;
            }
            .wrapper {
              width: 100%%;
              background-color: #f8fafc;
              padding: 40px 0;
            }
            .container {
              max-width: 600px;
              margin: 0 auto;
              background-color: #ffffff;
              border-radius: 12px;
              overflow: hidden;
              box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
              border: 1px solid #e2e8f0;
            }
            .header {
              background-color: #0f172a;
              padding: 24px 32px;
              border-bottom: 4px solid #fbbf24;
            }
            .header h1 {
              color: #ffffff;
              margin: 0;
              font-size: 22px;
              font-weight: 600;
              letter-spacing: 0.5px;
            }
            .content {
              padding: 32px;
            }
            .badge {
              display: inline-block;
              padding: 6px 12px;
              background-color: #eff6ff;
              color: #2563eb;
              font-size: 12px;
              font-weight: 600;
              border-radius: 9999px;
              margin-bottom: 16px;
              text-transform: uppercase;
              letter-spacing: 0.5px;
            }
            h2 {
              color: #1e293b;
              margin-top: 0;
              margin-bottom: 12px;
              font-size: 20px;
              font-weight: 700;
            }
            p.intro {
              color: #475569;
              font-size: 15px;
              line-height: 1.6;
              margin-bottom: 24px;
            }
            .details-table {
              width: 100%%;
              border-collapse: collapse;
              margin-bottom: 24px;
            }
            .details-table td {
              padding: 12px 0;
              border-bottom: 1px solid #f1f5f9;
              vertical-align: top;
              font-size: 14px;
            }
            .details-table td.label {
              color: #64748b;
              font-weight: 600;
              width: 30%%;
            }
            .details-table td.value {
              color: #1e293b;
              font-weight: 500;
            }
            .message-box {
              background-color: #f8fafc;
              border-left: 4px solid #3b82f6;
              border-radius: 4px;
              padding: 16px 20px;
              margin-top: 8px;
            }
            .message-title {
              font-size: 13px;
              color: #64748b;
              font-weight: 600;
              text-transform: uppercase;
              margin-bottom: 8px;
              letter-spacing: 0.5px;
            }
            .message-text {
              color: #334155;
              font-size: 14px;
              line-height: 1.6;
              white-space: pre-wrap;
              font-style: italic;
            }
            .footer {
              background-color: #f8fafc;
              padding: 24px 32px;
              border-top: 1px solid #e2e8f0;
              text-align: center;
            }
            .footer p {
              margin: 0;
              color: #64748b;
              font-size: 12px;
              line-height: 1.5;
            }
          </style>
        </head>
        <body>
          <div class="wrapper">
            <div class="container">
              <div class="header">
                <h1>A Star Classes</h1>
              </div>
              <div class="content">
                <span class="badge">Contact Us</span>
                <h2>New Inquiry Received</h2>
                <p class="intro">An online visitor has just submitted a "Contact Us" form request. Below are the details:</p>

                <table class="details-table">
                  <tr>
                    <td class="label">Full Name</td>
                    <td class="value">%s</td>
                  </tr>
                  <tr>
                    <td class="label">Email</td>
                    <td class="value"><a href="mailto:%s" style="color: #2563eb; text-decoration: none;">%s</a></td>
                  </tr>
                  <tr>
                    <td class="label">Phone Number</td>
                    <td class="value">%s</td>
                  </tr>
                  <tr>
                    <td class="label">Subject</td>
                    <td class="value">%s</td>
                  </tr>
                </table>

                <div class="message-title">Message Details</div>
                <div class="message-box">
                  <div class="message-text">%s</div>
                </div>
              </div>
              <div class="footer">
                <p>This is an automated administrative notification. Please do not reply directly to this email.</p>
                <p style="margin-top: 6px;">&copy; 2026 A Star Classes. All rights reserved.</p>
              </div>
            </div>
          </div>
        </body>
        </html>
        """
        .formatted(
            fullName,
            emailAddress,
            emailAddress,
            phoneNumber != null && !phoneNumber.trim().isEmpty() ? phoneNumber : "Not Provided",
            subjectName,
            messageText);
    sendEmail(adminEmail, subject, htmlBody);
  }

  /**
   * Send Schedule Demo request notification to the administrator.
   */
  public void sendScheduleDemoAdminNotification(
      String studentName,
      String parentName,
      String emailId,
      String mobileNumber,
      String boardName,
      String gradeName,
      String preferredDate,
      String preferredTime) {
    String subject = "A Star Classes Scheduling Demo Request: from " + studentName;
    String htmlBody = """
        <!DOCTYPE html>
        <html>
        <head>
          <meta charset="utf-8">
          <title>A Star Classes - Schedule Demo Request</title>
          <style>
            body {
              font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
              background-color: #f8fafc;
              margin: 0;
              padding: 0;
              -webkit-font-smoothing: antialiased;
            }
            .wrapper {
              width: 100%%;
              background-color: #f8fafc;
              padding: 40px 0;
            }
            .container {
              max-width: 600px;
              margin: 0 auto;
              background-color: #ffffff;
              border-radius: 12px;
              overflow: hidden;
              box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
              border: 1px solid #e2e8f0;
            }
            .header {
              background-color: #0f172a;
              padding: 24px 32px;
              border-bottom: 4px solid #10b981;
            }
            .header h1 {
              color: #ffffff;
              margin: 0;
              font-size: 22px;
              font-weight: 600;
              letter-spacing: 0.5px;
            }
            .content {
              padding: 32px;
            }
            .badge {
              display: inline-block;
              padding: 6px 12px;
              background-color: #ecfdf5;
              color: #059669;
              font-size: 12px;
              font-weight: 600;
              border-radius: 9999px;
              margin-bottom: 16px;
              text-transform: uppercase;
              letter-spacing: 0.5px;
            }
            h2 {
              color: #1e293b;
              margin-top: 0;
              margin-bottom: 12px;
              font-size: 20px;
              font-weight: 700;
            }
            p.intro {
              color: #475569;
              font-size: 15px;
              line-height: 1.6;
              margin-bottom: 24px;
            }
            .details-table {
              width: 100%%;
              border-collapse: collapse;
              margin-bottom: 8px;
            }
            .details-table td {
              padding: 12px 0;
              border-bottom: 1px solid #f1f5f9;
              vertical-align: top;
              font-size: 14px;
            }
            .details-table td.label {
              color: #64748b;
              font-weight: 600;
              width: 35%%;
            }
            .details-table td.value {
              color: #1e293b;
              font-weight: 500;
            }
            .footer {
              background-color: #f8fafc;
              padding: 24px 32px;
              border-top: 1px solid #e2e8f0;
              text-align: center;
            }
            .footer p {
              margin: 0;
              color: #64748b;
              font-size: 12px;
              line-height: 1.5;
            }
          </style>
        </head>
        <body>
          <div class="wrapper">
            <div class="container">
              <div class="header">
                <h1>A Star Classes</h1>
              </div>
              <div class="content">
                <span class="badge">Demo Class Request</span>
                <h2>New Demo Scheduled</h2>
                <p class="intro">A new demo class booking has been requested. Below are the details:</p>

                <table class="details-table">
                  <tr>
                    <td class="label">Student Name</td>
                    <td class="value">%s</td>
                  </tr>
                  <tr>
                    <td class="label">Parent Name</td>
                    <td class="value">%s</td>
                  </tr>
                  <tr>
                    <td class="label">Email Address</td>
                    <td class="value"><a href="mailto:%s" style="color: #2563eb; text-decoration: none;">%s</a></td>
                  </tr>
                  <tr>
                    <td class="label">Mobile Number</td>
                    <td class="value">%s</td>
                  </tr>
                  <tr>
                    <td class="label">Grade</td>
                    <td class="value">%s</td>
                  </tr>
                  <tr>
                    <td class="label">Education Board</td>
                    <td class="value">%s</td>
                  </tr>
                  <tr>
                    <td class="label">Preferred Date</td>
                    <td class="value">%s</td>
                  </tr>
                  <tr>
                    <td class="label">Preferred Time</td>
                    <td class="value">%s</td>
                  </tr>
                </table>
              </div>
              <div class="footer">
                <p>This is an automated administrative notification. Please do not reply directly to this email.</p>
                <p style="margin-top: 6px;">&copy; 2026 A Star Classes. All rights reserved.</p>
              </div>
            </div>
          </div>
        </body>
        </html>
        """.formatted(
        studentName,
        parentName,
        emailId,
        emailId,
        mobileNumber,
        gradeName,
        boardName,
        preferredDate,
        preferredTime);
    sendEmail(adminEmail, subject, htmlBody);
  }

  /**
   * Send Contact Us submission confirmation to the user.
   */
  public void sendContactUsUserConfirmation(String to, String fullName, String subjectName, String messageText) {
    String subject = "We've received your message — A Star Classes";
    String htmlBody = """
        <!DOCTYPE html>
        <html>
        <head>
          <meta charset="utf-8">
          <title>A Star Classes – Message Received</title>
          <style>
            body {
              font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
              background-color: #f8fafc;
              margin: 0;
              padding: 0;
              -webkit-font-smoothing: antialiased;
            }
            .wrapper { width: 100%%; background-color: #f8fafc; padding: 40px 0; }
            .container {
              max-width: 600px;
              margin: 0 auto;
              background-color: #ffffff;
              border-radius: 12px;
              overflow: hidden;
              box-shadow: 0 4px 12px rgba(0,0,0,0.06);
              border: 1px solid #e2e8f0;
            }
            .header {
              background: linear-gradient(135deg, #0f172a 0%%, #1e3a5f 100%%);
              padding: 32px 32px 24px;
              border-bottom: 4px solid #fbbf24;
              text-align: center;
            }
            .header h1 { color: #ffffff; margin: 0 0 4px; font-size: 24px; font-weight: 700; letter-spacing: 0.5px; }
            .header p  { color: #94a3b8; margin: 0; font-size: 13px; letter-spacing: 1px; text-transform: uppercase; }
            .content { padding: 36px 32px 28px; }
            .greeting { font-size: 18px; font-weight: 600; color: #1e293b; margin-bottom: 12px; }
            .intro    { color: #475569; font-size: 15px; line-height: 1.7; margin-bottom: 28px; }
            .card {
              background-color: #f8fafc;
              border: 1px solid #e2e8f0;
              border-radius: 10px;
              padding: 20px 24px;
              margin-bottom: 24px;
            }
            .card-title {
              font-size: 11px;
              font-weight: 700;
              text-transform: uppercase;
              letter-spacing: 1px;
              color: #94a3b8;
              margin-bottom: 14px;
            }
            .card table { width: 100%%; border-collapse: collapse; }
            .card td { padding: 8px 0; font-size: 14px; border-bottom: 1px solid #f1f5f9; vertical-align: top; }
            .card td.label { color: #64748b; font-weight: 600; width: 35%%; }
            .card td.value { color: #1e293b; font-weight: 500; }
            .message-quote {
              border-left: 3px solid #fbbf24;
              background-color: #fffbeb;
              border-radius: 0 6px 6px 0;
              padding: 14px 18px;
              color: #92400e;
              font-size: 14px;
              font-style: italic;
              line-height: 1.7;
              margin-top: 6px;
              white-space: pre-wrap;
            }
            .next-steps {
              background-color: #eff6ff;
              border-radius: 10px;
              padding: 18px 22px;
              margin-bottom: 24px;
            }
            .next-steps p { margin: 0; color: #1e40af; font-size: 14px; line-height: 1.7; }
            .signature { color: #475569; font-size: 14px; line-height: 1.7; }
            .signature strong { color: #1e293b; }
            .footer {
              background-color: #f8fafc;
              border-top: 1px solid #e2e8f0;
              padding: 20px 32px;
              text-align: center;
            }
            .footer p { margin: 0; color: #94a3b8; font-size: 12px; line-height: 1.6; }
          </style>
        </head>
        <body>
          <div class="wrapper">
            <div class="container">
              <div class="header">
                <h1>A Star Classes</h1>
                <p>Excellence in Education</p>
              </div>
              <div class="content">
                <div class="greeting">Dear %s,</div>
                <p class="intro">
                  Thank you for reaching out to us! We have successfully received your message and our team will
                  review it shortly. You can expect to hear back from us within <strong>1–2 business days</strong>.
                </p>

                <div class="card">
                  <div class="card-title">Your Submission Summary</div>
                  <table>
                    <tr>
                      <td class="label">Subject</td>
                      <td class="value">%s</td>
                    </tr>
                    <tr>
                      <td class="label">Your Message</td>
                      <td class="value"></td>
                    </tr>
                  </table>
                  <div class="message-quote">%s</div>
                </div>

                <div class="next-steps">
                  <p>&#128276; <strong>What happens next?</strong> One of our team members will review your inquiry and
                  get in touch with you at the email address you provided. In the meantime, feel free to explore
                  our courses and upcoming batches on our website.</p>
                </div>

                <div class="signature">
                  <p>Warm regards,</p>
                  <p><strong>The A Star Classes Team</strong><br>
                  Supporting Your Academic Journey</p>
                </div>
              </div>
              <div class="footer">
                <p>This is an automated confirmation. Please do not reply to this email.</p>
                <p style="margin-top: 6px;">&copy; 2026 A Star Classes. All rights reserved.</p>
              </div>
            </div>
          </div>
        </body>
        </html>
        """.formatted(fullName, subjectName, messageText);
    sendEmail(to, subject, htmlBody);
  }

  /**
   * Send Schedule Demo booking confirmation to the user.
   */
  public void sendScheduleDemoUserConfirmation(
      String to,
      String studentName,
      String parentName,
      String boardName,
      String gradeName,
      String preferredDate,
      String preferredTime) {
    String subject = "Your demo class is booked! — A Star Classes";
    String htmlBody = """
        <!DOCTYPE html>
        <html>
        <head>
          <meta charset="utf-8">
          <title>A Star Classes – Demo Booking Confirmed</title>
          <style>
            body {
              font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
              background-color: #f8fafc;
              margin: 0;
              padding: 0;
              -webkit-font-smoothing: antialiased;
            }
            .wrapper { width: 100%%; background-color: #f8fafc; padding: 40px 0; }
            .container {
              max-width: 600px;
              margin: 0 auto;
              background-color: #ffffff;
              border-radius: 12px;
              overflow: hidden;
              box-shadow: 0 4px 12px rgba(0,0,0,0.06);
              border: 1px solid #e2e8f0;
            }
            .header {
              background: linear-gradient(135deg, #0f172a 0%%, #064e3b 100%%);
              padding: 32px 32px 24px;
              border-bottom: 4px solid #10b981;
              text-align: center;
            }
            .header h1 { color: #ffffff; margin: 0 0 4px; font-size: 24px; font-weight: 700; letter-spacing: 0.5px; }
            .header p  { color: #6ee7b7; margin: 0; font-size: 13px; letter-spacing: 1px; text-transform: uppercase; }
            .content { padding: 36px 32px 28px; }
            .greeting { font-size: 18px; font-weight: 600; color: #1e293b; margin-bottom: 12px; }
            .intro    { color: #475569; font-size: 15px; line-height: 1.7; margin-bottom: 28px; }
            .badge {
              display: inline-flex;
              align-items: center;
              gap: 6px;
              background-color: #ecfdf5;
              color: #059669;
              font-size: 13px;
              font-weight: 600;
              padding: 8px 16px;
              border-radius: 9999px;
              border: 1px solid #a7f3d0;
              margin-bottom: 24px;
            }
            .card {
              background-color: #f8fafc;
              border: 1px solid #e2e8f0;
              border-radius: 10px;
              padding: 20px 24px;
              margin-bottom: 24px;
            }
            .card-title {
              font-size: 11px;
              font-weight: 700;
              text-transform: uppercase;
              letter-spacing: 1px;
              color: #94a3b8;
              margin-bottom: 14px;
            }
            .card table { width: 100%%; border-collapse: collapse; }
            .card td { padding: 9px 0; font-size: 14px; border-bottom: 1px solid #f1f5f9; vertical-align: top; }
            .card td.label { color: #64748b; font-weight: 600; width: 38%%; }
            .card td.value { color: #1e293b; font-weight: 500; }
            .highlight-row td { background-color: #f0fdf4; }
            .next-steps {
              background-color: #ecfdf5;
              border-radius: 10px;
              border: 1px solid #a7f3d0;
              padding: 18px 22px;
              margin-bottom: 24px;
            }
            .next-steps p { margin: 0; color: #065f46; font-size: 14px; line-height: 1.7; }
            .signature { color: #475569; font-size: 14px; line-height: 1.7; }
            .signature strong { color: #1e293b; }
            .footer {
              background-color: #f8fafc;
              border-top: 1px solid #e2e8f0;
              padding: 20px 32px;
              text-align: center;
            }
            .footer p { margin: 0; color: #94a3b8; font-size: 12px; line-height: 1.6; }
          </style>
        </head>
        <body>
          <div class="wrapper">
            <div class="container">
              <div class="header">
                <h1>A Star Classes</h1>
                <p>Excellence in Education</p>
              </div>
              <div class="content">
                <div class="greeting">Dear %s,</div>
                <p class="intro">
                  Wonderful news! &#127881; Your demo class request has been successfully received.
                  Our academic team will confirm your session and reach out to you shortly with further details.
                </p>

                <div class="badge">&#10003;&nbsp; Booking Request Received</div>

                <div class="card">
                  <div class="card-title">Booking Summary</div>
                  <table>
                    <tr>
                      <td class="label">Student Name</td>
                      <td class="value">%s</td>
                    </tr>
                    <tr>
                      <td class="label">Grade</td>
                      <td class="value">%s</td>
                    </tr>
                    <tr>
                      <td class="label">Education Board</td>
                      <td class="value">%s</td>
                    </tr>
                    <tr class="highlight-row">
                      <td class="label">Preferred Date</td>
                      <td class="value"><strong>%s</strong></td>
                    </tr>
                    <tr class="highlight-row">
                      <td class="label">Preferred Time</td>
                      <td class="value"><strong>%s</strong></td>
                    </tr>
                  </table>
                </div>

                <div class="next-steps">
                  <p>&#128276; <strong>Next Steps:</strong> Our team will review your preferred slot and contact you
                  to confirm the exact timing. Please keep an eye on your inbox. If you have any urgent queries,
                  feel free to reach out to us directly.</p>
                </div>

                <div class="signature">
                  <p>We look forward to welcoming you to A Star Classes!</p>
                  <p><strong>The A Star Classes Team</strong><br>
                  Supporting Your Academic Journey</p>
                </div>
              </div>
              <div class="footer">
                <p>This is an automated confirmation. Please do not reply to this email.</p>
                <p style="margin-top: 6px;">&copy; 2026 A Star Classes. All rights reserved.</p>
              </div>
            </div>
          </div>
        </body>
        </html>
        """.formatted(studentName, studentName, gradeName, boardName, preferredDate, preferredTime);
    sendEmail(to, subject, htmlBody);
  }
}
