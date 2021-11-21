package com.example.attendancesystem;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ValidateInputs {
    private String error;
    private JDBCPostgreSQLConnect sqlConnect;

    public ValidateInputs() {
        sqlConnect = new JDBCPostgreSQLConnect();
    }

    public String getError() {
        return error;
    }

    public boolean validateFirstName(String firstName) {
        if (Pattern.matches("^[A-Za-z '-]{1,50}$", firstName)) {
            return true;
        } else {
            error = "First name is invalid.";
            return false;
        }
    }

    public boolean validateLastName(String lastName) {
        if (Pattern.matches("^[A-Za-z '-]{1,50}$", lastName)) {
            return true;
        } else {
            error = "Last name is invalid.";
            return false;
        }
    }

    public boolean validateUsername(String username) {
        if (Pattern.matches("^[A-Za-z0-9 _.-]{1,50}$", username)) {
            return true;
        } else {
            error = "Username is invalid.";
            return false;
        }
    }
    public boolean validateNewUsername(String username) {
        if (Pattern.matches("^[A-Za-z0-9 _.-]{1,50}$", username)) {
            if (!sqlConnect.usernameInUse(username)) {
                return true;
            } else {
                error = "Username is in use.";
                return false;
            }
        } else {
            error = "Username is invalid.";
            return false;
        }
    }

    public boolean validatePasswords(String password, String confirmPassword) {
        if (Pattern.matches("^[A-Za-z0-9!@#$%^&*().,_-]{5,25}$", password)) {
            if (password.equals(confirmPassword)) {
                return true;
            } else {
                error = "Password and Confirm Password do not match.";
                return false;
            }
        } else {
            error = "Password is invalid.";
            return false;
        }
    }

    public boolean validateNewPasswords(String password, String confirmPassword) {
        if (Pattern.matches("^[A-Za-z0-9!@#$%^&*().,_-]{5,25}$", password)) {
            if (password.equals(confirmPassword)) {
                return true;
            } else {
                error = "New Password and Confirm New Password do not match.";
                return false;
            }
        } else {
            error = "New Password is invalid.";
            return false;
        }
    }

    public boolean validatePassword(String password) {
        if (Pattern.matches("^[A-Za-z0-9!@#$%^&*().,_-]{5,25}$", password)) {
            return true;
        } else {
            error = "Password is invalid.";
            return false;
        }
    }

    public boolean validateTime(String time) {
        Pattern pattern = Pattern.compile("^([0-9]{1,2}):([0-9]{1,2}):([0-9]{1,2})$");
        Matcher matcher = pattern.matcher(time);
        if (matcher.matches()) {
            int hours = Integer.parseInt(matcher.group(1));
            int minutes = Integer.parseInt(matcher.group(2));
            int seconds = Integer.parseInt(matcher.group(3));
            if (hours >= 0 && hours < 24 &&
                minutes >= 0 && minutes < 60 &&
                seconds >= 0 && seconds < 60) {
                return true;
            } else {
                error = "Invalid hours, minutes, or seconds.";
                return false;
            }
        } else {
            error = "Time must be in XX:XX:XX format.";
            return false;
        }
    }

    public boolean validateDate(String date) {
        Pattern pattern = Pattern.compile("^([0-9]{1,2})/([0-9]{1,2})/([0-9]{1,4})$");
        Matcher matcher = pattern.matcher(date);
        if (matcher.matches()) {
            int month = Integer.parseInt(matcher.group(1));
            int day = Integer.parseInt(matcher.group(2));
            int year = Integer.parseInt(matcher.group(3));
            if (year >= 0 && year < 9999 &&
                    month >= 1 && month <= 12 &&
                    day >= 1 && day <= 31) {
                return true;
            } else {
                error = "Invalid year, month, or day.";
                return false;
            }
        } else {
            error = "Date must be in XX/XX/XXXX format.";
            return false;
        }
    }
}
