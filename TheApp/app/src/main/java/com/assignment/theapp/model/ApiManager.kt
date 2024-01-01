package com.assignment.theapp.model

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class ApiManager {
    private val client = OkHttpClient()

    suspend fun login(username: String, password: String): String? {
        return withContext(Dispatchers.IO) {
            val mediaType = "application/json".toMediaTypeOrNull()
            val body = RequestBody.create(mediaType, """
                {
                    "username": "$username",
                    "password": "$password"
                }
            """.trimIndent())

            val request = Request.Builder()
                .url("https://api.baubuddy.de/index.php/login")
                .post(body)
                .addHeader("Authorization", "Basic QVBJX0V4cGxvcmVyOjEyMzQ1NmlzQUxhbWVQYXNz")
                .addHeader("Content-Type", "application/json")
                .build()

            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                val json = JSONObject(responseBody)

                return@withContext json.getJSONObject("oauth").getString("access_token")
            } else {
                return@withContext null
            }
        }
    }

    suspend fun makeAuthorizedRequest(accessToken: String): List<Task>? = suspendCancellableCoroutine { continuation ->
        GlobalScope.launch{
        withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url("https://api.baubuddy.de/dev/index.php/v1/tasks/select")
                    .addHeader("Authorization", "Bearer $accessToken")
                    .build()

                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    val responseBody = response.body?.string()

                    val tasks = parseResponse(responseBody)
                    continuation.resume(tasks)
                } else {
                    continuation.resume(null)
                }
            } catch (e: Exception) {
                continuation.resumeWithException(e)
            }
        }}
    }

    private fun parseResponse(responseBody: String?): List<Task>? {
        return try {
            val jsonArray = JSONArray(responseBody)
            val taskList = mutableListOf<Task>()

            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val taskk = jsonObject.getString("task")
                val taskTitle = jsonObject.getString("title")
                val taskDescription = jsonObject.getString("description")
                val taskColorCode = jsonObject.getString("colorCode")

                val task = Task(taskk, taskTitle, taskDescription, taskColorCode)
                taskList.add(task)
            }

            taskList
        } catch (e: JSONException) {
            e.printStackTrace()
            null
        }
    }
}
