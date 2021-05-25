package com.example.email_classificator

import android.accounts.AccountManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.email_classificator.Utils.Companion.PREF_ACCOUNT_NAME
import com.example.email_classificator.Utils.Companion.REQUEST_ACCOUNT_PICKER
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.gmail.Gmail
import com.google.api.services.gmail.GmailScopes
import com.google.api.services.gmail.model.Message
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.nio.charset.StandardCharsets


class MainActivity : AppCompatActivity() {

    private lateinit var mCredential: GoogleAccountCredential

    private val scopes = arrayOf(
        GmailScopes.GMAIL_LABELS,
        GmailScopes.GMAIL_COMPOSE,
        GmailScopes.GMAIL_INSERT,
        GmailScopes.GMAIL_MODIFY,
        GmailScopes.GMAIL_READONLY,
        GmailScopes.MAIL_GOOGLE_COM
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        init()

        chooseAccount()
    }

    private fun init() {
        mCredential = GoogleAccountCredential.usingOAuth2(
            applicationContext, scopes.toList()
        ).setBackOff(ExponentialBackOff())
    }

    private fun chooseAccount() {
        startActivityForResult(
            mCredential.newChooseAccountIntent(),
            REQUEST_ACCOUNT_PICKER
        )

    }

    private fun getResultsFromApi() {
        if (!isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices()
        } else {
            CoroutineScope(Dispatchers.IO).launch {
                makeRequest()
            }
        }
    }

    private fun makeRequest() {
        val transport = AndroidHttp.newCompatibleTransport()
        val jsonFactory: JsonFactory =
            JacksonFactory.getDefaultInstance()
        val mService = Gmail.Builder(
            transport, jsonFactory, mCredential
        ).setApplicationName(resources.getString(R.string.app_name)).build()

        try {
            val response = mService.users().messages().list("me").execute()
            val messageListBase64 = response.messages
            val messageList = messageListBase64.mapNotNull {
                try {
                    convertFromBase64ToString(
                        mService.users().messages().get("me", it.id).execute()
                    )
                } catch (e: Exception) {
                    null
                }
            }
            messageList.forEach {
                Log.i("Message", it)
            }

        } catch (e: Exception) {
            Log.e("Error", e.toString())
        }
    }

    private fun convertFromBase64ToString(message: Message): String {
        val data: ByteArray = Base64.decode(message.payload.parts[0].body.data, Base64.DEFAULT)
        return String(data, StandardCharsets.UTF_8)
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_ACCOUNT_PICKER -> if (resultCode == RESULT_OK && data != null && data.extras != null) {
                val accountName: String? = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
                val settings = getPreferences(Context.MODE_PRIVATE)
                val editor = settings.edit()
                editor.putString(PREF_ACCOUNT_NAME, accountName)
                editor.apply()
                mCredential.selectedAccountName = accountName
                getResultsFromApi()

            }
            else -> Log.i("Error", "Something went wrong")
        }
    }

    private fun isGooglePlayServicesAvailable(): Boolean {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(this)
        return connectionStatusCode == ConnectionResult.SUCCESS
    }

    private fun acquireGooglePlayServices() {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(this)
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            Log.i("ErrorGooglePlayServices", connectionStatusCode.toString())
        }
    }

}



