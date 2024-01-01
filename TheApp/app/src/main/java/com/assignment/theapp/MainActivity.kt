package com.assignment.theapp

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.work.Configuration
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.assignment.theapp.databinding.ActivityMainBinding
import com.assignment.theapp.model.ApiManager
import com.assignment.theapp.model.AppDatabase
import com.assignment.theapp.model.QRCodeScannerActivity
import com.assignment.theapp.model.RefreshWorker
import com.assignment.theapp.model.RefreshWorkerFactory
import com.assignment.theapp.model.Repository
import com.assignment.theapp.view.TaskAdapter
import com.assignment.theapp.viewmodel.TaskViewModel
import com.assignment.theapp.viewmodel.TaskViewModelFactory
import java.util.concurrent.TimeUnit

class MainActivity() : AppCompatActivity(){

    private lateinit var viewModel: TaskViewModel
    private lateinit var binding: ActivityMainBinding
    private val adapter = TaskAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        val database = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "app_database").build()
        val taskDao = database.taskDao()

        val apiManager = ApiManager()
        val repository = Repository(apiManager, taskDao)
        viewModel = ViewModelProvider(this, TaskViewModelFactory(repository, applicationContext)).get(TaskViewModel::class.java)

        val myConfig = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .setWorkerFactory(RefreshWorkerFactory(viewModel))
            .build()

        WorkManager.initialize(this, myConfig)

        val workManager = WorkManager.getInstance(applicationContext)

        val refreshWorkRequest = PeriodicWorkRequestBuilder<RefreshWorker>(60, TimeUnit.MINUTES)
            .build()

        workManager.enqueue(refreshWorkRequest)

        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)


        viewModel.taskList.observe(this, Observer { tasks ->
            tasks?.let {
                adapter.submitList(tasks)
            }
        })

        binding.loginButton.setOnClickListener {
            viewModel.login("365", "1")
        }

        binding.authorizedRequestButton.setOnClickListener {
            viewModel.refreshData()
        }
        binding.getDataFromDbButton.setOnClickListener {
            viewModel.getDataFromDb()
        }

        val swipeRefreshLayout = findViewById<SwipeRefreshLayout>(R.id.swipeRefreshLayout)

        viewModel.swipeRefreshing.observe(this, { isRefreshing ->
            swipeRefreshLayout.isRefreshing = isRefreshing
        })
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)

        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter(newText)
                return true
            }
        })

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_search -> true
            R.id.menu_scan_qr_code -> {
                val intent = Intent(this, QRCodeScannerActivity::class.java)
                startActivityForResult(intent, REQUEST_CODE_QR_SCAN)
                return true
        }

            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
    }
    companion object {
        private const val REQUEST_CODE_QR_SCAN = 123
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_QR_SCAN && resultCode == Activity.RESULT_OK) {
            val scannedText = data?.getStringExtra("scannedText")
            adapter.filter(scannedText)
        }
    }

    /*override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .setWorkerFactory(RefreshWorkerFactory(viewModel))
            .build()
    }*/
}


