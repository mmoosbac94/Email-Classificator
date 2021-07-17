package com.example.email_classificator

import android.content.Context
import android.util.Base64
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.email_classificator.Utils.Companion.scopes
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.gmail.Gmail
import com.google.api.services.gmail.model.Message
import org.tensorflow.lite.task.text.nlclassifier.BertNLClassifier
import java.nio.charset.StandardCharsets

class MainViewModel : ViewModel() {

    lateinit var mCredential: GoogleAccountCredential
    private lateinit var classifier: BertNLClassifier

    fun isGooglePlayServicesAvailable(context: Context): Boolean {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(context)
        return connectionStatusCode == ConnectionResult.SUCCESS
    }

    fun acquireGooglePlayServices(context: Context) {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(context)
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            Log.i("ErrorGooglePlayServices", connectionStatusCode.toString())
        }
    }

    fun startAuthentication(context: Context) {
        mCredential = GoogleAccountCredential.usingOAuth2(
            context, scopes.toList()
        ).setBackOff(ExponentialBackOff())
    }

    private fun createGmailService(): Gmail {
        val transport = AndroidHttp.newCompatibleTransport()
        val jsonFactory: JsonFactory =
            JacksonFactory.getDefaultInstance()

        return Gmail.Builder(
            transport, jsonFactory, mCredential
        ).build()
    }

    fun makeGmailAPIRequest(): List<CardItem> {
        val gmailService = createGmailService()
        val response = gmailService.users().messages().list("me").execute()
        val messageListBase64 = response.messages
        val messageList = getMessageList(messageListBase64, gmailService)

        return messageList.map {
            val result = classifier.classify(it).toString()
            CardItem(it, result)
        }

    }

    private fun getMessageList(
        messageListBase64: List<Message>,
        gmailService: Gmail
    ): List<String> {
        return messageListBase64.mapNotNull {
            try {
                convertFromBase64ToString(
                    gmailService.users().messages().get("me", it.id).execute()
                )
            } catch (e: Exception) {
                null
            }
        }
    }

    fun prepareBERTClassifier(context: Context) {
        val bertNLClassifier =
            BertNLClassifier.BertNLClassifierOptions.builder().setMaxSeqLen(512).build()

        classifier =
            BertNLClassifier.createFromFileAndOptions(
                context,
                "model.tflite",
                bertNLClassifier
            )
    }

    private fun convertFromBase64ToString(message: Message): String {
        val data: ByteArray = Base64.decode(message.payload.parts[0].body.data, Base64.DEFAULT)
        return String(data, StandardCharsets.UTF_8)
    }


}