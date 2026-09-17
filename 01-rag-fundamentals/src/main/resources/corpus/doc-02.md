# Resetting Your Password

If you cannot sign in to AcmeFlow, select **Forgot password** on the sign-in page and enter your registered email address. AcmeFlow sends a reset link that is valid for 30 minutes and can be used only once. For security, the confirmation screen appears even when no account exists for that address.

The new password must contain at least 12 characters and cannot match any of your previous five passwords. Completing a reset signs the account out of all browsers and mobile devices. API keys remain active and must be revoked separately from **Settings > Developer > API keys** if compromise is suspected.

Users who sign in exclusively through Google or Microsoft should reset their password with that identity provider. If an email does not arrive within five minutes, check the spam folder and confirm that messages from `no-reply@acmeflow.example` are permitted.

After five reset requests within one hour, further requests are temporarily blocked for 60 minutes. Support can help restore access, but agents will never ask for your current password or reset code.