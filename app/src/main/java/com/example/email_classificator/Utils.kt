package com.example.email_classificator

import com.google.api.services.gmail.GmailScopes

class Utils {

    companion object {
        const val REQUEST_ACCOUNT_PICKER = 0
        const val PREF_ACCOUNT_NAME = "accountName"

        val scopes = arrayOf(
            GmailScopes.GMAIL_LABELS,
            GmailScopes.GMAIL_COMPOSE,
            GmailScopes.GMAIL_INSERT,
            GmailScopes.GMAIL_MODIFY,
            GmailScopes.GMAIL_READONLY,
            GmailScopes.MAIL_GOOGLE_COM
        )

    }

}