# Android Quiz App - Package Diagram

## Project Package Structure

```
app.quiz
├── MainActivity.java
├── data/
│   ├── models/
│   │   ├── Flashcard.java
│   │   ├── FlashcardGroup.java
│   │   ├── LoginRequest.java
│   │   ├── LoginResponse.java
│   │   ├── PagedResponse.java
│   │   ├── SignupRequest.java
│   │   └── User.java
│   └── remote/
│       ├── ApiClient.java
│       └── FlashcardService.java
├── ui/
│   ├── activities/
│   │   ├── CreateFlashcardActivity.java
│   │   ├── FlashcardDetailActivity.java
│   │   ├── FlashcardListActivity.java
│   │   ├── LoginActivity.java
│   │   ├── MyFlashcardsActivity.java
│   │   ├── ProfileActivity.java
│   │   └── SignupActivity.java
│   └── adapters/
│       ├── CreateFlashcardAdapter.java
│       ├── FlashcardGroupAdapter.java
│       ├── FlashcardListAdapter.java
│       └── FlashcardSliderAdapter.java
└── utils/
    └── SessionManager.java
```

## Package Descriptions

### Root Package: `app.quiz`
- **MainActivity.java**: Main entry point of the application

### Data Layer: `app.quiz.data`
Handles data management, API communication, and data models.

#### Models Package: `app.quiz.data.models`
Contains data transfer objects and entity classes:
- **Flashcard.java**: Represents individual flashcard data
- **FlashcardGroup.java**: Represents a collection of flashcards
- **LoginRequest.java**: Data model for login API requests
- **LoginResponse.java**: Data model for login API responses
- **PagedResponse.java**: Generic wrapper for paginated API responses
- **SignupRequest.java**: Data model for user registration requests
- **User.java**: User entity model

#### Remote Package: `app.quiz.data.remote`
Handles network communication and API services:
- **ApiClient.java**: HTTP client configuration and setup
- **FlashcardService.java**: API service for flashcard-related operations

### UI Layer: `app.quiz.ui`
Contains all user interface components.

#### Activities Package: `app.quiz.ui.activities`
Contains all activity classes for different screens:
- **CreateFlashcardActivity.java**: Screen for creating new flashcards
- **FlashcardDetailActivity.java**: Detailed view of a single flashcard
- **FlashcardListActivity.java**: List view of flashcards
- **LoginActivity.java**: User authentication screen
- **MyFlashcardsActivity.java**: User's personal flashcard collection
- **ProfileActivity.java**: User profile management screen
- **SignupActivity.java**: User registration screen

#### Adapters Package: `app.quiz.ui.adapters`
Contains RecyclerView adapters for list displays:
- **CreateFlashcardAdapter.java**: Adapter for flashcard creation interface
- **FlashcardGroupAdapter.java**: Adapter for displaying flashcard groups
- **FlashcardListAdapter.java**: Adapter for flashcard list views
- **FlashcardSliderAdapter.java**: Adapter for flashcard slider/carousel views

### Utilities Package: `app.quiz.utils`
Contains utility classes and helper functions:
- **SessionManager.java**: Manages user session and authentication state

## Package Dependencies

### Dependency Flow
```
UI Layer (activities, adapters)
    ↓ depends on
Data Layer (models, remote)
    ↓ depends on
Utils Layer (SessionManager)
```

### Detailed Dependencies

1. **UI Activities** depend on:
   - Data models for displaying information
   - Remote services for API calls
   - SessionManager for authentication
   - UI adapters for list displays

2. **UI Adapters** depend on:
   - Data models for binding data to views
   - Activities for handling user interactions

3. **Remote Services** depend on:
   - Data models for request/response handling
   - ApiClient for HTTP communication

4. **SessionManager** is used by:
   - Activities for authentication checks
   - Remote services for adding auth headers

## Architecture Pattern

The project follows a **layered architecture** with clear separation of concerns:

- **Presentation Layer**: UI package (activities, adapters)
- **Data Layer**: Data package (models, remote services)
- **Utility Layer**: Utils package (session management)

This structure promotes:
- **Maintainability**: Clear separation of responsibilities
- **Testability**: Each layer can be tested independently
- **Scalability**: Easy to add new features within existing structure
- **Reusability**: Models and utilities can be shared across components

## Key Design Principles

1. **Single Responsibility**: Each class has a specific purpose
2. **Dependency Inversion**: Higher-level modules don't depend on lower-level modules
3. **Separation of Concerns**: UI, data, and utility logic are separated
4. **Modularity**: Related classes are grouped in appropriate packages

## Future Considerations

For enhanced architecture, consider adding:
- **Repository Pattern**: Abstract data access layer
- **ViewModel Classes**: For MVVM architecture implementation
- **Dependency Injection**: For better testability and loose coupling
- **Local Database**: For offline data storage
- **Interfaces Package**: For defining contracts between layers