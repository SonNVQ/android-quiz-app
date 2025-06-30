package app.quiz.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import app.quiz.data.models.User;

/**
 * Session manager for handling user authentication state and preferences
 * Implements business rules for session timeout and login attempt limits
 */
public class SessionManager {
    private static final String TAG = "SessionManager";
    private static final String PREF_NAME = "LinguaReadSession";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_AUTH_TOKEN = "authToken";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USER_NAME = "userName";
    private static final String KEY_USER_EMAIL = "userEmail";
    private static final String KEY_USER_ROLE = "userRole";
    private static final String KEY_LOGIN_ATTEMPTS = "loginAttempts";
    private static final String KEY_LAST_LOGIN_ATTEMPT = "lastLoginAttempt";
    private static final String KEY_LAST_ACTIVITY = "lastActivity";
    
    // Business rules constants
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final long LOCKOUT_DURATION = 15 * 60 * 1000; // 15 minutes in milliseconds
    private static final long SESSION_TIMEOUT = 30 * 60 * 1000; // 30 minutes in milliseconds
    
    private final SharedPreferences preferences;
    private final SharedPreferences.Editor editor;
    private static SessionManager instance;
    
    private SessionManager(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = preferences.edit();
    }
    
    public static synchronized SessionManager getInstance(Context context) {
        if (instance == null) {
            instance = new SessionManager(context.getApplicationContext());
        }
        return instance;
    }
    
    /**
     * Create user session after successful login
     * @param token Authentication token
     * @param user User information
     */
    public void createSession(String token, User user) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_AUTH_TOKEN, token);
        editor.putString(KEY_USER_ID, user.getId());
        editor.putString(KEY_USER_NAME, user.getUserName());
        editor.putString(KEY_USER_EMAIL, user.getEmail());
        editor.putString(KEY_USER_ROLE, user.getRole());
        editor.putLong(KEY_LAST_ACTIVITY, System.currentTimeMillis());
        editor.apply();
        
        // Reset login attempts on successful login
        resetLoginAttempts();
        
        Log.d(TAG, "Session created for user: " + user.getUserName());
    }
    
    /**
     * Create session with token only (for login response)
     * @param token Authentication token
     */
    public void createSession(String token) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_AUTH_TOKEN, token);
        editor.putLong(KEY_LAST_ACTIVITY, System.currentTimeMillis());
        editor.apply();
        
        // Reset login attempts on successful login
        resetLoginAttempts();
        
        Log.d(TAG, "Session created with token");
    }
    
    /**
     * Clear user session (logout)
     */
    public void clearSession() {
        editor.clear();
        editor.apply();
        Log.d(TAG, "Session cleared");
    }
    
    /**
     * Check if user is logged in and session is valid
     * @return true if user is logged in and session hasn't expired
     */
    public boolean isLoggedIn() {
        boolean isLoggedIn = preferences.getBoolean(KEY_IS_LOGGED_IN, false);
        if (!isLoggedIn) {
            return false;
        }
        
        // Check session timeout
        long lastActivity = preferences.getLong(KEY_LAST_ACTIVITY, 0);
        long currentTime = System.currentTimeMillis();
        
        if (currentTime - lastActivity > SESSION_TIMEOUT) {
            Log.d(TAG, "Session expired due to inactivity");
            clearSession();
            return false;
        }
        
        return true;
    }
    
    /**
     * Update last activity timestamp
     */
    public void updateActivity() {
        if (isLoggedIn()) {
            editor.putLong(KEY_LAST_ACTIVITY, System.currentTimeMillis());
            editor.apply();
        }
    }
    
    /**
     * Get authentication token
     * @return Authentication token or null if not logged in
     */
    public String getAuthToken() {
        if (isLoggedIn()) {
            return preferences.getString(KEY_AUTH_TOKEN, null);
        }
        return null;
    }
    
    /**
     * Get current user information
     * @return User object or null if not logged in
     */
    public User getCurrentUser() {
        if (!isLoggedIn()) {
            return null;
        }
        
        User user = new User();
        user.setId(preferences.getString(KEY_USER_ID, ""));
        user.setUserName(preferences.getString(KEY_USER_NAME, ""));
        user.setEmail(preferences.getString(KEY_USER_EMAIL, ""));
        user.setRole(preferences.getString(KEY_USER_ROLE, ""));
        user.setActive(true); // Assume active if logged in
        
        return user;
    }
    
    /**
     * Record failed login attempt
     */
    public void recordFailedLoginAttempt() {
        int attempts = preferences.getInt(KEY_LOGIN_ATTEMPTS, 0) + 1;
        editor.putInt(KEY_LOGIN_ATTEMPTS, attempts);
        editor.putLong(KEY_LAST_LOGIN_ATTEMPT, System.currentTimeMillis());
        editor.apply();
        
        Log.d(TAG, "Failed login attempt recorded. Total attempts: " + attempts);
    }
    
    /**
     * Reset login attempts counter
     */
    public void resetLoginAttempts() {
        editor.putInt(KEY_LOGIN_ATTEMPTS, 0);
        editor.putLong(KEY_LAST_LOGIN_ATTEMPT, 0);
        editor.apply();
    }
    
    /**
     * Check if account is temporarily locked due to failed attempts
     * @return true if account is locked
     */
    public boolean isAccountLocked() {
        int attempts = preferences.getInt(KEY_LOGIN_ATTEMPTS, 0);
        if (attempts < MAX_LOGIN_ATTEMPTS) {
            return false;
        }
        
        long lastAttempt = preferences.getLong(KEY_LAST_LOGIN_ATTEMPT, 0);
        long currentTime = System.currentTimeMillis();
        
        if (currentTime - lastAttempt > LOCKOUT_DURATION) {
            // Lockout period has expired, reset attempts
            resetLoginAttempts();
            return false;
        }
        
        return true;
    }
    
    /**
     * Get remaining lockout time in milliseconds
     * @return Remaining lockout time or 0 if not locked
     */
    public long getRemainingLockoutTime() {
        if (!isAccountLocked()) {
            return 0;
        }
        
        long lastAttempt = preferences.getLong(KEY_LAST_LOGIN_ATTEMPT, 0);
        long currentTime = System.currentTimeMillis();
        long elapsed = currentTime - lastAttempt;
        
        return Math.max(0, LOCKOUT_DURATION - elapsed);
    }
    
    /**
     * Get number of failed login attempts
     * @return Number of failed attempts
     */
    public int getFailedLoginAttempts() {
        return preferences.getInt(KEY_LOGIN_ATTEMPTS, 0);
    }
    
    /**
     * Get remaining login attempts before lockout
     * @return Number of remaining attempts
     */
    public int getRemainingLoginAttempts() {
        return Math.max(0, MAX_LOGIN_ATTEMPTS - getFailedLoginAttempts());
    }
    
    /**
     * Get current user's email
     * @return User email or null if not logged in
     */
    public String getUserEmail() {
        if (!isLoggedIn()) {
            return null;
        }
        return preferences.getString(KEY_USER_EMAIL, null);
    }
}