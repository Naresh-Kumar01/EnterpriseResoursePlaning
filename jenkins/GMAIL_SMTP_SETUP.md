# Gmail SMTP — Jenkins Email Extension Setup

## 1. Create Gmail App Password

1. Google Account → **Security** → enable **2-Step Verification**.
2. **App passwords** → Create → name: `Jenkins EnterpriseResourcePlanning`.
3. Copy the 16-character password (no spaces).

## 2. Jenkins Global SMTP

**Manage Jenkins → System → Extended E-mail Notification**

| Setting | Value |
|---------|--------|
| SMTP server | `smtp.gmail.com` |
| Default user E-mail suffix | `@gmail.com` |
| Use SMTP Authentication | ✓ |
| User Name | `Nareshsofttechh@gmail.com` |
| Password | *App Password* |
| Use TLS | ✓ |
| SMTP Port | `587` |
| Default Recipients | `Nareshsofttechh@gmail.com` |
| Default Content Type | `text/html` |

Click **Test configuration** → Send test email.

## 3. Standard E-mail Notification (optional)

**Manage Jenkins → System → E-mail Notification**

- SMTP server: `smtp.gmail.com`
- Use SMTP Authentication: ✓
- User Name / Password: same as above
- Use TLS: ✓
- Port: `587`
- Reply-To Address: your Gmail

## 4. Job Environment Variable

In Jenkins job → **Environment variables**:

```
NOTIFICATION_EMAIL = team-lead@gmail.com,sdet@gmail.com
```

## 5. Email Content (from Jenkinsfile)

- **SUCCESS** / **FAILURE** subject with build number and branch
- HTML body with Extent report link
- Attachments: Extent HTML, screenshots, Cucumber HTML, Surefire XML
- Console log attached (`attachLog: true`)

## Troubleshooting

| Issue | Fix |
|-------|-----|
| Authentication failed | Use App Password, not Gmail password |
| Less secure apps | Deprecated — use App Password only |
| Email not sent | Check Jenkins System Log for `MessagingException` |
| Empty attachments | Run build once; verify `test-output/extent-reports/SparkReport.html` exists |
