# Leadership Compass email handover

Leadership Compass sends four kinds of email from one mailbox:

- A welcome note when a 5-week plan is created
- A reminder at the start of weeks 2, 3, 4, and 5 (8:00 Australia/Sydney, for each person’s newest plan)
- A 6-digit code to confirm a new account at sign-up. Sign-in stays email and password.
- A password-reset link

The mailbox is Gmail. Switching to Microsoft 365 does not require a code change. Change the environment variables on the API (the Azure Container App) and restart it.

Do not put the mailbox password in git or in `application.properties`.

## Current Gmail sender

Create a Google account used only for this app.

1. Turn on **2-Step Verification** for that Google account (Google Account, Security).
2. Create an **App password**: Google Account, Security, App passwords. Name it `Leadership Compass SMTP`. Google shows a 16-character password once. That value is `MAIL_PASSWORD`. The normal Gmail password will not work.
3. The From address must be that Gmail address. Gmail rejects mail that claims to be from `no-reply@theguineagroup.com.au` unless that address is added as a verified “Send mail as” alias. The display name is **Leadership Compass**. Replies go to `theteam@theguineagroup.com.au`.

Set these on the API:

| Variable | Value |
| --- | --- |
| `APP_MAIL_ENABLED` | `true` |
| `APP_MAIL_PROVIDER` | `smtp` |
| `MAIL_HOST` | `smtp.gmail.com` |
| `MAIL_PORT` | `587` |
| `MAIL_USERNAME` | the Gmail address |
| `MAIL_PASSWORD` | the app password |
| `MAIL_FROM` | the same Gmail address |
| `MAIL_REPLY_TO` | `theteam@theguineagroup.com.au` |
| `APP_PUBLIC_BASE_URL` | the API origin, used for unsubscribe links |
| `APP_PUBLIC_SITE_URL` | the public site origin, used for password-reset links |

`MAIL_FROM` defaults to `MAIL_USERNAME` when it is left unset.

A consumer Gmail account can send about 500 messages a day. Sign-up codes, resets, and plan reminders all count. Microsoft 365 is the way to raise that limit.

Until `APP_MAIL_ENABLED=true` and both SMTP credentials are set, plan emails are skipped and new accounts are created without an email code. After mail is on, sign-up asks for the code. Sign-in does not. Accounts created before this change can still sign in.

## Switch to Microsoft 365 SMTP

Use this when the mailbox is `no-reply@theguineagroup.com.au` (or another TGG mailbox) and SMTP AUTH is allowed on that mailbox.

1. In the Microsoft 365 admin centre, enable authenticated SMTP for the mailbox, or create an app password if the tenant requires one.
2. Replace the Gmail values:

| Variable | Value |
| --- | --- |
| `APP_MAIL_ENABLED` | `true` |
| `APP_MAIL_PROVIDER` | `smtp` |
| `MAIL_HOST` | `smtp.office365.com` |
| `MAIL_PORT` | `587` |
| `MAIL_USERNAME` | the Microsoft 365 mailbox, for example `no-reply@theguineagroup.com.au` |
| `MAIL_PASSWORD` | that mailbox’s password or app password |
| `MAIL_FROM` | the same mailbox address |

Leave `MAIL_REPLY_TO`, `APP_PUBLIC_BASE_URL`, and `APP_PUBLIC_SITE_URL` as they are. Remove the Gmail app password from the Container App so it is not still stored there.

The From address must be the authenticated mailbox. Display name and reply-to address stay the same.

## Switch to Microsoft Graph

Use this when SMTP AUTH is blocked and a Graph app registration is available.

The app registration needs application permission `Mail.Send` and an administrator must grant consent. The sender mailbox is the user the app sends as.

| Variable | Value |
| --- | --- |
| `APP_MAIL_ENABLED` | `true` |
| `APP_MAIL_PROVIDER` | `graph` |
| `GRAPH_TENANT_ID` | the Microsoft 365 tenant id |
| `GRAPH_CLIENT_ID` | the app registration client id |
| `GRAPH_CLIENT_SECRET` | the app registration client secret |
| `GRAPH_SENDER` | the mailbox to send as, for example `no-reply@theguineagroup.com.au` |
| `MAIL_FROM` | the same mailbox address |

Graph does not use `MAIL_HOST`, `MAIL_USERNAME`, or `MAIL_PASSWORD`. Those SMTP values can be removed.

Plan reminders, sign-up codes, and password resets all use this same setting. Unsubscribe links stay in the message body.
