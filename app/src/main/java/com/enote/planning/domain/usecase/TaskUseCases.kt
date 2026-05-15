package com.enote.planning.domain.usecase

import com.enote.planning.domain.model.Task
import com.enote.planning.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class TaskUseCases(
    val getTasks: GetTasksUseCase,
    val addTask: AddTaskUseCase,
    val deleteTask: DeleteTaskUseCase,
    val toggleTask: ToggleTaskUseCase,
    val reorderTasks: ReorderTasksUseCase
)

class GetTasksUseCase(private val repository: TaskRepository) {
    operator fun invoke(): Flow<List<Task>> = repository.getAllTasks().map { tasks ->
        val uncompleted = tasks.filter { !it.isCompleted }.sortedBy { it.position }
        val completed = tasks.filter { it.isCompleted }.sortedBy { it.completedAt }
        uncompleted + completed
    }
}

class AddTaskUseCase(private val repository: TaskRepository) {
    suspend operator fun invoke(title: String, description: String) {
        if (title.isBlank()) return
        val capitalizedTitle = title.trim()
            .take(30)
            .replaceFirstChar { it.uppercase() }
        val capitalizedDescription = description.trim()
            .take(120)
            .replaceFirstChar { it.uppercase() }
        
        val minPos = repository.getMinPosition()
        repository.insertTask(
            Task(
                title = capitalizedTitle,
                description = capitalizedDescription,
                position = minPos - 1 // Always on top
            )
        )
    }
}

class DeleteTaskUseCase(private val repository: TaskRepository) {
    suspend operator fun invoke(task: Task) = repository.deleteTask(task)
}

class ToggleTaskUseCase(private val repository: TaskRepository) {
    suspend operator fun invoke(task: Task) {
        val isNowCompleted = !task.isCompleted
        repository.updateTask(
            task.copy(
                isCompleted = isNowCompleted,
                completedAt = if (isNowCompleted) System.currentTimeMillis() else null
            )
        )
    }
}

class ReorderTasksUseCase(private val repository: TaskRepository) {
    suspend operator fun invoke(uncompletedTasks: List<Task>) {
        val updatedTasks = uncompletedTasks.mapIndexed { index, task ->
            task.copy(position = index)
        }
        repository.updateTasks(updatedTasks)
    }
}
