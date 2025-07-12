# IXOZ Dictionary

A modern Android dictionary application that allows users to create, manage, and search their personal word collection with images.

## Features

### 📚 Core Functionality
- **Add Words**: Create new dictionary entries with word, meaning, and optional images
- **Edit Entries**: Modify existing words and their meanings
- **Delete Entries**: Remove words from your dictionary
- **Search**: Real-time search through words and meanings
- **Image Support**: Add photos to help remember words visually

### 🎨 Modern UI/UX
- Material Design 3 components
- Dark/Light theme support
- Smooth animations and transitions
- Responsive layout
- Empty state handling
- Search with real-time filtering

### 💾 Data Management
- SQLite database using Room persistence library
- Local storage for images
- Efficient data operations with LiveData
- MVVM architecture pattern

## Technical Stack

- **Language**: Kotlin
- **Architecture**: MVVM (Model-View-ViewModel)
- **Database**: Room (SQLite)
- **UI Components**: Material Design 3
- **Image Loading**: Glide
- **Dependency Injection**: Manual (Repository pattern)

## Project Structure

```
app/src/main/java/com/example/myapplication/
├── DictionaryApplication.kt          # Application class
├── MainActivity.kt                   # Main activity with list and search
├── AddEditEntryActivity.kt          # Activity for adding/editing entries
├── DictionaryEntry.kt               # Data model (Room Entity)
├── DictionaryDao.kt                 # Data Access Object
├── DictionaryDatabase.kt            # Room database
├── DictionaryRepository.kt          # Repository layer
├── DictionaryViewModel.kt           # ViewModel for UI logic
└── DictionaryAdapter.kt             # RecyclerView adapter

app/src/main/res/
├── layout/
│   ├── activity_main.xml           # Main activity layout
│   ├── activity_add_edit_entry.xml # Add/edit activity layout
│   └── item_dictionary_entry.xml   # Individual entry item layout
├── drawable/                       # Vector icons and backgrounds
├── values/
│   ├── colors.xml                  # Color definitions
│   └── themes.xml                  # App themes
└── values-night/
    └── themes.xml                  # Dark theme
```

## Setup Instructions

1. **Clone the repository**
   ```bash
   git clone https://github.com/ixoz/XOZI
   cd MyApplication2
   ```

2. **Open in Android Studio**
   - Open Android Studio
   - Select "Open an existing Android Studio project"
   - Navigate to the project directory and select it

3. **Build and Run**
   - Connect an Android device or start an emulator
   - Click the "Run" button (green play icon) in Android Studio
   - The app will install and launch on your device

## Usage Guide

### Adding a New Word
1. Tap the floating action button (+)
2. Enter the word and its meaning
3. Optionally tap the image area to add a photo
4. Tap "Save" to add the entry

### Searching Words
- Use the search bar at the top to filter words
- Search works on both words and meanings
- Results update in real-time as you type

### Editing an Entry
1. Tap the edit button (pencil icon) on any entry
2. Modify the word, meaning, or image
3. Tap "Save" to update the entry

### Deleting an Entry
1. Tap the delete button (trash icon) on any entry
2. Confirm the deletion in the dialog

## Permissions

The app requires the following permissions:
- `READ_EXTERNAL_STORAGE`: To access images from the device gallery

## Database Schema

The app uses a single table `dictionary_entries` with the following structure:

```sql
CREATE TABLE dictionary_entries (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    word TEXT NOT NULL,
    meaning TEXT NOT NULL,
    imagePath TEXT,
    createdAt INTEGER NOT NULL
);
```

## Architecture

The app follows the MVVM (Model-View-ViewModel) architecture:

- **Model**: `DictionaryEntry` (data class) and `DictionaryDao` (database operations)
- **View**: Activities and layouts
- **ViewModel**: `DictionaryViewModel` (business logic and data management)
- **Repository**: `DictionaryRepository` (data access abstraction)

## Dependencies

Key dependencies used in this project:

```kotlin
// Room Database
implementation("androidx.room:room-runtime:2.6.1")
implementation("androidx.room:room-ktx:2.6.1")
kapt("androidx.room:room-compiler:2.6.1")

// Lifecycle Components
implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")

// RecyclerView
implementation("androidx.recyclerview:recyclerview:1.3.2")

// Image Loading
implementation("com.github.bumptech.glide:glide:4.16.0")

// Material Design
implementation("com.google.android.material:material:1.11.0")
```

## Future Enhancements

Potential improvements for future versions:

- [X] Export/Import functionality
- [ ] Categories/Tags for words
- [X] Pronunciation support
- [ ] Offline dictionary integration
- [ ] Cloud backup
- [ ] Multiple language support
- [ ] Quiz/Flashcard mode
- [X] Statistics and progress tracking

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request
