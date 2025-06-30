# Android Quiz App

A modern Android application for language learning and quiz management, built with Java and following Material Design principles.

## Features Implemented

### Authentication System
- **UC-01 Login**: Secure user authentication with session management
- **UC-02 Logout**: Session termination with confirmation dialog
- **UC-03 Signup**: User registration with validation

### Security Features
- Session timeout (30 minutes)
- Account lockout after 5 failed login attempts (15-minute lockout)
- Password complexity validation
- Email format validation
- Input sanitization

### UI Components
- Material Design components
- Responsive layouts with ConstraintLayout
- Custom error message styling
- Progress indicators
- Confirmation dialogs

## Project Structure

```
app/src/main/java/app/quiz/
├── MainActivity.java                    # Main dashboard activity
├── ui/activities/
│   ├── LoginActivity.java              # Login screen (UC-01)
│   └── SignupActivity.java             # Registration screen (UC-03)
├── data/
│   ├── models/
│   │   ├── User.java                   # User data model
│   │   ├── LoginRequest.java           # Login request model
│   │   ├── LoginResponse.java          # Login response model
│   │   └── SignupRequest.java          # Registration request model
│   └── remote/
│       └── ApiClient.java              # HTTP client for API calls
└── utils/
    └── SessionManager.java             # Session and authentication management
```

## API Integration

The app integrates with the backend API running on `localhost:3333/api/v1`:

- `POST /api/v1/User/register` - User registration
- `POST /api/v1/User/login` - User authentication

## Build Requirements

- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 35 (Android 15)
- **Java Version**: 11
- **Build Tool**: Gradle with Kotlin DSL

## Building the App

1. **Build the project**:
   ```bash
   ./gradlew build
   ```

2. **Install on device/emulator**:
   ```bash
   ./gradlew installDebug
   ```

## Testing

### API Testing
Run the provided test script to verify backend connectivity:
```bash
./test_api.sh
```

### Manual Testing
1. Start an Android emulator or connect a physical device
2. Install the app using `./gradlew installDebug`
3. Test the following scenarios:

#### Registration Flow
- Open the app (launches LoginActivity)
- Tap "Sign Up" to go to registration
- Test validation:
  - Empty fields
  - Invalid email format
  - Weak password
  - Password mismatch
- Register with valid credentials
- Verify redirect to MainActivity

#### Login Flow
- Enter valid credentials
- Verify successful login and redirect to MainActivity
- Test invalid credentials (should show error)
- Test account lockout (5 failed attempts)

#### Session Management
- Login successfully
- Navigate to MainActivity
- Test logout functionality
- Test session timeout (wait 30 minutes or modify timeout for testing)

#### Main Dashboard
- Verify welcome message with user email
- Test logout from menu
- Verify logout confirmation dialog

## Business Rules Implemented

### BR-01: Unique Email
- Email addresses must be unique across all users
- Registration fails if email already exists

### BR-02: Password Policy
- Minimum 8 characters
- At least one uppercase letter
- At least one lowercase letter
- At least one digit
- At least one special character

### BR-03: Session Timeout
- Sessions expire after 30 minutes of inactivity
- Users are automatically logged out when session expires

### BR-04: Account Lockout
- Account locked after 5 consecutive failed login attempts
- Lockout duration: 15 minutes
- Attempts reset after successful login

## Architecture

### Design Patterns
- **MVVM Architecture**: Separation of concerns
- **Repository Pattern**: Data access abstraction
- **Singleton Pattern**: SessionManager instance
- **Observer Pattern**: UI updates via callbacks

### Key Components

#### SessionManager
- Manages user authentication state
- Handles session timeout
- Tracks failed login attempts
- Stores user preferences securely

#### ApiClient
- HTTP client for backend communication
- Handles request/response serialization
- Implements error handling and callbacks

#### Activities
- **LoginActivity**: Implements UC-01 with validation and error handling
- **SignupActivity**: Implements UC-03 with comprehensive validation
- **MainActivity**: Dashboard with logout functionality (UC-02)

## Security Considerations

1. **Input Validation**: All user inputs are validated client-side
2. **Session Management**: Secure token storage using SharedPreferences
3. **Network Security**: HTTPS recommended for production
4. **Error Handling**: Generic error messages to prevent information disclosure
5. **Account Protection**: Automatic lockout prevents brute force attacks

## Next Steps

To extend the application:

1. **Add Quiz Features**:
   - Quiz listing and selection
   - Question display and answering
   - Score tracking and results

2. **Enhanced Security**:
   - Biometric authentication
   - Certificate pinning
   - Token refresh mechanism

3. **User Experience**:
   - Offline support
   - Push notifications
   - Dark mode support

4. **Testing**:
   - Unit tests for business logic
   - UI tests with Espresso
   - Integration tests

## Troubleshooting

### Common Issues

1. **Build Failures**:
   - Ensure all dependencies are properly configured
   - Check for missing string/color/dimension resources

2. **API Connection Issues**:
   - Verify backend server is running on localhost:3333/api/v1
   - Check network permissions in AndroidManifest.xml
   - Ensure device/emulator can access localhost

3. **Installation Issues**:
   - Connect an Android device or start an emulator
   - Enable USB debugging on physical devices
   - Check minimum SDK compatibility

## License

This project is part of the Android Quiz App development and follows the established coding standards and architectural patterns.