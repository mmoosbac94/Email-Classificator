package com.example.email_classificator

import android.accounts.AccountManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.email_classificator.Utils.Companion.PREF_ACCOUNT_NAME
import com.example.email_classificator.Utils.Companion.REQUEST_ACCOUNT_PICKER
import com.example.email_classificator.databinding.ActivityMainBinding
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import kotlinx.coroutines.*


class MainActivity : AppCompatActivity() {

    private lateinit var mainViewModel: MainViewModel

    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        //Task library currently only support the default seq_len value (128)

        //MaxSeqLen has been added to the Java/Swift(will be available tomorrow externally) API

        mainViewModel.prepareBERTClassifier(applicationContext)

        mainViewModel.startAuthentication(applicationContext)
        chooseAccount()
    }


    private fun chooseAccount() {
        startActivityForResult(
            mainViewModel.mCredential.newChooseAccountIntent(),
            REQUEST_ACCOUNT_PICKER
        )
    }

    private fun processAll() {
        if (!mainViewModel.isGooglePlayServicesAvailable(applicationContext)) {
            mainViewModel.acquireGooglePlayServices(applicationContext)
        } else {
            GlobalScope.launch(Dispatchers.IO) {
                val data = getData()
                withContext(Dispatchers.Main) {
                    val recyclerviewItemAdapter = RecyclerviewItemAdapter(data)
                    val recyclerView = binding.recyclerEmailCardView
                    recyclerView.setHasFixedSize(true)
                    val layoutManager: RecyclerView.LayoutManager =
                        LinearLayoutManager(applicationContext)
                    recyclerView.layoutManager = layoutManager
                    recyclerView.itemAnimator = DefaultItemAnimator()
                    recyclerView.adapter = recyclerviewItemAdapter
                }
            }
        }
    }

    private fun getData(): List<CardItem> {
        try {
            return mainViewModel.makeGmailAPIRequest()
        } catch (e: UserRecoverableAuthIOException) {
            Log.e("Fehler", e.toString())
            startActivityForResult(e.intent, REQUEST_ACCOUNT_PICKER)
        }
        return emptyList()
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
                mainViewModel.mCredential.selectedAccountName = accountName
                processAll()

            }
            else -> Log.i("Error", "Something went wrong")
        }
    }

}