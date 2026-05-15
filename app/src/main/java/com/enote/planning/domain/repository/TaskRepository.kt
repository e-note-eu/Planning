package com.enote.planning.domain.repository

import com.enote.planning.domain.model.Task
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    fun getAllTasks(): Flow<List<Task>>
    suspend fun insertTask(task: Task)
    suspend fun updateTask(task: Task)
    suspend fun updateTasks(tasks: List<Task>)
    suspend fun deleteTask(task: Task)
    suspend fun getMinPosition(): Int
}
