
💧 Hydrate-me

A simple, smart water-reminder app that helps you stay hydrated every day.

The Hydrate-Me App is a lightweight hydration-tracking app that helps you set daily water goals, receive reminders, and monitor your hydration habits over time, plus awards you for your achievements. While storing progress locally using SQL for simplicity and smooth performance.

Features
🔔 Smart Hydration Reminders

Never forget to drink water again—customizable, timely notifications based on your preferences.

📊 Daily Water Tracking

Log water intake with a single tap and visualize your hydration progress throughout the day.

🏆 Achievements & Rewards

Earn badges, XP, streak rewards, and celebrate your consistency with a built-in achievement system.

🎯 Personalized Daily Goals

Set your own goals or let the app calculate suggested daily intake.

💾 Offline-Ready with Local Storage

All hydration data is saved locally using Room (SQL) and works seamlessly without internet.

🌙 Clean Material Design 3 UI

Smooth, modern, and easy to navigate.

flowchart TD

    A[User] -->|Interacts With| B[Hydrate_me Android App]

    subgraph Android App (Kotlin)
        B --> C[UI Layer\Jetpack Compose / XML]
        C --> D[ViewModel\n(Kotlin)]
        D --> E[Repository]
        E --> F[SQL Database\n(Room / SQLite)]
        D --> G[Notification Manager]
    end

    subgraph Android Device
        B
        G --> H[(OS Notification System)]
    end

    subgraph Android Emulator
        I[Emulator] -->|Runs| B
        I -->|Simulates Notifications| H
    end

Installation for Hydrate-Me is available as an installable APK or via build from source.

Put the link for the app [ here]
How users will interact with Hydrate-me app

flowchart TD

    A[User Opens App] --> B{First Time User?}

    B -->|Yes| C[Set Daily Goal\Choose Units (ml/oz)]
    B -->|No| D[Load Settings\from SQL Database]

    C --> E[Save Settings to SQL]
    D --> E

    E --> F[Home Screen\Shows Progress]

    F --> G{Add Water Intake?}

    G -->|Yes| H[Select Amount or Custom Input]
    H --> I[Save Entry to SQL Database]
    I --> F

    G -->|No| J[Schedule Reminder via\WorkManager/AlarmManager]

    J --> K[(OS Notification System)]
    K --> L[User Receives Hydration Reminder]

    L --> F

For developers
hydrate-me/
├── data/             # Room database, DAOs, entities
├── repository/    # Data handling logic
├── UI/                 # Activities, fragments, UI components
├── viewmodel/   # MVVM ViewModels
├── utils/              # Helpers, constants, mappers
└── workers/        # Background tasks (WorkManager)


// While using (Primary language) is Kotlin, Android SDK, Jetpack Libraries
(ViewModel, LiveData/StateFlow, Room, Compose/XML)

When getting started clone Repo
git clone https://github.com/yourusername/Hydrate_me.git
cd Hydrate_me

🛣️ Roadmap

⏱️ Adaptive Reminder System v2 (AI-based intervals)

🧩 Challenge Mode (weekly hydration challenges)

🌍 Data Sync & Cloud Backup

🎨 Better analytics & charts

📱 Widget Support

Contributions are welcome!
To contribute:

Branch off of dev, not main.

Follow the project coding style.

Submit a PR into dev with:

Description of changes

Screenshots (for UI updates)

Linked issues (e.g., Closes #12)

Thank you everyone for making the Hydrate-Me app a great tool for users to build healthier habits while staying hydrated!