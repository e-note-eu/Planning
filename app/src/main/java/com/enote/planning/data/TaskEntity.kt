package com.enote.planning.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.enote.planning.domain.model.Task

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val description: String,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val position: Int = 0
)

fun TaskEntity.toDomain(): Task {
    return Task(id, title, description, isCompleted, createdAt, completedAt, position)
}

fun Task.toEntity(): TaskEntity {
    return TaskEntity(id, title, description, isCompleted, createdAt, completedAt, position)
}
