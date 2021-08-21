package com.example.email_classificator

import android.accounts.AccountManager
import android.content.Context
import android.content.Intent
import android.opengl.Visibility
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
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
import org.mortbay.jetty.Main


class MainActivity : AppCompatActivity() {

    private lateinit var mainViewModel: MainViewModel

    private lateinit var binding: ActivityMainBinding

    private lateinit var recyclerviewItemAdapter: RecyclerviewItemAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        setupRecyclerView()

        mainViewModel.prepareBERTClassifier(applicationContext)

        mainViewModel.startAuthentication(applicationContext)
        chooseAccount()
    }

    private fun setupRecyclerView() {
        val recyclerView = binding.recyclerEmailCardView
        recyclerView.setHasFixedSize(true)
        val layoutManager: RecyclerView.LayoutManager =
            LinearLayoutManager(applicationContext)

        recyclerView.layoutManager = layoutManager
        recyclerView.itemAnimator = DefaultItemAnimator()

        recyclerviewItemAdapter = RecyclerviewItemAdapter(emptyList())
        recyclerView.adapter = recyclerviewItemAdapter
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.options_menu_personal -> {
                val listPersonalMails = mainViewModel.getOnlyPersonalMails()
                recyclerviewItemAdapter.itemList = listPersonalMails
                recyclerviewItemAdapter.notifyDataSetChanged()
                true
            }
            R.id.options_menu_non_personal -> {
                val listNonPersonalMails = mainViewModel.getOnlyNonPersonalMails()
                recyclerviewItemAdapter.itemList = listNonPersonalMails
                recyclerviewItemAdapter.notifyDataSetChanged()
                true
            }
            R.id.options_menu_all_mails -> {
                recyclerviewItemAdapter.itemList = mainViewModel.data
                recyclerviewItemAdapter.notifyDataSetChanged()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
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
            binding.progressBar.visibility = View.VISIBLE
            GlobalScope.launch(Dispatchers.IO) {
                mainViewModel.makeGmailAPIRequest()
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.INVISIBLE
                    recyclerviewItemAdapter.itemList = mainViewModel.data
                    recyclerviewItemAdapter.notifyDataSetChanged()
                }
            }
        }
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