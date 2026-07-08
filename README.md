# TaskFlow 📋

A Persian-first Android task manager with **chain-based dependency logic** — tasks unlock sequentially, so you can't complete step 2 before finishing step 1.

## Screenshots

<img width="486" height="1080" alt="photo_2_2026-07-08_10-01-55" src="https://github.com/user-attachments/assets/0577b8a8-eff2-46b7-8cee-51376f71a133" />
<img width="486" height="1080" alt="photo_8_2026-07-08_10-01-55" src="https://github.com/user-attachments/assets/5fbf4bb9-6d36-4170-95d7-1e69b23d7678" />
<img width="486" height="1080" alt="photo_5_2026-07-08_10-01-55" src="https://github.com/user-attachments/assets/3cea77e7-89a9-461a-a68f-513a99a84fff" />
<img width="486" height="1080" alt="photo_6_2026-07-08_10-01-55" src="https://github.com/user-attachments/assets/3be7576c-bc24-477f-bb38-0a4f6672d7b8" />

> _Add screenshots here_

## Features

- 🔗 **Chain-based tasks** — tasks are linked in sequence; each unlocks only after the previous is completed
- 🔒 **Dependency locking** — locked tasks are clearly marked and automatically unlock on completion
- ⏰ **Deadlines & reminders** — set deadlines with WorkManager-powered notifications that survive device restarts
- 📅 **Persian (Shamsi) calendar** — fully localized date picker and display with custom algorithm (no third-party library)
- 🔢 **Persian digits** — all numbers displayed in Persian throughout the app
- 📊 **Progress tracking** — visual progress bar per chain with current task indicator
- ✏️ **Edit & reorder** — drag-and-drop task reordering with expand/collapse UI
- 📋 **Revision system** — mark completed tasks for review with notes
- 🔁 **Duplicate chains** — swipe to duplicate any chain as a template
- 📤 **Export** — share chain progress as formatted Persian text
- 🏠 **Home screen widget** — see pending tasks at a glance
- 🌿 **Glassmorphism UI** — matcha green theme with glass-effect cards
- 🌙 **Dark/Light mode** — follows system theme

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Kotlin |
| UI | Jetpack Compose + Material3 |
| Architecture | MVVM + Clean Architecture |
| DI | Hilt |
| Database | Room |
| Async | Coroutines + Flow |
| Notifications | WorkManager |
| Navigation | Navigation Compose |
| Widget | Glance |
| Font | Vazirmatn |

## Architecture

```
taskflow/
├── data/
│   ├── local/        — Room DB, DAOs, Entities
│   └── repository/   — Repository implementations
├── domain/
│   ├── model/        — Task, Chain, Reminder
│   └── usecase/      — Business logic
├── ui/
│   ├── home/         — Home screen
│   ├── chain/        — Chain & task screens
│   ├── task/         — Task detail
│   ├── calendar/     — Persian calendar view
│   └── theme/        — Glassmorphism theme
├── worker/           — WorkManager notification workers
└── di/               — Hilt modules
```

## Getting Started

1. Clone the repository
```bash
git clone https://github.com/mnfarzaneh/TaskFlow.git
```

2. Open in Android Studio (Hedgehog or newer)

3. Build and run on a device with API 26+

## Requirements

- Android 8.0 (API 26) or higher
- Android Studio Hedgehog+

## License

MIT License — feel free to use and modify.

---

Built with ❤️ by [@mnfarzaneh](https://github.com/mnfarzaneh)
