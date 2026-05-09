# Password Policy

All passwords for accounts on the IAM Portfolio platform must meet the
following rules:

- Minimum length: 12 characters.
- Must contain at least one uppercase letter, one lowercase letter, one
  digit, and one symbol from the set !@#$%^&*()-_=+.
- Must not be one of the user's last 5 passwords.
- Maximum age before mandatory rotation: 180 days.
- After 5 consecutive failed login attempts within 15 minutes, the
  account is automatically suspended (lifecycle state SUSPENDED) and
  a notification is sent to the user's manager.
- Privileged accounts (anyone holding ROLE_ADMIN or
  ROLE_IDENTITY_MANAGER) MUST also enable two-factor authentication
  (TOTP) within 24 hours of role assignment.

Recovery is via the standard /api/auth/forgot-password endpoint;
support staff cannot reset passwords on a user's behalf without an
approval ticket from the user's manager.
