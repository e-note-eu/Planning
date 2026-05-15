package com.enote.planning.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.enote.planning.data.TaskDatabase
import com.enote.planning.data.TaskRepositoryImpl
import com.enote.planning.domain.model.Task
import com.enote.planning.domain.usecase.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TaskViewModel(application: Application) : AndroidViewModel(application) {
    
    private val useCases: TaskUseCases

    // Local state for dragging to make it fluid
    private val _localTasksOrder = MutableStateFlow<List<Task>?>(null)
    private val _dbTasks = MutableStateFlow<List<Task>>(emptyList())

    val tasks: StateFlow<List<Task>> = combine(_dbTasks, _localTasksOrder) { dbList, localList ->
        localList ?: dbList
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        val database = TaskDatabase.getDatabase(application)
        val repository = TaskRepositoryImpl(database.taskDao())
        
        useCases = TaskUseCases(
            getTasks = GetTasksUseCase(repository),
            addTask = AddTaskUseCase(repository),
            deleteTask = DeleteTaskUseCase(repository),
            toggleTask = ToggleTaskUseCase(repository),
            reorderTasks = ReorderTasksUseCase(repository)
        )

        viewModelScope.launch {
            useCases.getTasks().collect {
                _dbTasks.value = it
            }
        }
    }

    fun addTask(title: String, description: String) {
        viewModelScope.launch {
            useCases.addTask(title, description)
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            useCases.deleteTask(task)
        }
    }

    fun toggleTaskCompletion(task: Task) {
        viewModelScope.launch {
            useCases.toggleTask(task)
        }
    }

    fun onMove(fromIndex: Int, toIndex: Int) {
        val currentList = tasks.value.toMutableList()
        // Ensure we only reorder uncompleted tasks
        val uncompletedCount = currentList.count { !it.isCompleted }
        if (fromIndex >= uncompletedCount || toIndex >= uncompletedCount) return

        val item = currentList.removeAt(fromIndex)
        currentList.add(toIndex, item)
        _localTasksOrder.value = currentList
    }

    fun onDragEnd() {
        val orderToSave = _localTasksOrder.value
        if (orderToSave != null) {
            viewModelScope.launch {
                useCases.reorderTasks(orderToSave)
                _localTasksOrder.value = null // Switch back to DB flow once saved
            }
        }
    }
}
