# Exporting Workspace Data

Workspace owners and administrators can request an export from **Settings > Data management > Export**. An export contains projects, tasks, comments, member profiles, and audit events in JSON format. File attachments are included in their original formats. Passwords, API keys, billing card details, and deleted records are never included.

Exports are prepared asynchronously. Small workspaces usually finish within a few minutes, while large workspaces can take several hours. When the archive is ready, AcmeFlow emails the requester a download link. The link expires after 48 hours and requires the requester to sign in again.

Free workspaces can request one export every 30 days. Pro workspaces can request one every 24 hours. Only one export can be processed for a workspace at a time. Export preparation does not count against API request limits.

The downloaded ZIP archive is not encrypted, so it should be stored and shared securely. For an account-level copy of personal information rather than a full workspace export, contact privacy support at `privacy@acmeflow.example`.