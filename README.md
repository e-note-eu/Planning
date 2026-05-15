# Task Planner - Android Technical Showcase

A modern, high-performance Task Management application built with **Jetpack Compose** and **Clean Architecture**. This project demonstrates advanced Android development skills, including custom gesture handling, local persistence, and professional-grade UI/UX.

## 🚀 Key Features

- **Intuitive Task Management**: Create tasks with character-limited fields (Title: 30, Description: 120) and real-time validation.
- **Advanced Drag-and-Drop**: Smooth, continuous reordering of active tasks using a custom-built gesture engine.
- **Smart Completion Tracking**: Toggle status with visual feedback. Completed tasks automatically move to a dedicated section, ordered by completion time.
- **Immersive UI/UX**:
  - **Full Immersive Mode**: Navigation bars are hidden for a focused experience.
  - **Dynamic Theming**: Support for Light/Dark modes with persistent user preference.
  - **Detailed View**: View full task details and timestamps (creation & completion) in an elegant dialog.
- **Localization**: Fully localized in English, following Android best practices for string resources.

## 🏗 Architecture & Best Practices

The app is built using **Clean Architecture** to ensure separation of concerns and testability:

- **Domain Layer**: Framework-independent business logic, models, and Use Cases (`GetTasks`, `AddTask`, `ReorderTasks`, etc.).
- **Data Layer**: Robust persistence using **Room Database** with the Repository pattern and Data Mappers.
- **Presentation Layer**: Reactive UI using **MVVM** and **StateFlow**.
- **Gesture Handling**: Custom `detectDragGesturesAfterLongPress` implementation for fluid reordering.

## 🛠 Tech Stack

- **Language**: Kotlin 2.0+
- **UI**: Jetpack Compose (Material 3)
- **Database**: Room (with KSP)
- **Concurrency**: Coroutines & Flow
- **Dependency Management**: Gradle Version Catalog
- **Architecture**: Clean Architecture + MVVM

## 📂 Project Structure

```text
com.enote.planning/
├── data/           # Room Entity, DAO, DB, and Repository Implementation
├── domain/         # Business Models (POJO), Repository Interfaces, and Use Cases
├── ui/             # Compose Screens, ViewModels, and App Theme
└── MainActivity.kt # Entry Point with Immersive Mode & Theme management
```

## 📝 How to Run

1. Clone the repository.
2. Open in **Android Studio** (2024.2.1+).
3. Sync Gradle and run on a device with **Min SDK 26**.

---
*Developed as a portfolio project showcasing modern Android standards and attention to user experience.*
