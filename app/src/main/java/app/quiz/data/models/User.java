package app.quiz.data.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * User model representing a user in the LinguaRead platform
 * Based on the API documentation from api-docs/user.txt
 */
public class User implements Parcelable {
    private String id;
    private String userName;
    private String email;
    private boolean isActive;
    private String role;

    // Default constructor
    public User() {}

    // Constructor with all fields
    public User(String id, String userName, String email, boolean isActive, String role) {
        this.id = id;
        this.userName = userName;
        this.email = email;
        this.isActive = isActive;
        this.role = role;
    }

    // Parcelable constructor
    protected User(Parcel in) {
        id = in.readString();
        userName = in.readString();
        email = in.readString();
        isActive = in.readByte() != 0;
        role = in.readString();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    // Getters
    public String getId() {
        return id;
    }

    public String getUserName() {
        return userName;
    }

    public String getEmail() {
        return email;
    }

    public boolean isActive() {
        return isActive;
    }

    public String getRole() {
        return role;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public void setRole(String role) {
        this.role = role;
    }

    // Validation methods
    public boolean isValidEmail() {
        return email != null && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public boolean isValidUserName() {
        return userName != null && !userName.trim().isEmpty() && userName.length() >= 3;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(userName);
        dest.writeString(email);
        dest.writeByte((byte) (isActive ? 1 : 0));
        dest.writeString(role);
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", userName='" + userName + '\'' +
                ", email='" + email + '\'' +
                ", isActive=" + isActive +
                ", role='" + role + '\'' +
                '}';
    }
}