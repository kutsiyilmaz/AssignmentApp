package com.assignment.theapp.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.assignment.theapp.model.Repository
import com.assignment.theapp.model.Task
import kotlinx.coroutines.launch

    class TaskViewModel(private val repository: Repository,private val context: Context) : ViewModel() {

        var toastStatus = MutableLiveData<Boolean?>()

        private val _isLoading = MutableLiveData<Boolean>()
        val isLoading: LiveData<Boolean> get() = _isLoading

        private val _error = MutableLiveData<String>()
        val error: LiveData<String> get() = _error

        private val _accessToken = MutableLiveData<String>()

        val accessToken: LiveData<String>
            get() = _accessToken

        private val _taskList = MutableLiveData<List<Task>>().apply { value = emptyList() }
        val taskList: LiveData<List<Task>> get() = _taskList

        private var _swipeRefreshing = MutableLiveData<Boolean>()
        val swipeRefreshing: LiveData<Boolean> get() = _swipeRefreshing

        fun refreshData() {
            viewModelScope.launch {
                _swipeRefreshing.value = true
                try {
                    val tasks = repository.getTasks(accessToken.value.toString(), swipeRefreshing.value!!)
                    _taskList.value = tasks
                } catch (e: Exception) {
                    _error.value = "Failed to fetch tasks"
                } finally {
                    _swipeRefreshing.value = false
                }
            }
        }

        fun login(username: String, password: String) {
            viewModelScope.launch {
                val token = repository.login(username, password)
                _accessToken.value = token.toString()
                toastStatus.value = true

            }
        }
        fun getDataFromDb() {
            viewModelScope.launch {
                val tasks = repository.getTasksFromDatabase()
                if (tasks.isNotEmpty()) {
                    _taskList.value = tasks
                } else {
                    _error.value = "Local database is empty."
                    Toast.makeText(
                        context,
                        "Local database is empty. Please first login and then swipe down to refresh or press authorize button.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

    }

