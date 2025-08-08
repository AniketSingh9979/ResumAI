# Mail Service Troubleshooting Guide

## Issue Summary
**Error**: `Failed to send email to candidate: aniketvirat9979@gmail.com - Error: Mail server connection failed. Failed messages: jakarta.mail.MessagingException: Exception reading response`

## Root Causes Identified

### 1. Missing API Endpoint ✅ FIXED
- **Problem**: Backend was calling `/api/mail/interview-result` but mail service only had `/selected` and `/not-selected` endpoints
- **Solution**: Added the missing endpoint in `MailController.java`

### 2. Gmail App Password Format ✅ FIXED
- **Problem**: Gmail app password had spaces: `wazo ufim rlia wuiv`
- **Solution**: Removed spaces: `wazoufimrliawuiv`

### 3. Gmail SMTP Connection Issues ⚠️ ONGOING
- **Problem**: Gmail SMTP is rejecting connections with "Exception reading response"
- **Common Causes**:
  - Google's enhanced security measures
  - App password may need regeneration
  - Account may need "Less secure app access" enabled
  - 2FA configuration issues

## Current Configuration

### Temporary Solution (Active)
```properties
# Using localhost SMTP for testing (no real emails sent)
spring.mail.host=localhost
spring.mail.port=1025
spring.mail.from=test@resumai.com
spring.mail.properties.mail.smtp.auth=false
```

### Gmail Configuration (Disabled for Troubleshooting)
```properties
# Gmail SMTP (currently disabled due to connection issues)
#spring.mail.host=smtp.gmail.com
#spring.mail.port=587
#spring.mail.username=mail.ctstest@gmail.com
#spring.mail.password=wazoufimrliawuiv
#spring.mail.from=mail.ctstest@gmail.com
```

## Solutions to Try

### Option 1: Fix Gmail Configuration

1. **Regenerate App Password**:
   - Go to Google Account Settings → Security → 2-Step Verification → App passwords
   - Generate a new 16-character app password
   - Update `application.properties` with the new password (no spaces)

2. **Verify Gmail Account Settings**:
   - Ensure 2-Factor Authentication is enabled
   - Ensure "Less secure app access" is enabled (if available)
   - Check if account has been flagged for suspicious activity

3. **Update SMTP Properties**:
   ```properties
   spring.mail.host=smtp.gmail.com
   spring.mail.port=587
   spring.mail.username=mail.ctstest@gmail.com
   spring.mail.password=YOUR_NEW_APP_PASSWORD
   spring.mail.properties.mail.smtp.auth=true
   spring.mail.properties.mail.smtp.starttls.enable=true
   spring.mail.properties.mail.smtp.starttls.required=true
   spring.mail.properties.mail.smtp.ssl.trust=smtp.gmail.com
   ```

### Option 2: Use Alternative SMTP Provider

Consider using a more reliable email service:

#### SendGrid
```properties
spring.mail.host=smtp.sendgrid.net
spring.mail.port=587
spring.mail.username=apikey
spring.mail.password=YOUR_SENDGRID_API_KEY
```

#### Mailgun
```properties
spring.mail.host=smtp.mailgun.org
spring.mail.port=587
spring.mail.username=YOUR_MAILGUN_USERNAME
spring.mail.password=YOUR_MAILGUN_PASSWORD
```

### Option 3: Use Local SMTP for Development

For development/testing purposes, use MailHog:

1. **Install MailHog**:
   ```bash
   # Windows (using Chocolatey)
   choco install mailhog
   
   # Or download from: https://github.com/mailhog/MailHog
   ```

2. **Run MailHog**:
   ```bash
   mailhog
   ```

3. **Configure Application**:
   ```properties
   spring.mail.host=localhost
   spring.mail.port=1025
   spring.mail.properties.mail.smtp.auth=false
   ```

4. **View Emails**: http://localhost:8025

## Testing the Mail Service

### Test Endpoint
Use the new test endpoint to verify email configuration:

```bash
POST http://localhost:8082/mail-service/api/mail/test?email=your-test-email@example.com
```

### Debug Logs
Enable debug logging to see detailed SMTP communication:

```properties
spring.mail.properties.mail.debug=true
logging.level.org.springframework.mail=DEBUG
```

## Recommendations

1. **Immediate Fix**: Keep localhost SMTP for development until Gmail issues are resolved
2. **Production**: Use a professional email service like SendGrid or Mailgun for reliability
3. **Gmail Issues**: If you must use Gmail, regenerate the app password and verify account security settings
4. **Monitoring**: Add proper error handling and fallback mechanisms

## Files Modified

1. `mail-service/src/main/resources/application.properties` - Updated SMTP configuration
2. `mail-service/src/main/java/com/resumai/mailservice/controller/MailController.java` - Added `/interview-result` endpoint
3. `mail-service/src/main/java/com/resumai/mailservice/service/MailService.java` - Added test email method and improved error handling

## Next Steps

1. Test the current localhost configuration to ensure the application flow works
2. If Gmail is required, follow the Gmail troubleshooting steps above
3. Consider implementing a proper email service provider for production use
4. Add email queue/retry mechanism for better reliability 