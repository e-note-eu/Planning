package com.enote.planning.ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.enote.planning.R
import com.enote.planning.domain.model.Task
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TaskScreen(
    viewModel: TaskViewModel = viewModel(),
    isDarkMode: Boolean,
    onThemeToggle: () -> Unit
) {
    val tasks by viewModel.tasks.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var taskToDelete by remember { mutableStateOf<Task?>(null) }
    var taskToViewDetails by remember { mutableStateOf<Task?>(null) }
    
    val lazyListState = rememberLazyListState()
    val density = LocalDensity.current
    val itemHeight = 72.dp
    val itemHeightPx = with(density) { itemHeight.toPx() }

    // Drag and drop state
    var draggedItemIndex by remember { mutableStateOf<Int?>(null) }
    var draggingOffset by remember { mutableFloatStateOf(0f) }

    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { 
                    Text(
                        stringResource(R.string.top_bar_title),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    ) 
                },
                actions = {
                    IconButton(onClick = onThemeToggle) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Default.WbSunny else Icons.Default.Brightness3,
                            contentDescription = stringResource(R.string.theme_toggle_content_desc),
                            tint = if (isDarkMode) Color(0xFFFFD600) else MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .padding(bottom = 80.dp, end = 16.dp)
                    .size(64.dp)
            ) {
                Icon(Icons.Default.Add, modifier = Modifier.size(32.dp), contentDescription = stringResource(R.string.add_task_content_desc))
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
            
            Box(modifier = Modifier.fillMaxSize()) {
                if (tasks.isEmpty()) {
                    EmptyState(modifier = Modifier.align(Alignment.Center))
                } else {
                    val uncompletedTasks = remember(tasks) { tasks.filter { !it.isCompleted } }
                    val completedTasks = remember(tasks) { tasks.filter { it.isCompleted } }

                    LazyColumn(
                        state = lazyListState,
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectDragGesturesAfterLongPress(
                                    onDragStart = { offset ->
                                        val layoutInfo = lazyListState.layoutInfo
                                        val item = layoutInfo.visibleItemsInfo.firstOrNull { 
                                            offset.y.toInt() in it.offset..(it.offset + it.size) 
                                        }
                                        if (item != null && item.index < uncompletedTasks.size) {
                                            draggedItemIndex = item.index
                                        }
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        draggingOffset += dragAmount.y
                                        
                                        draggedItemIndex?.let { currentIndex ->
                                            val threshold = itemHeightPx * 0.6f
                                            if (draggingOffset > threshold && currentIndex < uncompletedTasks.size - 1) {
                                                viewModel.onMove(currentIndex, currentIndex + 1)
                                                draggedItemIndex = currentIndex + 1
                                                draggingOffset -= itemHeightPx
                                            } else if (draggingOffset < -threshold && currentIndex > 0) {
                                                viewModel.onMove(currentIndex, currentIndex - 1)
                                                draggedItemIndex = currentIndex - 1
                                                draggingOffset += itemHeightPx
                                            }
                                        }
                                    },
                                    onDragEnd = {
                                        draggedItemIndex = null
                                        draggingOffset = 0f
                                        viewModel.onDragEnd()
                                    },
                                    onDragCancel = {
                                        draggedItemIndex = null
                                        draggingOffset = 0f
                                        viewModel.onDragEnd()
                                    }
                                )
                            }
                    ) {
                        itemsIndexed(uncompletedTasks, key = { _, task -> task.id }) { index, task ->
                            val isDragging = index == draggedItemIndex
                            val elevation by animateDpAsState(if (isDragging) 12.dp else 0.dp, label = "elevation")
                            
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(itemHeight)
                                    .zIndex(if (isDragging) 10f else 0f)
                                    .graphicsLayer {
                                        translationY = if (isDragging) draggingOffset else 0f
                                        shadowElevation = elevation.toPx()
                                        scaleX = if (isDragging) 1.04f else 1.0f
                                        scaleY = if (isDragging) 1.04f else 1.0f
                                    }
                                    .background(
                                        if (isDragging) MaterialTheme.colorScheme.surfaceVariant 
                                        else MaterialTheme.colorScheme.background
                                    )
                            ) {
                                TaskItem(
                                    task = task,
                                    onCheckedChange = { viewModel.toggleTaskCompletion(task) },
                                    onDelete = { taskToDelete = task },
                                    onClick = { taskToViewDetails = task },
                                    showDragHandle = true
                                )
                            }
                            if (index < uncompletedTasks.size - 1) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    thickness = 0.5.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant
                                )
                            }
                        }

                        if (completedTasks.isNotEmpty()) {
                            item(key = "separator") {
                                CompletedTasksSeparator()
                            }
                            
                            itemsIndexed(completedTasks, key = { _, task -> task.id }) { index, task ->
                                Box(modifier = Modifier.fillMaxWidth().height(itemHeight)) {
                                    TaskItem(
                                        task = task,
                                        onCheckedChange = { viewModel.toggleTaskCompletion(task) },
                                        onDelete = { taskToDelete = task },
                                        onClick = { taskToViewDetails = task },
                                        showDragHandle = false
                                    )
                                }
                                if (index < completedTasks.size - 1) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                        thickness = 0.5.dp,
                                        color = MaterialTheme.colorScheme.outlineVariant
                                    )
                                }
                            }
                        }
                        item { Spacer(modifier = Modifier.height(160.dp)) }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddTaskDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { title, desc ->
                viewModel.addTask(title, desc)
                showAddDialog = false
            }
        )
    }

    taskToDelete?.let { task ->
        DeleteConfirmDialog(
            onDismiss = { taskToDelete = null },
            onConfirm = {
                viewModel.deleteTask(task)
                taskToDelete = null
            }
        )
    }

    taskToViewDetails?.let { task ->
        TaskDetailsDialog(
            task = task,
            onDismiss = { taskToViewDetails = null }
        )
    }
}

@Composable
fun TaskItem(
    task: Task,
    onCheckedChange: (Boolean) -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit,
    showDragHandle: Boolean
) {
    val dateFormatter = remember { SimpleDateFormat("dd.MM.yy HH:mm", Locale.getDefault()) }
    
    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(
                if (task.isCompleted) 
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f) 
                else 
                    MaterialTheme.colorScheme.surface
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showDragHandle) {
            Icon(
                imageVector = Icons.Default.DragHandle,
                contentDescription = "Reorder",
                tint = MaterialTheme.colorScheme.outlineVariant,
                modifier = Modifier.padding(end = 12.dp)
            )
        }
        
        Checkbox(
            checked = task.isCompleted,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp, end = 8.dp)
        ) {
            Text(
                text = task.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                color = if (task.isCompleted) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (task.description.isNotEmpty()) {
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    if (task.isCompleted && task.completedAt != null) {
                        Text(
                            text = "Created: ${dateFormatter.format(Date(task.createdAt))}",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = "Done: ${dateFormatter.format(Date(task.completedAt))}",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = MaterialTheme.colorScheme.outline
                        )
                    } else {
                        Text(
                            text = dateFormatter.format(Date(task.createdAt)),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
        
        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = stringResource(R.string.delete_task_content_desc),
                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun CompletedTasksSeparator() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 32.dp, bottom = 12.dp, start = 16.dp, end = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "COMPLETED",
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            ),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.width(12.dp))
        HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Composable
fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Your list is empty",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.new_task_dialog_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { if (it.length <= 30) title = it },
                    label = { Text(stringResource(R.string.task_title_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = {
                        Text(
                            text = "${title.length}/30",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.End,
                        )
                    },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { if (it.length <= 120) description = it },
                    label = { Text(stringResource(R.string.task_desc_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = {
                        Text(
                            text = "${description.length}/120",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.End,
                        )
                    },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(title, description) }, enabled = title.isNotBlank()) {
                Text(stringResource(R.string.add_button))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel_button))
            }
        }
    )
}

@Composable
fun TaskDetailsDialog(
    task: Task,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                text = task.title,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
            ) 
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (task.description.isNotEmpty()) {
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                val dateFormatter = remember { SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault()) }
                Text(
                    text = "Created: ${dateFormatter.format(Date(task.createdAt))}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.outline
                )
                if (task.isCompleted && task.completedAt != null) {
                    Text(
                        text = "Completed: ${dateFormatter.format(Date(task.completedAt))}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun DeleteConfirmDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.delete_confirm_title)) },
        text = { Text(stringResource(R.string.delete_confirm_msg)) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text(stringResource(R.string.delete_button))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel_button))
            }
        }
    )
}
