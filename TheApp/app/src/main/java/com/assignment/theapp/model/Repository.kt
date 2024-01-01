package com.assignment.theapp.model

class Repository(private val apiManager: ApiManager, private val taskDao: TaskDao) {

    suspend fun login(username: String, password: String): String? {
        return apiManager.login(username, password)
    }

    suspend fun getTasks(accessToken: String, swipeRefreshing: Boolean): List<Task> {
        val localTasks = taskDao.getAllTasks()

        return if (localTasks.isEmpty() || swipeRefreshing) {
            try {
                val remoteTasks = apiManager.makeAuthorizedRequest(accessToken)
                if (remoteTasks != null) {
                    if (!swipeRefreshing) {
                        taskDao.deleteAllTasks()
                        taskDao.insertTasks(remoteTasks.map { task ->
                            TaskEntity(
                                0,
                                task.task,
                                task.title,
                                task.description,
                                task.colorCode
                            )
                        })
                    }
                    remoteTasks
                } else {
                    emptyList()
                }
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }
    suspend fun getTasksFromDatabase(): List<Task> {
        return taskDao.getAllTasks().map { taskEntity ->
            Task(
                taskEntity.task,
                taskEntity.title,
                taskEntity.description,
                taskEntity.colorCode
            )
        }
    }
}
